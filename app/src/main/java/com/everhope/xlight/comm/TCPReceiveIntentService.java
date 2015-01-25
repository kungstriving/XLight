package com.everhope.xlight.comm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.everhope.xlight.XLightApplication;
import com.everhope.xlight.constants.Constants;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class TCPReceiveIntentService extends IntentService {

    private static final String TAG = "TCPReceiveIntentService@Light";
    private static final String ACTION_LISTEN_TCP_DETECTGATE_BACK = "com.everhope.xlight.comm.action.tcp.detectgate.back";
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
        intent.setAction(ACTION_LISTEN_TCP_DETECTGATE_BACK);
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
            if (ACTION_LISTEN_TCP_DETECTGATE_BACK.equals(action)) {
                ResultReceiver receiver = intent.getParcelableExtra(EXTRA_TCP_BACK_RECEIVER);
                handleActionListenBack(receiver);
            } else if (ACTION_LISTEN_TCP_SERVICEDISCOVER_BACK.equals(action)) {
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
            is.read(bytes);
            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        receiver.send(resultCode, resultData);
    }
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionListenBack(ResultReceiver receiver) {
        //开始监听接收数据
        int resultCode = 0;
        Bundle resultData = new Bundle();

        receiver.send(resultCode, resultData);
    }

}
