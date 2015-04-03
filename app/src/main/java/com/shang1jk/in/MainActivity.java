package com.shang1jk.in;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


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
}
