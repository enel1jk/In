package com.shang1jk.in;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends ActionBarActivity {
    private final int IP_COUNT = 256 - 2 - 2;   //256 - x.x.x.0 - x.x.x.255 - local - gateway
    private final String ARP_PATH = "/proc/net/arp";
    private EndPoint local, gateway;
    boolean isScanning = false;

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
                        local = NetUtil.getLocal(MainActivity.this);
                        gateway = NetUtil.getGateway(MainActivity.this);
                        UdpProber udpProber = new UdpProber(MainActivity.this);
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


        findViewById(R.id.scan_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(MainActivity.this, ScanService.class);
                startService(service);

//                //TODO: 做成service？
//                Timer timer = new Timer("scan_timer", true);
//                TimerTask task = new TimerTask() {
//                    @Override
//                    public void run() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                findViewById(R.id.scan).performClick();
//                            }
//                        });
//                    }
//                };
//                if (!isScanning) {
//                    timer.schedule(task, 0, 2 * 60 * 1000);
//                    isScanning = true;
//                }
            }
        });

    }

    private void readArp() {
        try {
            FileReader reader = new FileReader(ARP_PATH);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String s = null;
            while ((s = bufferedReader.readLine()) != null) {
                Log.e("arp table", s);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
