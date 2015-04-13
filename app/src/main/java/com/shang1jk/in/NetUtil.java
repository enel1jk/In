package com.shang1jk.in;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2015/4/10.
 */
public class NetUtil {
    /**
     * @return 本地ip、mac
     */
    public static EndPoint getLocal(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
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

        return new EndPoint(localIp, localMac);
    }

    /**
     * @return 网关ip、mac
     */
    public static EndPoint getGateway(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        int gateway = dhcpInfo.gateway;
        //ip
        String gatewayIp = EndPoint.convertIp2String(gateway);
        //mac
        String gatewayMac = connectionInfo.getBSSID();

        return new EndPoint(gatewayIp, gatewayMac);
    }



}
