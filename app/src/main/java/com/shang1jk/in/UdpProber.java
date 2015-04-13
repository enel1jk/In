package com.shang1jk.in;

import android.content.Context;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Administrator on 2015/4/10.
 */
public class UdpProber extends Thread {
    int xxx = 1;
    private static final int PROBER_THREAD_POOL_SIZE = 25;

    private class SingleProber extends Thread {
        private InetAddress mAddress = null;

        public SingleProber(InetAddress address) {
            mAddress = address;
        }

        @Override
        public void run() {
            xxx ++;
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

    private static final short NETBIOS_UDP_PORT = 137;
    // NBT UDP PACKET: QUERY; REQUEST; UNICAST
    private static final byte[] NETBIOS_REQUEST = {
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
    private ThreadPoolExecutor mExecutor = null;
    private EndPoint local, gateway;

    public UdpProber(Context context) {
        mExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(PROBER_THREAD_POOL_SIZE);
        local = NetUtil.getLocal(context);
        gateway = NetUtil.getGateway(context);
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
