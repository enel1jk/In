package com.shang1jk.in;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Administrator on 2015/4/10.
 */
public class ScanService extends Service {
    private final long SCAN_PERIOD = 2 * 60 * 1000; //扫描间隔时间
    private final int IP_COUNT = 256 - 2 - 2;   //256 - x.x.x.0 - x.x.x.255 - local - gateway
    private final String ARP_PATH = "/proc/net/arp";
    private EndPoint local, gateway;
    private boolean isScanning = false;
    private Timer timer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(getClass().getSimpleName(), "service onCreate");
        local = NetUtil.getLocal(this);
        gateway = NetUtil.getGateway(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("xxx", "onStartCommand");
        timer = new Timer("scan_timer", true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                UdpProber udpProber = new UdpProber(ScanService.this);
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
        };
        if (!isScanning) {
            timer.schedule(task, 0, SCAN_PERIOD);
            isScanning = true;
        }

        return super.onStartCommand(intent, flags, startId);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Log.e("xxx", "ScanService onDestroy");
    }
}
