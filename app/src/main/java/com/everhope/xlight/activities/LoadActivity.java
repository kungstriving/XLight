package com.everhope.xlight.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.xlight.R;
import com.everhope.xlight.XLightApplication;
import com.everhope.xlight.comm.DataAgent;
import com.everhope.xlight.comm.LogonResponseMsg;
import com.everhope.xlight.constants.Constants;
import com.everhope.xlight.constants.LogonRespStatus;
import com.everhope.xlight.helpers.AppUtils;
import com.everhope.xlight.helpers.MessageUtils;
import com.everhope.xlight.models.ClientLoginMsgResponse;
import com.everhope.xlight.models.ServiceDiscoverMsg;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;

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
        progressBar = (ProgressBar) findViewById(R.id.load_progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

//        connectAndLoginToGate();
        connectToGateAP();
//        fakeLoading();
    }

    /**
     * 直接通过AP网络连接和登录
     */
    private void connectToGateAP() {
        final DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final TextView infoView = (TextView)findViewById(R.id.load_info);
                String info = "";
                switch (msg.what) {
                    case 0:
                        //服务发现
                        dataAgent.serviceDiscover(LoadActivity.this, new ResultReceiver(new Handler()) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                                    //读到了回应消息
                                    byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                                    //解析回应消息
                                    ServiceDiscoverMsg serviceDiscoverMsg = MessageUtils.decomposeServiceDiscoverMsg(msgBytes, msgBytes.length);
                                    Log.i(TAG, "服务发现回应消息 " + serviceDiscoverMsg.toString());
                                    //登录网关
                                    logonToGate(Constants.SYSTEM_SETTINGS.GATE_AP_IP);

                                } else {
                                    //出错
                                    infoView.setText("网关未找到");
                                }
                            }
                        });

                        break;
                    case -1:
                        info = "网关连接失败";
                        infoView.setText(info);
                    default:
                        break;
                }

            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dataAgent.buildConnection(Constants.SYSTEM_SETTINGS.GATE_AP_IP, Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
                    handler.sendEmptyMessage(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
                    handler.sendEmptyMessage(-1);
                    return;
                }

            }
        }).start();
    }

    /**
     * 模拟5秒钟后进入画面
     */
    private void fakeLoading() {
        //模拟5秒钟后进入新画面
        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //删除splash view 停止loading
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                //显示actionbar
                getSupportActionBar().show();
                handler.removeCallbacks(this);

                Intent intent = new Intent(LoadActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }, 5000);
    }

    /**
     * 连接到网关并登录
     */
    private void connectAndLoginToGate() {

        Log.i(TAG, "连接到网关");

        //获取存储的sta地址
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String gateIP = pref.getString(Constants.SYSTEM_SETTINGS.GATE_STA_IP, "");
        if (StringUtils.isEmpty(gateIP)) {
            Log.w(TAG, "网关STA地址为空");
            //网关地址为空 则在当前局域网内逐个连接测试
            XLightApplication lightApp = XLightApplication.getInstance();
            DataAgent dataAgent = lightApp.getDataAgent();

            Log.i(TAG, "开始搜寻网关");
            final TextView textView = (TextView)findViewById(R.id.load_info);
            textView.setText("搜寻网关");

            dataAgent.detectGateInLan(this, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                        //获取到了网关STA地址
                        String gateStaIP = resultData.getString(Constants.KEYS_PARAMS.GATE_STA_IP);
                        Log.i(TAG, String.format("获取到了网关地址[%s]", gateStaIP));
                        textView.setText(String.format("网关地址[%s]", gateStaIP));
                        //登录到网关
//                        loginToGate(gateStaIP);

                    } else {
                        //未获取到网关STA地址
                        Toast.makeText(LoadActivity.this, "未获取到网关地址", Toast.LENGTH_SHORT).show();
                        //关闭loading 进入AP网络设置界面
                        if (progressBar.isShown()) {
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                        }
                        //连接AP网络
                        switchToAPNetwork();

                    }
                }
            });
        } else {
            //如果存在sta地址 则直连
            Toast.makeText(LoadActivity.this, "直连STA", Toast.LENGTH_LONG).show();
        }
    }
/*
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
*/
    /**
     * 登录网关
     *
     * @param gateStaIP 所有登录网关地址
     */
    private void logonToGate(final String gateStaIP) {

        Log.i(TAG, "登录到网关");
        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();

        String clientID = AppUtils.getAndroidDeviceID();

        dataAgent.loginInGate(this, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                //接收到登录回调
                if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                    //网络发送与接收正常，不代表登录成功，需要对返回消息进行解析之后进行判断
                    //解析消息
                    int bytesCount = resultData.getInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT);
                    byte[] bytesData = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);

                    ClientLoginMsgResponse logonResponseMsg = MessageUtils.decomposeLogonReturnMsg(bytesData, bytesCount);
                    short msgRandID = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                    if (logonResponseMsg.getMessageID() != msgRandID) {
                        Log.w(TAG, "消息ID不匹配");
                        return;
                    }

                    Log.i(TAG, String.format("登录网关回应消息 [%s]", logonResponseMsg.toString()));
                    //判断登录结果
                    if (logonResponseMsg.getReturnCode() == ClientLoginMsgResponse.RETURN_CODE_OK) {
                        //将sta地址加入pref
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        editor.putString(Constants.SYSTEM_SETTINGS.GATE_STA_IP, gateStaIP);
                        editor.commit();
                        //正常登录 进入主页面
                        Intent intent = new Intent(LoadActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else if (logonResponseMsg.getReturnCode() == ClientLoginMsgResponse.RETURN_CODE_USERNAME_NOEXIST) {
                        //用户名不存在
                        Toast.makeText(LoadActivity.this, "当前手机未加入到网关", Toast.LENGTH_LONG).show();
                    } else if (logonResponseMsg.getReturnCode() == ClientLoginMsgResponse.RETURN_CODE_PWD_WRONG) {
                        //密码错误
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "登录网关失败", Toast.LENGTH_LONG).show();
                }
            }
        }, clientID);

    }

    /**
     * 切换到AP网络
     * 首先还是进行连接，连通之后进行服务发现（相当于握手）
     * 打开AP设置页面
     */
    private void switchToAPNetwork() {
        Intent intent = new Intent(LoadActivity.this, APSetupActivity.class);
        startActivity(intent);
    }

}
