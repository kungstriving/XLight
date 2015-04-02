package com.everhope.elighte.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.everhope.elighte.services.SyncIntentService;

/**
 * Created by kongxiaoyang on 2015/3/21.
 */
public class SyncStationAlarmReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 90000;

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, SyncIntentService.class);
        i.setAction(SyncIntentService.ACTION_SYNC_STATION_STATUS);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(i);

    }
}
