package com.everhope.elighte.comm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.constants.Constants;

import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * 所有消息发送的接收线程
 * 单线程操作
 */
public class TCPReceiveIntentService extends IntentService {

    private static final String TAG = "TCPReceiveIntentService@Light";
    private static final String ACTION_LISTEN_TCP_BACK = "com.everhope.xlight.comm.action.tcp.back";
    private static final String ACTION_LISTEN_TCP_SERVICEDISCOVER_BACK = "com.everhope.xlight.comm.action.tcp.servicediscover.back";

    private static final String EXTRA_TCP_BACK_RECEIVER = "com.everhope.xlight.comm.extra.tcp.detectgate.receiver";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionListenBack(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, TCPReceiveIntentService.class);
        intent.setAction(ACTION_LISTEN_TCP_BACK);
        intent.putExtra(EXTRA_TCP_BACK_RECEIVER, receiver);
        context.startService(intent);
    }

    public static void startActionListenServiceDiscover(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, TCPReceiveIntentService.class);
        intent.setAction(ACTION_LISTEN_TCP_SERVICEDISCOVER_BACK);
        intent.putExtra(EXTRA_TCP_BACK_RECEIVER, receiver);
        context.startService(intent);
    }

    public TCPReceiveIntentService() {
        super("TCPReceiveIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LISTEN_TCP_BACK.equals(action)) {
                //监听网关回应消息
                ResultReceiver receiver = intent.getParcelableExtra(EXTRA_TCP_BACK_RECEIVER);
                handleActionListenBack(receiver);
            } else if (ACTION_LISTEN_TCP_SERVICEDISCOVER_BACK.equals(action)) {
                //监听服务发现回应消息
                ResultReceiver receiver = intent.getParcelableExtra(EXTRA_TCP_BACK_RECEIVER);
                handleActionServiceDisBack(receiver);
            }
        }
    }

    private void handleActionServiceDisBack(ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        InputStream is = XLightApplication.getInstance().getDataAgent().getInputStream();
        byte[] bytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
        try {
            int readedNum = is.read(bytes);
            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, bytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, e.getMessage());
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }
        receiver.send(resultCode, resultData);
    }

    /**
     * 监听网络回应
     * @param receiver
     */
    private void handleActionListenBack(ResultReceiver receiver) {
        //开始监听接收数据
        int resultCode = 0;
        Bundle resultData = new Bundle();

        InputStream inputStream = XLightApplication.getInstance().getDataAgent().getInputStream();
        byte[] bytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
        byte[] readedBytes;
        try {
            int readedNum = inputStream.read(bytes);
            readedBytes = ArrayUtils.subarray(bytes, 0, readedNum);
            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, e.getMessage());
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }
        receiver.send(resultCode, resultData);
    }

}
