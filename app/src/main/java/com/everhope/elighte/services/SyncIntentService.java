package com.everhope.elighte.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.GetAllLightsStatusMsg;
import com.everhope.elighte.models.SetGateNetworkMsg;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 同步站点状态的服务
 */
public class SyncIntentService extends IntentService {

    private static final String TAG = "SyncIntentService@Light";

    private static final String ACTION_SYNC_STATION_STATUS =
            "com.everhope.elighte.services.action.sync.station.status";

//    private static final String EXTRA_PARAM1 = "com.everhope.elighte.services.extra.PARAM1";
//    private static final String EXTRA_PARAM2 = "com.everhope.elighte.services.extra.PARAM2";


    public static void startActionSyncStationStatus(Context context) {
        Intent intent = new Intent(context, SyncIntentService.class);
        intent.setAction(ACTION_SYNC_STATION_STATUS);
        context.startService(intent);
    }

    public SyncIntentService() {
        super("SyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SYNC_STATION_STATUS.equals(action)) {
                handleActionSyncStationStatus();
            }
        }
    }

    /**
     * 处理同步操作
     */
    private void handleActionSyncStationStatus() {
        Log.i(TAG, "启动同步站点状态操作");

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        GetAllLightsStatusMsg getAllLightsStatusMsg = MessageUtils.composeGetAllLightsStatusMsg();

//        Log.i(TAG, String.format("设置网关网络密码消息[%s]", setGateNetworkMsg.toString()));
    }

}
