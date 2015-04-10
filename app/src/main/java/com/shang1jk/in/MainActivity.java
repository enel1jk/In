package com.shang1jk.in;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends ActionBarActivity {
    private final int IP_COUNT = 256 - 2 - 2;   //256 - x.x.x.0 - x.x.x.255 - local - gateway
    private final String ARP_PATH = "/proc/net/arp";
    private EndPoint local, gateway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        local = getLocal();
                        gateway = getGateway();
                        UdpProber udpProber = new UdpProber();
                        udpProber.start();

                        ThreadPoolExecutor executor = udpProber.getExecutor();
                        while (true) {
                            if (executor.getCompletedTaskCount() >= IP_COUNT) {
                                executor.shutdown();
                                readArp();
                                break;
                            }
                        }

                    }
                }).start();
            }
        });

        findViewById(R.id.read).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readArp();
            }
        });

    }

    private void readArp() {
        try {
            FileReader reader = new FileReader(ARP_PATH);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String s = null;
            int pointCount = 0;
            while ((s = bufferedReader.readLine()) != null) {
                pointCount++;
                Log.e("arp table", s);
            }
//            Log.e("point count: ", "" + pointCount);
            System.out.println("" + pointCount);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final short NETBIOS_UDP_PORT = 137;
    // NBT UDP PACKET: QUERY; REQUEST; UNICAST
    private static final byte[] NETBIOS_REQUEST =
            {
                    (byte) 0x82, (byte) 0x28, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                    (byte) 0x1, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
                    (byte) 0x0, (byte) 0x0, (byte) 0x20, (byte) 0x43, (byte) 0x4B,
                    (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                    (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                    (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                    (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                    (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                    (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
                    (byte) 0x0, (byte) 0x0, (byte) 0x21, (byte) 0x0, (byte) 0x1
            };


    /**
     * arp广播
     */
    private class UdpProber extends Thread {
        private static final int PROBER_THREAD_POOL_SIZE = 25;

        private class SingleProber extends Thread {
            private InetAddress mAddress = null;

            public SingleProber(InetAddress address) {
                mAddress = address;
            }

            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(NETBIOS_REQUEST, NETBIOS_REQUEST.length, mAddress, NETBIOS_UDP_PORT);

                    socket.setSoTimeout(1000);
                    socket.send(packet);

                    socket.close();
                } catch (Exception ignored) {
                }

            }
        }

        private ThreadPoolExecutor mExecutor = null;

        public UdpProber() {
            mExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(PROBER_THREAD_POOL_SIZE);
        }

        @Override
        public void run() {
            Log.i(getClass().getSimpleName(), "UdpProber started ...");

            byte[] localIpByteArray = local.getIpByteArray();

            //x.x.x.1~x.x.x.254
            for (int i = 1; i < 255; i++) {
                if (i != local.getIpByteArray()[3] && i != gateway.getIpByteArray()[3]) {
                    try {
                        InetAddress byAddress = Inet4Address.getByAddress(localIpByteArray);
                        mExecutor.execute(new SingleProber(byAddress));
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    localIpByteArray[3]++;
                }
            }
        }

        public ThreadPoolExecutor getExecutor() {
            return mExecutor;
        }

    }

    /**
     * @return 本地ip、mac
     */
    private EndPoint getLocal() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        int ipAddress = dhcpInfo.ipAddress;
        //ip
        String localIp = EndPoint.convertIp2String(ipAddress);
        //mac
        String localMac = null;
        try {
            NetworkInterface mInterface = NetworkInterface.getByInetAddress(Inet4Address.getByAddress(EndPoint.convertIp2ByteArray(ipAddress)));
            byte[] hardwareAddress = mInterface.getHardwareAddress();
            localMac = EndPoint.convertMac2String(hardwareAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Log.i(getLocalClassName(), "local: " + localIp + "\t" + localMac);
        return new EndPoint(localIp, localMac);
    }

    /**
     * @return 网关ip、mac
     */
    private EndPoint getGateway() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        int gateway = dhcpInfo.gateway;
        //ip
        String gatewayIp = EndPoint.convertIp2String(gateway);
        //mac
        String gatewayMac = connectionInfo.getBSSID();

        Log.i(getLocalClassName(), "gateway: " + gatewayIp + "\t" + gatewayMac.toUpperCase());
        return new EndPoint(gatewayIp, gatewayMac);
    }

}
