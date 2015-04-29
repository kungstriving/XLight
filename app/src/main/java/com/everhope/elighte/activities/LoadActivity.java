package com.everhope.elighte.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

import com.everhope.elighte.R;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.ClientLoginMsgResponse;
import com.everhope.elighte.models.ServiceDiscoverMsg;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

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

    /**
     * sta 连接失败 handler标志
     */
    private static final int HANDLER_STA_CONNECT_FAIL = 1;

    /**
     * Intent 参数 重新来过 需要清空缓存的sta地址
     */
    public static final String CLEAR_RENEW = "clear_renew";

    private ProgressBar progressBar;
    private boolean debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        if (intent.getBooleanExtra(CLEAR_RENEW, false)) {
            //重置
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.SYSTEM_SETTINGS.GATE_STA_IP, "");
            editor.commit();
        }

        setContentView(R.layout.activity_load);

        getSupportActionBar().hide();
        progressBar = (ProgressBar) findViewById(R.id.load_progressBar);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        connectAndLoginToGate();
//        connectToGateAP();
//        fakeLoading();
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        this.debug = sharedPreferences.getBoolean(Constants.SYSTEM_SETTINGS.DEBUG, false);
    }

    /**
     * 使用sta地址连接
     */
    private void connectToGate(final String gateIP, final int port) {
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
                                    //显示回应消息dialog
                                    if (debug) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoadActivity.this);

//                                        bundle.putBoolean("goon", false);
                                        builder.setMessage(Arrays.toString(msgBytes));
                                        builder.setTitle("服务发现返回消息");
                                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                logonToGate(Constants.SYSTEM_SETTINGS.GATE_AP_IP);
                                            }
                                        });
                                        builder.create().show();
                                    } else {
                                        //登录网关
                                        logonToGate(gateIP);
                                    }

                                } else {
                                    //出错
                                    infoView.setText("网关未找到");
                                    //重试 或者 重新配置网关信息 或者退出系统
                                    Dialog dialog = new AlertDialog.Builder(LoadActivity.this)
                                            .setTitle("未发现网关")
                                            .setMessage("在当前网络中未找到网关")
                                            .setPositiveButton("重新设置",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            Intent intent = new Intent(LoadActivity.this, APSetupActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    })
                                            .setNegativeButton("重试一次",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            progressBar.setVisibility(ProgressBar.VISIBLE);
                                                            connectAndLoginToGate();
                                                        }
                                    }).create();

                                    dialog.show();
                                }
                            }
                        });
                        break;
                    case HANDLER_STA_CONNECT_FAIL:
                        //广播重试
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        editor.putString(Constants.SYSTEM_SETTINGS.GATE_STA_IP, "");
                        editor.commit();
                        connectAndLoginToGate();
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
                    dataAgent.buildConnection(gateIP, port);
//                    dataAgent.buildConnection(Constants.SYSTEM_SETTINGS.GATE_AP_IP, Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
                    handler.sendEmptyMessage(0);

                } catch (IOException e) {
                    //建立连接sta失败 则广播搜索
                    Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
                    handler.sendEmptyMessage(HANDLER_STA_CONNECT_FAIL);
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

        Log.i(TAG, "开始连接到网关");

        final TextView textView = (TextView)findViewById(R.id.load_info);
        //获取存储的sta地址
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String gateIP = pref.getString(Constants.SYSTEM_SETTINGS.GATE_STA_IP, "");
        if (StringUtils.isEmpty(gateIP)) {
            Log.w(TAG, "网关STA地址为空");
            //网关地址为空 则在当前局域网内逐个连接测试
            XLightApplication lightApp = XLightApplication.getInstance();
            DataAgent dataAgent = lightApp.getDataAgent();

            Log.i(TAG, "开始UDP广播搜寻网关");

            textView.setText("搜寻网关");

            dataAgent.detectGateInLan(this, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                        //获取到了网关STA地址
                        String gateStaIP = resultData.getString(Constants.KEYS_PARAMS.GATE_STA_IP);
                        Log.i(TAG, String.format("获取到了网关地址[%s]", gateStaIP));
//                        textView.setText(String.format("网关地址[%s]", gateStaIP));
                        //登录到网关
                        connectToGate(gateStaIP, Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);

                    } else {
                        //未获取到网关STA地址
//                        Toast.makeText(LoadActivity.this, "未获取到网关地址", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "未获取到网关地址");
                        //关闭loading 进入AP网络设置界面
                        if (progressBar.isShown()) {
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                        }

                        //重试 或者 重新配置网关信息 或者退出系统
                        Dialog dialog = new AlertDialog.Builder(LoadActivity.this)
                                .setTitle("未发现网关")
                                .setMessage("在当前网络中未找到网关")
                                .setPositiveButton("重新设置",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                Intent intent = new Intent(LoadActivity.this, APSetupActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                .setNegativeButton("重试一次",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                progressBar.setVisibility(ProgressBar.VISIBLE);
                                                connectAndLoginToGate();
                                            }
                                        }).create();

                        dialog.show();

                    }
                }
            });
        } else {
            //如果存在sta地址 则直连
            Toast.makeText(LoadActivity.this, String.format("直连STA[%s]", gateIP), Toast.LENGTH_LONG).show();
            textView.setText("连接网关...");
            connectToGate(gateIP, Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
        }
    }

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
                    short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                    ClientLoginMsgResponse logonResponseMsg = null;
                    try {
                        logonResponseMsg = MessageUtils.decomposeLogonReturnMsg(bytesData, bytesCount, idShould);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                        return;
                    }

                    Log.i(TAG, String.format("登录网关回应消息 [%s]", logonResponseMsg.toString()));
                    //判断登录结果
                    if (logonResponseMsg.getReturnCode() == ClientLoginMsgResponse.RETURN_CODE_OK) {
                        //将sta地址加入pref
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        editor.putString(Constants.SYSTEM_SETTINGS.GATE_STA_IP, gateStaIP);
                        editor.putString(Constants.SYSTEM_SETTINGS.GATE_MAC, logonResponseMsg.getGatePhysicalAddr());
                        editor.putString(Constants.SYSTEM_SETTINGS.GATE_VER, logonResponseMsg.getGateProtoVer() + "");
                        editor.putString(Constants.SYSTEM_SETTINGS.GATE_DESC, logonResponseMsg.getGateDesc());
                        editor.apply();
                        //设置网络通信特征码
                        MessageUtils.messageSign = logonResponseMsg.getSign();
                        //正常登录 进入主页面
                        Intent intent = new Intent(LoadActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
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
}
