package com.everhope.xlight.comm;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * 通信相关后台服务类
 */
public class CommIntentService extends IntentService {
    private static final String TAG = "CommIntentService";

    /**
     * 检测网关IP
     */
    private static final String ACTION_DETECT_GATE = "com.everhope.xlight.comm.action.detect.gate";
//    private static final String ACTION_BAZ = "com.everhope.xlight.comm.action.detect.date";

    //传递参数
    private static final String EXTRA_PARAM1 = "com.everhope.xlight.comm.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.everhope.xlight.comm.extra.PARAM2";

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

    public CommIntentService() {
        super("CommIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_DETECT_GATE:
//                    hand
                    break;
                default:
                    break;
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
