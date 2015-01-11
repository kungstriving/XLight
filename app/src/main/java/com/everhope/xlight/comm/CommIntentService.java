package com.everhope.xlight.comm;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.util.Log;

import com.everhope.xlight.constants.Constants;

import java.io.IOException;
import java.io.InputStream;

/**
 * 通信相关后台服务类
 */
public class CommIntentService extends IntentService {
    private static final String TAG = "CommIntentService";

    /**
     * 检测网关IP
     */
    private static final String ACTION_DETECT_GATE = "com.everhope.xlight.comm.action.detect.gate";
    private static final String ACTION_REC_DATA = "com.everhope.xlight.comm.action.rec.data";

    //传递参数
    private static final String EXTRA_RESULTRECEIVER = "com.everhope.xlight.comm.extra.resultreceiver";

    /**
     * 启动网关侦测操作
     * @param context
     */
    public static void startActionDetectGate(Context context) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_DETECT_GATE);
//        intent.putExtra("", "");
        context.startActivity(intent);
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
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_REC_DATA:
                    ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    handleActionRecData(receiver);
                    break;
                case ACTION_DETECT_GATE:
                    handleActionDetectGate();
                    break;
                default:
                    break;
            }

        }
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
     * 检测网关服务
     */
    private void handleActionDetectGate() {
        //send udp packet data to 255.255.255.255
    }
}
