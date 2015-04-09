package com.shang1jk.in;

import java.nio.ByteOrder;

/**
 * Created by Administrator on 2015/4/9.
 */
public class EndPoint {
    private String ip, mac;

    public EndPoint(String ip, String mac) {
        this.ip = ip;
        this.mac = mac;
    }

    public String getIpString() {
        return null;
    }

    public byte[] getIpByteArray() {
        return convertIp2ByteArray(ip);
    }

    public static byte[] convertIp2ByteArray(int ipInt) {
        byte[] ipByteArray = new byte[4];
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            ipByteArray[0] = (byte) (ipInt & 0xFF);
            ipByteArray[1] = (byte) (ipInt >> 8 & 0xFF);
            ipByteArray[2] = (byte) (ipInt >> 16 & 0xFF);
            ipByteArray[3] = (byte) (ipInt >> 24 & 0xFF);
        } else {
            ipByteArray[0] = (byte) (ipInt >> 24 & 0xFF);
            ipByteArray[1] = (byte) (ipInt >> 16 & 0xFF);
            ipByteArray[2] = (byte) (ipInt >> 8 & 0xFF);
            ipByteArray[3] = (byte) (ipInt & 0xFF);
        }
        return ipByteArray;
    }

    public static byte[] convertIp2ByteArray(String ipString) {
        byte[] ipByteArray = new byte[4];
        String[] split = ipString.split(":");
        for (int i = 0; i < split.length; i++) {
            Integer integer = Integer.valueOf(split[i]);
            ipByteArray[i] = (byte) (integer & 0xFF);
        }
        return ipByteArray;
    }

    public static String convertIp2String(int ipInt) {
        byte[] ipByteArray = convertIp2ByteArray(ipInt);
        StringBuffer sb = new StringBuffer();
        for (byte b : ipByteArray) {
            int i = b;
            i &= 0xFF;
            sb.append(String.valueOf(i));
            sb.append(":");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static String convertIp2String(byte[] ipByteArray) {
        return null;
    }

    public static String convertMac2String(byte[] macByteArray) {
        StringBuffer sb = new StringBuffer();
        for (byte b : macByteArray) {
            int i = b;
            i &= 0xFF;  //保留后八bit

            String s = Integer.toHexString(i);
            s = s.length() == 1 ? "0" + s : s;

            sb.append(s);
            sb.append(":");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString().toUpperCase();
    }
}
