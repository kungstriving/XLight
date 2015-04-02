package com.everhope.elighte.helpers;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.everhope.elighte.R;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.models.SubGroup;
import com.everhope.elighte.models.Scene;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 系统工具类
 * Created by kongxiaoyang on 2015/1/10.
 */
public class AppUtils {

    private static final String TAG = "AppUtils@Light";

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private static final Map<Integer, String> errorMap = new HashMap<>();

    static {
        errorMap.put(12293, "该灯当前不可用");
    }

    public static String getMACString(byte[] mac) {
        char[] macChar = Hex.encodeHex(mac);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < macChar.length; i++) {
            sb.append(macChar[i]);
            if ((i % 2 != 0) && (i != (macChar.length -1))) {
                sb.append(":");
            }
        }

        return sb.toString();
    }

    /**
     * 检测当前wifi是否开启
     * 注意 不是wifi是否连接
     * @param context
     * @return
     */
    public static boolean checkWifiIfOpen(Context context) {

        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isAvailable();
    }

    public static void initDB () {
        //新建四个场景
        Scene sunsetScene = new Scene();
        sunsetScene.name = "日落";
        sunsetScene.imgName = "sunset";
        sunsetScene.brightness = 0;
        sunsetScene.status = 0;
        sunsetScene.save();

        Scene seaScene = new Scene();
        seaScene.name = "海边";
        seaScene.imgName = "sea";
        seaScene.brightness = 0;
        seaScene.status = 0;
        seaScene.save();

        Scene forestScene = new Scene();
        forestScene.name = "森林";
        forestScene.imgName = "forest";
        forestScene.brightness = 0;
        forestScene.status = 0;
        forestScene.save();

        Scene rainbowScene = new Scene();
        rainbowScene.name = "彩虹";
        rainbowScene.imgName = "rainbow";
        rainbowScene.brightness = 0;
        rainbowScene.status = 0;
        rainbowScene.save();

        //新建分组
        SubGroup ungroup = new SubGroup();
        ungroup.name = "所有灯";
        ungroup.save();

    }

    public static String getErrorInfo(String key) {
        return errorMap.get(Integer.parseInt(key));
    }

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {

            for (;;) {
                final int result = sNextGeneratedId.get();
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }
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

    public static String getSubnetBroadcaseAddr(Context context) {
        WifiManager wifii = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wifii.getDhcpInfo();

        String s_ipAddress = intToIp(d.ipAddress);
        String s_netmask = intToIp(d.netmask);

        SubnetUtils subnetUtils = new SubnetUtils(s_ipAddress, s_netmask);
        return subnetUtils.getInfo().getBroadcastAddress();
    }

    private static String intToIp(int addr) {
        return ((addr & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF));
    }

    public static int hsbColorValueToRGB(byte[] hsb) {
        byte h = hsb[0];
        byte s = hsb[1];
        byte b = hsb[2];
        float hFloat = (float)(((int)h)-0.5)/254f;
        float sFloat = (float)(((int)s)-0.5)/254f;
        float bFloat = (float)(((int)b)-0.5)/254f;
        return HSBtoRGB(hFloat, sFloat, bFloat);
    }

    /**
     * HSB is exactly the same as HSV
     * 将颜色值转换为协议中相关的值范围
     * @param colorValue
     * @return
     */
    public static byte[] rgbColorValueToHSB(int colorValue) {
        byte[] hsb = new byte[3];
        float[] tempHSB = new float[3];
        RGBtoHSB(Color.red(colorValue), Color.green(colorValue), Color.blue(colorValue), tempHSB);
//        RGBtoHSB(255,0,0,tempHSB);
        int hValue = (int)Math.floor(tempHSB[0] * 254 + 0.5);
        hsb[0] = (byte)hValue;
        int sValue = (int)Math.floor(tempHSB[1] * 254 + 0.5);
        hsb[1] = (byte)sValue;
        int bValue = (int)Math.floor(tempHSB[2] * 254 + 0.5);
        hsb[2] = (byte)bValue;

        return hsb;
    }

    /**
     * copy from java.awt.Color.java
     * @param hue
     * @param saturation
     * @param brightness
     * @return
     */
    private static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
    }

    /**
     * copy from java.awt.Color.java
     *
     * @param r
     * @param g
     * @param b
     * @param hsbvals [0]=h [1]=s [2]=b
     * @return
     */
    private static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        float hue, saturation, brightness;
        if (hsbvals == null) {
            hsbvals = new float[3];
        }
        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        int cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        brightness = ((float) cmax) / 255.0f;
        if (cmax != 0)
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }
}
