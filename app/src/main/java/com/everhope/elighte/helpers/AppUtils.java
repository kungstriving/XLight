package com.everhope.elighte.helpers;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.view.View;

import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.models.SubGroup;
import com.everhope.elighte.models.Scene;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 系统工具类
 * Created by kongxiaoyang on 2015/1/10.
 */
public class AppUtils {

    private static final String TAG = "AppUtils@Light";

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

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

//        //新建三个灯
//        Light light1 = new Light();
//        light1.lightID = "0001";
//        light1.name="0001";
//        light1.save();
//
//        Light light2 = new Light();
//        light2.lightID = "0002";
//        light2.name="0002";
//        light2.save();
//
//        Light light3 = new Light();
//        light3.lightID = "0003";
//        light3.name="0003";
//        light3.save();
//
//        //每个场景包含三个灯
//        LightScene lightScene = new LightScene();
//        lightScene.light = light1;
//        lightScene.scene = sunsetScene;
//        lightScene.save();
//
//        LightScene lightScene1 = new LightScene();
//        lightScene1.light = light2;
//        lightScene1.scene = sunsetScene;
//        lightScene1.save();
//
//        LightScene lightScene2 = new LightScene();
//        lightScene2.light = light3;
//        lightScene2.scene = sunsetScene;
//        lightScene2.save();
//
//        //
//        lightScene = new LightScene();
//        lightScene.light = light1;
//        lightScene.scene = seaScene;
//        lightScene.save();
//
//        lightScene1 = new LightScene();
//        lightScene1.light = light2;
//        lightScene1.scene = seaScene;
//        lightScene1.save();
//
//        lightScene2 = new LightScene();
//        lightScene2.light = light3;
//        lightScene2.scene = seaScene;
//        lightScene2.save();
//
//        //
//        lightScene = new LightScene();
//        lightScene.light = light1;
//        lightScene.scene = forestScene;
//        lightScene.save();
//
//        lightScene1 = new LightScene();
//        lightScene1.light = light2;
//        lightScene1.scene = forestScene;
//        lightScene1.save();
//
//        lightScene2 = new LightScene();
//        lightScene2.light = light3;
//        lightScene2.scene = forestScene;
//        lightScene2.save();
//
//        //
//        lightScene = new LightScene();
//        lightScene.light = light1;
//        lightScene.scene = rainbowScene;
//        lightScene.save();
//
//        lightScene1 = new LightScene();
//        lightScene1.light = light2;
//        lightScene1.scene = rainbowScene;
//        lightScene1.save();
//
//        lightScene2 = new LightScene();
//        lightScene2.light = light3;
//        lightScene2.scene = rainbowScene;
//        lightScene2.save();
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
}
