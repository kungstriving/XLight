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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
        setContentsStatus(false);


        //添加返回按钮
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//
//        Button retryBtn = (Button)findViewById(R.id.retry_btn);
//        retryBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                Intent intent = new Intent(APSetupActivity.this, MainActivity.class);
////                startActivity(intent);
////                finish();
//            }
//        });
        Button setSSIDBtn = (Button)findViewById(R.id.set_ssid_btn);
        setSSIDBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Toast.makeText(APSetupActivity.this, "设置网关连接网络", Toast.LENGTH_LONG).show();
                //通知网关路由器密码
                final String ssid = ((EditText)findViewById(R.id.wifi_id)).getText().toString();
                String pwd = ((EditText)findViewById(R.id.wifi_pwd)).getText().toString();
                //WPA2 PSK or WPA PSK or 无
                int securityPos = ((Spinner)findViewById(R.id.wifi_security)).getSelectedItemPosition();
                String securityType = "WPA2PSK";
                if (securityPos == 1) {
                    securityType = "WPAPSK";
                } else if (securityPos == 2) {
                    securityType = "OPEN";
                }

                DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
                dataAgent.setGateNetwork(APSetupActivity.this, ssid, securityType, pwd, new ResultReceiver(new Handler()){
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

                            //判断结果
                            if (setGateNetworkMsgResponse.getReturnCode() == SetGateNetworkMsgResponse.RETURN_CODE_OK) {

                                //设置成功 重新进入搜寻网关模式
                                //切换网络到原网络下
                                mainHandler.sendEmptyMessage(2);
                                swichConnectionToLast(ssid);
//                                //等候10秒 等待网关重置正常
//                                try {
//                                    Thread.sleep(10*1000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }

                            } else if (setGateNetworkMsgResponse.getReturnCode() == SetGateNetworkMsgResponse.RETURN_CODE_CONF_GATE_FAIL) {
                                //设置网关失败
                                Toast.makeText(APSetupActivity.this, "设置网关失败", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            //出错
                            Toast.makeText(APSetupActivity.this, "出错啦", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case 0:
                        //处理正常
                        progressBar.setVisibility(View.INVISIBLE);
                        setContentsStatus(true);
                        return;
                    case 1:
                        //连接到AP网关错误
                        progressBar.setVisibility(View.INVISIBLE);
                        setContentsStatus(true);
                        //连接出错 提示用户是否已经打开网关
                        AlertDialog.Builder builder = new AlertDialog.Builder(APSetupActivity.this);

                        builder.setMessage(R.string.if_gate_close_hint);
                        builder.setTitle("确认");
                        builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mainHandler.sendEmptyMessage(2);
                                switchConnectionToAP(APSetupActivity.this);
                                //connectToGateAP();
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
                        //重新开始阻塞等待
                        progressBar.setVisibility(View.VISIBLE);
                        setContentsStatus(false);
                        break;
                    case 3:
                        //已经连接到AP 开始连接网关
                        progressBar.setVisibility(View.INVISIBLE);
                        setContentsStatus(true);
                        //开始连接AP网关
                        connectToGateAP();
                        break;
                    case 4:
                        //已经连接到之前家庭网络
                        progressBar.setVisibility(View.INVISIBLE);
                        setContentsStatus(true);
                        //返回到load页面重新加载
                        Intent intent = new Intent(APSetupActivity.this, LoadActivity.class);
                        intent.putExtra(LoadActivity.CLEAR_RENEW, true);
                        startActivity(intent);
                        finish();
                        break;
                    case 5:
                        //连接到之前家庭网络错误
                        progressBar.setVisibility(View.INVISIBLE);
                        setContentsStatus(true);
                        builder = new AlertDialog.Builder(APSetupActivity.this);

                        builder.setMessage("家庭网络连接失败，请检查网络设置");
                        builder.setTitle("确认");
                        builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mainHandler.sendEmptyMessage(2);
                                swichConnectionToLast(lastSSID);
                            }
                        });
                        builder.create().show();
                        break;
                    default:
                        break;
                }
            }
        };

        //切换连接wifi到AP网络
        switchConnectionToAP(APSetupActivity.this);

//        Intent intent = new Intent(APSetupActivity.this, MainActivity.class);
//        startActivity(intent);
//        finish();
        return;
    }

    private void setContentsStatus(boolean status) {
        findViewById(R.id.wifi_id).setEnabled(status);
        findViewById(R.id.wifi_pwd).setEnabled(status);
        findViewById(R.id.wifi_security).setEnabled(status);
        findViewById(R.id.set_ssid_btn).setEnabled(status);
//        findViewById(R.id.retry_btn).setEnabled(status);
    }

    private void swichConnectionToLast(String ssid) {

        lastSSID = ssid;
        final APSetupActivity apSetupActivity = APSetupActivity.this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                WifiManager wifiManager = (WifiManager)apSetupActivity.getSystemService(Context.WIFI_SERVICE);

                boolean connectedReal = false;

                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for( WifiConfiguration i : list ) {
                    if(i.SSID != null && i.SSID.equals("\"" + lastSSID + "\"")) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();

                        break;
                    }
                }

                //等待监测是否成功
                int checkCount = 0;

                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                while (true) {
                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (mWifi.isConnected()) {
                        //已连接
                        if (wifiManager.getConnectionInfo().getSSID().equals(lastSSID)) {
                            connectedReal = true;
                            break;
                        }
                    }

                    if (checkCount >= 20) {
                        //连接尝试次数超限
                        break;
                    }
                    checkCount++;
                    //wifiManager.reconnect();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (connectedReal) {
                    mainHandler.sendEmptyMessage(4);
                }

                if (!connectedReal) {
                    mainHandler.sendEmptyMessage(5);
                }
            }
        }).start();
    }

    private void switchConnectionToAP(final Context context) {
        //检查wifi是否开启
        if (!AppUtils.checkWifiIfOpen(APSetupActivity.this)) {
            //如果未连接 则提示用户连接wifi
            Toast.makeText(APSetupActivity.this, "请开启WIFI连接", Toast.LENGTH_LONG).show();
            mainHandler.sendEmptyMessage(0);
            return;
        }
        final APSetupActivity apSetupActivity = APSetupActivity.this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

                boolean connectedReal = false;
                for(int i = 0; i < Constants.SYSTEM_SETTINGS.GATE_AP_SSID_ARR.length; i++) {
                    String elighteSSID = Constants.SYSTEM_SETTINGS.GATE_AP_SSID_ARR[i];
                    String pwd = "";

                    WifiConfiguration wifiConfiguration = new WifiConfiguration();
                    wifiConfiguration.SSID = "\"" + elighteSSID + "\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            wifiConfiguration.preSharedKey = "\"" + pwd + "\"";

                    int addReturn = wifiManager.addNetwork(wifiConfiguration);
                    if (addReturn == -1) {
                        continue;
                    }
                    apSetupActivity.lastSSID = wifiManager.getConnectionInfo().getSSID();        //保存该id 后续恢复连接

                    wifiManager.disconnect();
                    wifiManager.enableNetwork(addReturn, true);
                    wifiManager.reconnect();

                    //等待监测是否成功
                    int checkCount = 0;

                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    while (true) {
                        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (mWifi.isConnected()) {
                            //已连接
                            if (wifiManager.getConnectionInfo().getSSID().equals(elighteSSID)) {
                                connectedReal = true;
                                break;
                            }
                        }

                        if (checkCount >= 20) {
                            //连接尝试次数超限
                            break;
                        }
                        checkCount++;
                        //wifiManager.reconnect();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (connectedReal) {
                        mainHandler.sendEmptyMessage(3);
                        break;
                    }
                }

                if (connectedReal == false) {
                    mainHandler.sendEmptyMessage(1);
                }
            }
        }).start();


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
                                    short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                                    //解析回应消息
                                    ServiceDiscoverMsg serviceDiscoverMsg = null;
                                    try {
                                        serviceDiscoverMsg = MessageUtils.decomposeServiceDiscoverMsg(msgBytes, msgBytes.length, idShould);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.w(TAG, String.format("消息解析出错 [%s]", e.getMessage()));
                                        return;
                                    }
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
                    short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);

                    ClientLoginMsgResponse logonResponseMsg = null;
                    try {
                        logonResponseMsg = MessageUtils.decomposeLogonReturnMsg(bytesData, bytesCount, idShould);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w(TAG, String.format("消息解析出错 [%s]", e.getMessage()));
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
    public void onBackPressed() {
        finish();
        System.exit(0);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
