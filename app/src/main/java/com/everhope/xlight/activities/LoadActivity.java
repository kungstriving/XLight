package com.everhope.xlight.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.everhope.xlight.R;
import com.everhope.xlight.XLightApplication;
import com.everhope.xlight.helpers.AppUtils;
import com.everhope.xlight.comm.DataAgent;
import com.everhope.xlight.comm.LogonResponseMsg;
import com.everhope.xlight.helpers.MessageUtils;
import com.everhope.xlight.constants.Constants;
import com.everhope.xlight.constants.LogonRespStatus;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * loading页面 该页面为系统启动第一个画面
 * 1.负责显示loading动画
 * 2.连接网关
 * 3.连接网关成功跳转到MainActivity
 * 4.失败则跳转到设置页面
 */
public class LoadActivity extends ActionBarActivity {

    private static final String TAG = "LoadActivity@Light";
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_load);

        getSupportActionBar().hide();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        connectToGate();
        //模拟5秒钟后进入新画面
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                //删除splash view 停止loading
//                progressBar.setVisibility(ProgressBar.INVISIBLE);
//                //显示actionbar
//                getSupportActionBar().show();
//                handler.removeCallbacks(this);
//
//                Intent intent = new Intent(LoadActivity.this, MainActivity.class);
//                startActivity(intent);
//            }
//        }, 5000);
    }

    private void testSubnet() {
        WifiManager wifii = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wifii.getDhcpInfo();

//        String s_dns1="DNS 1: "+String.valueOf(d.dns1);
//        String s_dns2="DNS 2: "+String.valueOf(d.dns2);
        String s_gateway = intToIp(d.gateway);
        String s_ipAddress = intToIp(d.ipAddress);
//        String s_leaseDuration="Lease Time: "+String.valueOf(d.leaseDuration);
        String s_netmask = intToIp(d.netmask);
        String s_serverAddress = intToIp(d.serverAddress);

        //dispaly them
        Log.i(TAG,
                "Network Info\n" + "getway" + s_gateway + "\n" + "netmask" + s_netmask + "\n"
                        + "server " + s_serverAddress + "\n" + "ip " + s_ipAddress);

//        SubnetUtils subnetUtils = new SubnetUtils(s_gateway, s_netmask);
        SubnetUtils subnetUtils = new SubnetUtils(s_ipAddress, s_netmask);
        String[] allips = subnetUtils.getInfo().getAllAddresses();
        for (int i = 0; i < allips.length; i++) {
            Log.i(TAG, String.format("Net ip got %s", allips[i]));
        }
    }

    /**
     * 连接到网关
     */
    private void connectToGate() {

        Log.i(TAG, "连接到网关");

        //使用STA地址连接
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String gateIP = pref.getString(Constants.SYSTEM_SETTINGS.GATE_STA_IP, "");
        if (StringUtils.isEmpty(gateIP)) {
            Log.w(TAG, "网关STA地址为空");
            //网关地址为空 则局域网内广播测试连接消息
            XLightApplication lightApp = XLightApplication.getInstance();

            DataAgent dataAgent = lightApp.getDataAgent();

            Log.i(TAG, "开始搜寻网关");
            Toast.makeText(LoadActivity.this, "开始搜寻网关", Toast.LENGTH_SHORT).show();
            dataAgent.detectGateInLan(this, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                        //获取到了网关STA地址
//                        String gateStaIP = resultData.getString(Constants.KEYS_PARAMS.GATE_STA_IP);
                        String gateStaIP = resultData.getString(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                        Log.i(TAG, "获取到了网关地址");
                        //Log.i(TAG, gateStaIP);
                        //Toast.makeText(LoadActivity.this, gateStaIP, Toast.LENGTH_LONG).show();
                        //测试直接使用AP网络
                        String gateAPIP = Constants.SYSTEM_SETTINGS.GATE_AP_IP;

                        //服务发现
                        testConnection();
//                        logonToGate(gateAPIP);

                    } else {
                        //未获取到网关STA地址
                        //关闭loading 进入AP网络设置界面
                        if (progressBar.isShown()) {
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                        }
                        //连接AP网络
                        switchToAPNetwork();
                        Intent intent = new Intent(LoadActivity.this, APSetupActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
        //  判断STA地址是否存在

        //  STA地址不存在 全网广播 8899端口

        //      有回应，则使用回应地址，更新STA地址，然后再使用STA地址连接

        //      无回应，则连接到网关AP网络（10.10.100.254）
        //

        //  STA地址存在，直接建立socket
    }

    private void testConnection() {
        final DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();

        dataAgent.serviceDiscover(LoadActivity.this, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                byte[] databytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                Toast.makeText(LoadActivity.this, "resultCode = " + resultCode + " bytes = " + Arrays.toString(databytes), Toast.LENGTH_LONG).show();
            }
        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    dataAgent.buildConnection("10.10.100.254", Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                }
//                dataAgent.serviceDiscover(LoadActivity.this, new ResultReceiver(new Handler()) {
//                    @Override
//                    protected void onReceiveResult(int resultCode, Bundle resultData) {
//                        byte[] databytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
//                        Toast.makeText(LoadActivity.this, "resultCode = " + resultCode + " bytes = " + Arrays.toString(databytes), Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//        }).start();

//            dataAgent.buildConnection(Constants.SYSTEM_SETTINGS.GATE_AP_IP, Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    dataAgent.buildConnection("10.10.100.254", Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                }
//                dataAgent.serviceDiscover(LoadActivity.this, new ResultReceiver(new Handler()) {
//                    @Override
//                    protected void onReceiveResult(int resultCode, Bundle resultData) {
//                        byte[] databytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
//                        Toast.makeText(LoadActivity.this, "resultCode = " + resultCode + " bytes = " + Arrays.toString(databytes), Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//        }, 1000);


    }

    /**
     * 根据sta地址登录网关
     *
     * @param gateStaIP
     */
    private void logonToGate(String gateStaIP) {

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        //建立连接
        try {
            dataAgent.buildConnection(gateStaIP, Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, e.getMessage());
            //显示错误消息
            Toast.makeText(getApplicationContext(), "建立连接失败",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String clientID = AppUtils.getAndroidDeviceID();

        dataAgent.logonToGate(clientID, getApplicationContext(), new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                //接收到登录回调
                if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                    //解析消息
                    int bytesCount = resultData.getInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT);
                    byte[] bytesData = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);

                    LogonResponseMsg logonResponseMsg = MessageUtils.decomposeLogonReturnMsg(bytesData, bytesCount);
                    //判断登录结果
                    if (logonResponseMsg.getLogonRespStatus() == LogonRespStatus.OK) {
                        //正常登录 进入主页面
                        Intent intent = new Intent(LoadActivity.this, APSetupActivity.class);
                        startActivity(intent);
                    } else if (logonResponseMsg.getLogonRespStatus() == LogonRespStatus.NOEXIST) {
                        //网关不存在该设备id 则进入添加手机画面
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "登录网关失败", Toast.LENGTH_LONG).show();
                }
            }
        });
        //将sta地址加入pref
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putString(Constants.SYSTEM_SETTINGS.GATE_STA_IP, gateStaIP);
        editor.commit();
    }

    private void switchToAPNetwork() {

    }

    public String intToIp(int addr) {
        return ((addr & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF));
    }
}
