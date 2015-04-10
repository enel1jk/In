package com.shang1jk.in;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends ActionBarActivity {
    private final int IP_COUNT = 256 - 2 - 2;   //256 - x.x.x.0 - x.x.x.255 - local - gateway
    private final String ARP_PATH = "/proc/net/arp";
    private EndPoint local, gateway;
    boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.scan)
    public void scanOnce() {
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

    @OnClick(R.id.read)
    public void readArp() {
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

    @OnClick(R.id.scan_task_start)
    public void startScanTask() {
        Intent service = new Intent(MainActivity.this, ScanService.class);
        startService(service);
    }

    @OnClick(R.id.scan_task_stop)
    public void stopScanTask() {
        Intent service = new Intent(MainActivity.this, ScanService.class);
        stopService(service);
    }
}
