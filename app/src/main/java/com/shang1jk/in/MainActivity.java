package com.shang1jk.in;

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
    private final String ip = "192.168.1.107";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("xxx", "xxx");

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
        /*isReachable*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean reachable = false;
                try {
                    reachable = InetAddress.getByName(ip).isReachable(3000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (reachable) {
                    Log.e("isReachable", "yes");
                } else {
                    Log.e("isReachable", "no");
                }
            }
        }).start();


//        String mac = getMac(ip);
//        Log.e("MAC", mac);
    }

    private String getMac(String ip) throws UnknownHostException, SocketException {
        NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(ip));
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
    }
}
