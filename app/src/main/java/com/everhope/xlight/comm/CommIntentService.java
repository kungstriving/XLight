package com.everhope.xlight.comm;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.everhope.xlight.XLightApplication;
import com.everhope.xlight.constants.Constants;
import com.everhope.xlight.helpers.MessageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 通信相关后台服务类
 */
public class CommIntentService extends IntentService {
    private static final String TAG = "CommIntentService@Light";

    private static final String ACTION_CONNECT = "com.everhope.xlight.comm.action.connect";
    /**
     * 检测网关IP
     */
    private static final String ACTION_DETECT_GATE = "com.everhope.xlight.comm.action.detect.gate";
    private static final String ACTION_REC_DATA = "com.everhope.xlight.comm.action.rec.data";
    private static final String ACTION_SERVICE_DISCOVER = "com.everhope.xlight.comm.action.servcie.discover";

    //传递参数
    private static final String EXTRA_RESULTRECEIVER = "com.everhope.xlight.comm.extra.resultreceiver";

    public static void startActionConnect(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_CONNECT);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    public static void startActionServiceDiscover(Context context) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_SERVICE_DISCOVER);

        context.startService(intent);
    }

    /**
     * 启动网关侦测操作
     * @param context
     */
    public static void startActionDetectGate(Context context) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_DETECT_GATE);

        context.startService(intent);
    }

    public static void startActionRecData(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_REC_DATA);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    public CommIntentService() {
        super("CommIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver receiver = null;
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_REC_DATA:
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    handleActionRecData(receiver);
                    break;
                case ACTION_DETECT_GATE:
                    handleActionDetectGate();
                    break;
                case ACTION_SERVICE_DISCOVER:
                    handleActionServiceDis();
                    break;
                case ACTION_CONNECT:
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);

                    handleActionConnect(receiver);
                    break;
                default:
                    break;
            }

        }
    }

    private void handleActionConnect(ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();
        try {
            DataAgent.getSingleInstance().buildConnection("10.10.100.254", Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionRecData(ResultReceiver receiver) {
        InputStream istream = DataAgent.getSingleInstance().getInputStream();
        Bundle bundle = new Bundle();

        byte[] dataBuf = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
        try {
            int actualReadedBytes = istream.read(dataBuf);

            bundle.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, actualReadedBytes);
            bundle.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, dataBuf);

            receiver.send(Constants.COMMON.RESULT_CODE_OK, bundle);
        } catch (IOException e) {
            Log.e(TAG, "读取数据出错");
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            //网络错误
            receiver.send(Constants.COMMON.RESULT_CODE_NETWORK_ERROR, new Bundle());
        } finally {
            try {
                if (istream != null) {
                    istream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "关闭输入流出错");
                Log.e(TAG, e.getMessage());
            }
        }

    }

    /**
     * 发送服务发现报文
     */
    private void handleActionServiceDis() {
        OutputStream os = XLightApplication.getInstance().getDataAgent().getOutputStream();
        byte[] bytes = MessageUtils.composeServiceDiscoverMsg();
        try {
            os.write(bytes);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * 检测网关服务
     */
    private void handleActionDetectGate() {
        //目前未知UDP广播方式检测网关是否可用

        //使用TCP方式连接
        for (int i = 0; i < 5; i++) {
            Log.i(TAG, "检测中...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        final Handler handler = new Handler();
//        final int count = 0;
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.i(TAG, "detecting...");
//                if (count < 5) {
//                    handler.postDelayed(this, 2000);
//                } else {
//                    handler.removeCallbacks(this);
//                }
//            }
//        }, 2000);

    }
}
