package com.everhope.xlight;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.everhope.xlight.app.AppUtils;
import com.everhope.xlight.comm.DataAgent;
import com.everhope.xlight.comm.LogonResponseMsg;
import com.everhope.xlight.comm.MessageUtils;
import com.everhope.xlight.constants.Constants;
import com.everhope.xlight.constants.LogonRespStatus;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * 第一个页面，负责显示splash画面和连接网关
 */
public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        //检查是否第一次运行
        if (isVeryFirstLoad()) {
            //是第一次运行
            setVeryFirstLoad();

        }

        //连接到网关
        connectToGate();

        getWindow().setBackgroundDrawableResource(R.drawable.splash);
        setContentView(R.layout.activity_main);

    }

    /**
     * 连接到网关
     */
    private void connectToGate() {

        Log.i(TAG, "连接到网关");

        //使用STA地址连接
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String gateIP = pref.getString(Constants.SYSTEM_SETTINGS.GATE_STA_IP, "");
        if (StringUtils.isEmpty(gateIP)) {
            Log.w(TAG, "网关STA地址为空");
            //网关地址为空 则局域网内广播测试连接消息
            XLightApplication lightApp = XLightApplication.getInstance();

            DataAgent dataAgent = lightApp.getDataAgent();

            dataAgent.detectGateInLan(this, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                        //获取到了网关STA地址
                        String gateStaIP = resultData.getString(Constants.KEYS_PARAMS.GATE_STA_IP);

                        //使用sta地址直连网关，即直接登录网关
                        logonToGate(gateStaIP);

                    } else {
                        //连接AP网络
                        switchToAPNetwork();
                        //关闭loading 进入AP网络设置界面
                        if (progressBar.isShown()) {
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                        }

                        Intent intent = new Intent(MainActivity.this, APSetupActivity.class);
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

    /**
     * 根据sta地址登录网关
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
                        Intent intent = new Intent(MainActivity.this, APSetupActivity.class);
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
    /**
     * 设置启动过
     */
    private void setVeryFirstLoad() {
        Log.i(TAG, "第一次启动，设置标志位");

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.SYSTEM_SETTINGS.IS_LOADED_YET, true);
        editor.commit();
    }
    /**
     * 检查是否第一次运行。
     * 即在本机上没有启动过该APP
     * @return true 已经启动过；false 还没有启动过
     */
    private boolean isVeryFirstLoad() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return pref.getBoolean(Constants.SYSTEM_SETTINGS.IS_LOADED_YET, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //显示loading
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        //开启后台线程

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
