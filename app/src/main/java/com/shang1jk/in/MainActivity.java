package com.shang1jk.in;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {
    private final String ip = "192.168.1.107";
    private Runtime runtime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runtime = Runtime.getRuntime();
        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanLAN();
            }
        });
        findViewById(R.id.scan_udp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UdpProber().start();
            }
        });

    }

    /**
     * scan LAN and deal the results
     */
    private void scanLAN() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String localhost = getLocalhost();
                    String ipPerfix = localhost.substring(0, localhost.lastIndexOf('.') + 1);
                    Log.e(getLocalClassName(), "localhost:" + localhost);
                    for (int i = 0; i < 256; i++) {
                        String ip = ipPerfix + i;
                        String mac = "";
                        //TODO: 减小超时时间
                        Process exec = runtime.exec("ping -c 1 -W 1 " + ip);    //ping 1 次，超时时间 1 秒
                        boolean reachable = (exec.waitFor() == 0);

                        InetAddress inetAddress = InetAddress.getByName(ip);
                        if (reachable) {
                            mac = getMac(inetAddress);
                            if (mac != null) {
                                Log.e(getLocalClassName(), "ip:" + ip + ", mac:" + mac);
                            }
                        } else {
                            Log.e(getLocalClassName(), "ip:: " + ip + " isn't reachable");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获取指定地址对应的mac
     *
     * @param inetAddress
     * @return
     */
    private String getMac(InetAddress inetAddress) {
        try {
            NetworkInterface ni = NetworkInterface.getByInetAddress(inetAddress);
            byte[] mac = ni.getHardwareAddress();
            StringBuffer sb = new StringBuffer();
            for (byte b : mac) {
                int i = b & 0xFF; //转为正整数
                String s = Integer.toHexString(i).toUpperCase();
                sb.append(s.length() == 1 ? 0 + s : s);
                sb.append(":");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } catch (NullPointerException e) {
            Log.e("error", inetAddress.getHostAddress());
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return 本地ip
     */
    private String getLocalhost() {
        String localIp = null;
        try {
            Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.getName().toLowerCase().contains("wlan")) {
                    Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress != null && inetAddress instanceof Inet4Address) {
                            localIp = inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return localIp;
    }

    private InetAddress getHostAddress() {
        try {
            Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.getName().toLowerCase().contains("wlan")) {
                    Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress != null && inetAddress instanceof Inet4Address) {
                            return inetAddress;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
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
     * arp扫描？
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

                    socket.setSoTimeout(200);
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
            Log.e("xxx", "UdpProber started ...");

            InetAddress hostAddress = getHostAddress();
            byte[] address = hostAddress.getAddress();
            for (int i = 0; i < 250; i++) {
                address[3] += 0b1;
                try {
                    InetAddress byAddress = Inet4Address.getByAddress(address);
                    mExecutor.execute(new SingleProber(byAddress));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }

        }

        public synchronized void exit() {
            try {
                mExecutor.shutdown();
                mExecutor.awaitTermination(30, TimeUnit.SECONDS);
                mExecutor.shutdownNow();
            } catch (Exception ignored) {
            }
        }
    }

    private void getDHCPInfo() {
        /*
        *
        * gateway = new Target(mNetwork.getGatewayAddress(), mNetwork.getGatewayHardware()),
        device = new Target(mNetwork.getLocalAddress(), mNetwork.getLocalHardware());
        * */
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        int ipAddress = dhcpInfo.ipAddress;
        int gateway = dhcpInfo.gateway;
        String bssid = connectionInfo.getBSSID();
        NetworkInterface mInterface = NetworkInterface.getByInetAddress(getLocalAddress());
    }


}
