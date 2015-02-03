package com.everhope.xlight.helpers;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.everhope.xlight.XLightApplication;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;

/**
 * 系统工具类
 * Created by kongxiaoyang on 2015/1/10.
 */
public class AppUtils {

    /**
     * 获取系统设备ID，返回唯一标示
     * 目前获取MAC地址
     * @return
     */
    public static String getAndroidDeviceID() {
        WifiManager wifiMan = (WifiManager) XLightApplication.getInstance().getSystemService(
                Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        String mac = wifiInf.getMacAddress();
        return StringUtils.remove(mac, ':');
    }

    /**
     * 获取当前子网所有IP地址
     * @param context
     * @return
     */
    public static String[] getSubnetAddresses(Context context) {

        WifiManager wifii = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wifii.getDhcpInfo();

//        String s_gateway = intToIp(d.gateway);
        String s_ipAddress = intToIp(d.ipAddress);
        String s_netmask = intToIp(d.netmask);
//        String s_serverAddress = intToIp(d.serverAddress);

        SubnetUtils subnetUtils = new SubnetUtils(s_ipAddress, s_netmask);
        return subnetUtils.getInfo().getAllAddresses();
    }

    private static String intToIp(int addr) {
        return ((addr & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF));
    }
}
