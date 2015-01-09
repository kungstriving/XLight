package com.everhope.xlight.comm;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.ResultReceiver;

/**
 *
 */
public class UDPReceiveIntentService extends IntentService {
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
        receiver.send(0, null);
    }
}
