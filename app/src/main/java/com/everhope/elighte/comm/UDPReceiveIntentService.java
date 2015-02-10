package com.everhope.elighte.comm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.everhope.elighte.constants.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *
 */
public class UDPReceiveIntentService extends IntentService {

    private static final String TAG = "UDPREceiveIntentService@Light";

    private static final String ACTION_LISTEN_DETECT_GATE_BACK = "com.everhope.xlight.comm.action.listen.detect.gate.back";

    private static final String EXTRA_DETECT_GATE_BACK_RECEIVER = "com.everhope.xlight.comm.extra.detect.gate.back.receiver";
    /**
     * 启动监听
     * @param context
     */
    public static void startActionListenDetectBack(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, UDPReceiveIntentService.class);
        intent.setAction(ACTION_LISTEN_DETECT_GATE_BACK);

        //设置回调对象
        intent.putExtra(EXTRA_DETECT_GATE_BACK_RECEIVER, receiver);

        context.startService(intent);
    }

    public UDPReceiveIntentService() {
        super("UDPReceiveIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LISTEN_DETECT_GATE_BACK.equals(action)) {
                //监听回应
                ResultReceiver receiver = intent.getParcelableExtra(EXTRA_DETECT_GATE_BACK_RECEIVER);
                handleAction(receiver);
            }
        }
    }

    /**
     * 执行操作
     * @param receiver
     */
    private void handleAction(ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();
        try {
            DatagramSocket datagramSocket = new DatagramSocket(8899);
            byte[] receiveData = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
            Log.i(TAG, "开始监听UDP回应");
            datagramSocket.receive(datagramPacket);
            String str = new String(datagramPacket.getData()).trim();
            Log.d(TAG, str);
            resultData.putString(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, str);
            receiver.send(resultCode, resultData);
        } catch (SocketException e) {
            Log.w(TAG, "出错啦");
            e.printStackTrace();
            return;
        } catch (IOException e) {
            Log.w(TAG, "出错啦");
            e.printStackTrace();
            return;
        }

    }
}
