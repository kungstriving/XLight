package com.everhope.elighte.services;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.activities.APSetupActivity;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.GetAllLightsStatusMsg;
import com.everhope.elighte.models.GetStationsStatusMsg;
import com.everhope.elighte.models.GetStationsStatusMsgResponse;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.MultiStationBrightControlMsg;
import com.everhope.elighte.models.SetGateNetworkMsg;
import com.everhope.elighte.models.StationBrightTurnCmd;
import com.everhope.elighte.models.StationColorTurnCmd;
import com.everhope.elighte.models.StationDeviceStatusCmd;
import com.everhope.elighte.models.StationDeviceSwitchCmd;
import com.everhope.elighte.models.StationSubCmd;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 同步站点状态的服务
 */
public class SyncIntentService extends IntentService {

    private static final String TAG = "SyncIntentService@Light";

    public static final String ACTION_SYNC_STATION_STATUS =
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
//                ResultReceiver receiver = new ResultReceiver(new )
                handleActionSyncStationStatus();
                checkSocketStatus();
            }
        }
    }

    private void checkSocketStatus() {
        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        if (!dataAgent.isConnected()) {
            Log.w(TAG, "当前socket已断开，进行重连操作");
            Toast.makeText(getApplicationContext(), "网关失联，正在重新连接...", Toast.LENGTH_LONG).show();
            try {
                dataAgent.reconnect();
            } catch (IOException e) {
                //重连失败，需要进行重新网关设置
                Log.w(TAG, "网关重连失败");
                showReconfigWindow();
            }
        }
    }

    private void showReconfigWindow() {

        Dialog dialog = new AlertDialog.Builder(getApplicationContext())
                .setTitle("错误")
                .setMessage("网关连接失败")
                .setPositiveButton("重新设置",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = new Intent(getApplicationContext(), APSetupActivity.class);
                                startActivity(intent);
                            }
                        })
                .setNegativeButton("重试一次",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkSocketStatus();
                            }
                        }).create();

        dialog.show();
    }
    /**
     * 处理同步操作
     */
    private void handleActionSyncStationStatus() {
        Log.i(TAG, "启动同步站点状态操作");

        //最多一次遥信30个站点
        int maxSignalStationCount = 30;

        //获取要更新的站点id
        List<Light> lightList = Light.getAll();
        List<Short> shortIDs = new ArrayList<>();
        int count = 1;
        for (Light light : lightList) {
            shortIDs.add(Short.parseShort(light.lightID));
            if(shortIDs.size() == maxSignalStationCount || count == lightList.size()) {
                //发送消息
                Short[] tmpIDs = new Short[shortIDs.size()];
                tmpIDs = shortIDs.toArray(tmpIDs);
                sendToGate(ArrayUtils.toPrimitive(tmpIDs));
                shortIDs.clear();
            }
            count++;
        }

    }

    private void sendToGate(short [] ids) {

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        Socket socket = dataAgent.getSocket();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        GetStationsStatusMsg getStationsStatusMsg = MessageUtils.composeGetStationsStatusMsg(ids);

        Log.d(TAG, String.format("批量获取站点状态-消息[%s]", getStationsStatusMsg.toString()));

        byte[] bytes = getStationsStatusMsg.toMessageByteArray();
        short msgID = getStationsStatusMsg.getMessageID();

        try {
            //清空管道 重要！！ 否则读取到之前的消息
            //socket.setSoTimeout(Constants.SYSTEM_SETTINGS.NETWORK_DATA_LONG_SOTIMEOUT);
            is.skip(is.available());

            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);
            socket.setSoTimeout(Constants.SYSTEM_SETTINGS.NETWORK_DATA_SOTIMEOUT);
            GetStationsStatusMsgResponse getStationsStatusMsgResponse = null;
            try {
                getStationsStatusMsgResponse = MessageUtils.decomposeGetStationsStatusMsgResponse(readedBytes, readedBytes.length, msgID);
            } catch (Exception e) {
                Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                return;
            }
            Log.i(TAG, String.format("批量获取站点状态回应消息 [%s]",getStationsStatusMsgResponse.toString()));
            handleMessageReturn(getStationsStatusMsgResponse, msgID);

        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
        }
    }

    /**
     * 存入数据库
     * @param msgResponse
     * @param msgID
     */
    private void handleMessageReturn(GetStationsStatusMsgResponse msgResponse, short msgID) {
        //检测消息ID
        if (msgResponse.getMessageID() != msgID) {
            Log.w(TAG, String.format("消息ID不匹配 发送[%s] 收到[%s]", msgID, msgResponse.getMessageID()));
            return;
        }

        Map<Short, List<StationSubCmd>> map = msgResponse.getMap();
        for(Map.Entry<Short, List<StationSubCmd>> entry : map.entrySet()) {
            short stationID = entry.getKey();
            List<StationSubCmd> commands = entry.getValue();
            Light light = Light.getByLightID(stationID+"");
            for (StationSubCmd stationSubCmd : commands) {
                FunctionCodes.SubFunctionCodes functionCodes = stationSubCmd.getSubFunctionCode();
                switch (functionCodes) {
                    case DEVICE_STATUS:
                        StationDeviceStatusCmd stationDeviceStatusCmd = (StationDeviceStatusCmd)stationSubCmd;
                        byte flagsByte = stationDeviceStatusCmd.getFlags();
                        byte temp = 0;
                        temp = (byte)(flagsByte & 0x01);
                        light.lostConnection = (temp != 0);
                        temp = (byte)(flagsByte & 0x02);
                        light.triggerAlarm = (temp != 0);
                        break;
                    case DEVICE_SWITCH:
                        StationDeviceSwitchCmd stationDeviceSwitchCmd = (StationDeviceSwitchCmd)stationSubCmd;
                        light.switchOn = stationDeviceSwitchCmd.isSwitchStatus();
                        break;
                    case COLOR_TURN:
                        StationColorTurnCmd stationColorTurnCmd = (StationColorTurnCmd)stationSubCmd;
                        byte h = stationColorTurnCmd.getH();
                        byte s = stationColorTurnCmd.getS();
                        byte b = stationColorTurnCmd.getB();
                        byte[] hsb = new byte[3];
                        hsb[0] = h;
                        hsb[1] = s;
                        hsb[2] = b;
                        int rgbColor = AppUtils.hsbColorValueToRGB(hsb);
                        light.rColor = Color.red(rgbColor);
                        light.gColor = Color.green(rgbColor);
                        light.bColor = Color.blue(rgbColor);

                        break;
                    case BRIGHTNESS_TURN:
                        StationBrightTurnCmd stationBrightTurnCmd = (StationBrightTurnCmd)stationSubCmd;
                        light.brightness = stationBrightTurnCmd.getBrightValue();
                        break;
                    default:
                        break;
                }
            }

            //存储
            light.save();
        }
    }
}
