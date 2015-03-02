package com.everhope.elighte.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.constants.ConnectGateStatus;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.ClientLoginMsgResponse;
import com.everhope.elighte.models.ServiceDiscoverMsg;
import com.everhope.elighte.models.SetGateNetworkMsgResponse;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * AP网络模式下设置STA连接网关信息
 */
public class APSetupActivity extends ActionBarActivity {

    private static final String TAG = "APSetupActivity@Light";
    private Handler mainHandler;

    private String lastSSID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apsetup);
        //打开ProgressBar
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.load_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        Button setSSIDBtn = (Button)findViewById(R.id.set_ssid_btn);
        setSSIDBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(APSetupActivity.this, "设置网关连接网络", Toast.LENGTH_LONG).show();
                //通知网关路由器密码
                String ssid = ((EditText)findViewById(R.id.wifi_id)).getText().toString();
                String pwd = ((EditText)findViewById(R.id.wifi_pwd)).getText().toString();

                DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
                dataAgent.setGateNetwork(APSetupActivity.this, new ResultReceiver(new Handler()){
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                            //读到了回应消息
                            byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                            //解析回应消息
                            SetGateNetworkMsgResponse setGateNetworkMsgResponse = MessageUtils.decomposeSetGateReturnMsg(msgBytes, msgBytes.length);
                            Log.i(TAG, "设置网关连接信息回应消息 " + setGateNetworkMsgResponse.toString());

                            //获取发送的ID
                            short msgRandID = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                            if (setGateNetworkMsgResponse.getMessageID() != msgRandID) {
                                Log.w(TAG, "消息ID不匹配");
                                return;
                            }

                            //判断登录结果
                            if (setGateNetworkMsgResponse.getReturnCode() == SetGateNetworkMsgResponse.RETURN_CODE_OK) {

                                //设置成功 重新进入搜寻网关模式
                                //TODO 这里需要进行重新搜寻 目前直接进入主页面
                                //切换网络到原网络下 目前不用切换到原网络，直接使用AP网络进行使用
//                                swichConnectionToLast();
                                Intent intent = new Intent(APSetupActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            } else if (setGateNetworkMsgResponse.getReturnCode() == SetGateNetworkMsgResponse.RETURN_CODE_CONF_GATE_FAIL) {
                                //设置网关失败
                                Toast.makeText(APSetupActivity.this, "设置网关失败", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            //出错
                            Toast.makeText(APSetupActivity.this, "出错啦", Toast.LENGTH_LONG).show();
                        }
                    }
                },ssid, pwd);
            }
        });
        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case 0:
                        //处理正常
                        progressBar.setVisibility(View.INVISIBLE);
                        return;
                    case 1:
                        //连接到AP网关错误
                        progressBar.setVisibility(View.INVISIBLE);
                        //连接出错 提示用户是否已经打开网关
                        AlertDialog.Builder builder = new AlertDialog.Builder(APSetupActivity.this);

                        builder.setMessage(R.string.if_gate_close_hint);
                        builder.setTitle("确认");
                        builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mainHandler.sendEmptyMessage(2);
                                switchConnectionToAP(APSetupActivity.this);
                                connectToGateAP();
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                XLightApplication.connectGateStatus = ConnectGateStatus.DISCONNECTED;
//                                //TODO 进入错误处理页面 或者 错误消息指导页面
//                                Intent intent = new Intent(APSetupActivity.this, MainActivity.class);
//                                startActivity(intent);
//                                finish();
                            }
                        });
                        builder.create().show();
                        return;
                    case 2:
                        //重新开始连接到网关
                        progressBar.setVisibility(View.VISIBLE);
                    default:
                        break;
                }
            }
        };

        //切换连接wifi到AP网络
        switchConnectionToAP(APSetupActivity.this);
        //开始连接AP网关
        connectToGateAP();
    }

    private void swichConnectionToLast() {
        WifiManager wifiManager = (WifiManager)APSetupActivity.this.getSystemService(Context.WIFI_SERVICE);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + this.lastSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }

        while (true) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.w(TAG, e.getMessage());
            }
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void switchConnectionToAP(Context context) {
        String elighteSSID = Constants.SYSTEM_SETTINGS.GATE_AP_SSID;
        String pwd = "";

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + elighteSSID + "\"";
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfiguration.preSharedKey = "\"" + pwd + "\"";

        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(wifiConfiguration);
        this.lastSSID = wifiManager.getConnectionInfo().getSSID();        //保存该id 后续恢复连接

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + elighteSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }

        //等待监测是否成功
        while (true) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.w(TAG, e.getMessage());
            }
        }
    }

    /**
     * 直接通过AP网络连接和登录
     */
    private void connectToGateAP() {
        final DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();

        final Handler connHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final TextView infoView = (TextView)findViewById(R.id.load_info);
                String info = "";
                switch (msg.what) {
                    case 0:
                        //服务发现
                        dataAgent.serviceDiscover(APSetupActivity.this, new ResultReceiver(new Handler()) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                                    //读到了回应消息
                                    byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                                    //解析回应消息
                                    ServiceDiscoverMsg serviceDiscoverMsg = MessageUtils.decomposeServiceDiscoverMsg(msgBytes, msgBytes.length);
                                    Log.i(TAG, "服务发现回应消息 " + serviceDiscoverMsg.toString());
                                    logonToGate();
                                } else {
                                    //出错
                                    Toast.makeText(APSetupActivity.this, "出错啦", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        break;
                    default:
                        break;
                }

            }
        };

        //新建线程连接到AP网关
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //停止10秒 等待连接到AP网络
//                    Thread.sleep(10000);
                    dataAgent.buildConnection(Constants.SYSTEM_SETTINGS.GATE_AP_IP, Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
                    connHandler.sendEmptyMessage(0);

                } catch (Exception e) {
                    //1=连接到AP网络出错
                    mainHandler.sendEmptyMessage(1);

                    return;
                }

            }
        }).start();


    }

    /**
     * 登录AP网关
     *
     */
    private void logonToGate() {

        Log.i(TAG, "登录到AP网关");
        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();

        String clientID = AppUtils.getAndroidDeviceID();

        dataAgent.loginInGate(APSetupActivity.this, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                //接收到登录回调
                if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                    //网络发送与接收正常，不代表登录成功，需要对返回消息进行解析之后进行判断
                    //解析消息
                    int bytesCount = resultData.getInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT);
                    byte[] bytesData = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);

                    ClientLoginMsgResponse logonResponseMsg = MessageUtils.decomposeLogonReturnMsg(bytesData, bytesCount);
                    //获取发送的ID
                    short msgRandID = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                    if (logonResponseMsg.getMessageID() != msgRandID) {
                        Log.w(TAG, "消息ID不匹配");
                        return;
                    }

                    Log.i(TAG, String.format("登录网关回应消息 [%s]", logonResponseMsg.toString()));
                    //判断登录结果
                    if (logonResponseMsg.getReturnCode() == ClientLoginMsgResponse.RETURN_CODE_OK) {

                        //设置网络通信特征码
                        short newMsgSign = logonResponseMsg.getSign();
                        MessageUtils.messageSign = newMsgSign;
                        //已登录网关 提示用户输入家庭网关信息
                        mainHandler.sendEmptyMessage(0);
                        Toast.makeText(APSetupActivity.this, "请输入家庭网关连接信息", Toast.LENGTH_LONG).show();

                    } else if (logonResponseMsg.getReturnCode() == ClientLoginMsgResponse.RETURN_CODE_USERNAME_NOEXIST) {
                        //用户名不存在 即当前MAC地址没有加入网关白名单
                        Toast.makeText(APSetupActivity.this, "当前手机未加入到网关", Toast.LENGTH_LONG).show();
                        addToGateWhiteList();
                    } else if (logonResponseMsg.getReturnCode() == ClientLoginMsgResponse.RETURN_CODE_PWD_WRONG) {
                        //密码错误 目前不存在密码错误的情况
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "登录网关失败", Toast.LENGTH_LONG).show();
                }
            }
        }, clientID);

    }

    private void addToGateWhiteList() {
        //显示进度条倒数对话框
        //不断登录
        //登录成功，进入MainActivity
        //登录不成功，不能进入MainActivity，提示再次进行按压操作
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_apsetup, menu);
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
