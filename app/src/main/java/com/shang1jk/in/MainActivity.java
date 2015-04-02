package com.shang1jk.in;

import android.net.IpPrefix;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("xxx", "xxx");

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    scanLAN();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * scan LAN and deal the results
     */
    private void scanLAN() throws IOException {
//        InetAddress
//        InetSocketAddress

//        import android.net.ConnectivityManager;
//        import android.net.DhcpInfo;
//        import android.net.NetworkInfo;
//        import android.net.NetworkInfo.State;
//        import android.net.wifi.SupplicantState;
//        import android.net.wifi.WifiInfo;
//        import android.net.wifi.WifiManager;
//        import android.net.wifi.WifiManager.WifiLock;


//        DhcpInfo dhcpInfo = new DhcpInfo();

        Runtime runtime = Runtime.getRuntime();
        String ip = "192.168.199.190";
        Process exec = runtime.exec("ping " + ip + "-w 3 -n 3");


        String mac = getMac(ip);
        Log.e("MAC", mac);
    }

    private String getMac(String ip) throws SocketException, UnknownHostException {
        NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(ip));
        byte[] mac = ni.getHardwareAddress();
        StringBuffer sb = new StringBuffer();
        for (byte b : mac) {
            int i = b & 0xFF; //转为正整数
            String s = Integer.toHexString(i).toUpperCase() ;
            sb.append(s.length() == 1 ? 0 + s : s);
            sb.append(":");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
