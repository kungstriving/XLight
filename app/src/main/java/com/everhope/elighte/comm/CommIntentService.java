package com.everhope.elighte.comm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.BindStationsToRemoterMsg;
import com.everhope.elighte.models.ClientLoginMsg;
import com.everhope.elighte.models.DeleteStationForceMsg;
import com.everhope.elighte.models.DeleteStationMsg;
import com.everhope.elighte.models.EnterStationIdentifyMsg;
import com.everhope.elighte.models.ExitStationIdentifyMsg;
import com.everhope.elighte.models.GetAllStationsMsg;
import com.everhope.elighte.models.MultiStationBrightControlMsg;
import com.everhope.elighte.models.MultiStationColorControlMsg;
import com.everhope.elighte.models.SearchStationsMsg;
import com.everhope.elighte.models.ServiceDiscoverMsg;
import com.everhope.elighte.models.SetGateNetworkMsg;
import com.everhope.elighte.models.SetLightsOnOffMsg;
import com.everhope.elighte.models.StationColorControlMsg;
import com.everhope.elighte.models.UnBindStationsFromRemoterMsg;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 所有通信消息的发送线程
 * 该对象只有一个线程，不会多线程发送。
 */
public class CommIntentService extends IntentService {
    private static final String TAG = "CommIntentService@Light";

    private static final String ACTION_CONNECT = "com.everhope.xlight.comm.action.connect";
    /**
     * 检测网关IP
     */
    private static final String ACTION_DETECT_GATE = "com.everhope.xlight.comm.action.detect.gate";
    private static final String ACTION_SERVICE_DISCOVER = "com.everhope.xlight.comm.action.servcie.discover";
    /**
     * 登录到网关
     */
    private static final String ACTION_LOGIN_GATE = "com.everhope.xlight.comm.action.login.gate";
    /**
     * 设置网关密码
     */
    private static final String ACTION_SET_NETWORK = "com.everhope.xlight.comm.action.set.network";

    /**
     * 获取所有灯列表
     */
    private static final String ACTION_GET_ALL_LIGHTS = "com.everhope.xlight.comm.action.get.all.lights";

    /**
     * 进入站点识别
     */
    private static final String ACTION_ENTER_STATION_ID = "com.everhope.xlight.comm.action.enter.stat.id";

    /**
     * 退出站点识别
     */
    private static final String ACTION_EXIT_STATION_ID = "com.everhope.xlight.comm.action.exit.stat.id";

    /**
     * 设置站点颜色
     */
    private static final String ACTION_SET_STATION_COLOR = "com.everhope.xlight.comm.action.set.station.clr";

    private static final String ACTION_SET_MULTI_STATION_BRIGHT = "com.everhope.xlight.comm.action.set.multi.station.bright";

    private static final String ACTION_SEARCH_NEW_STATIONS = "action.search.new.stations";

    private static final String ACTION_DELETE_STATION = "action.delete.station";

    private static final String ACTION_DELETE_STATION_FORCE = "action.delete.station.force";

    private static final String ACTION_SEND_SCENE_CONTROL = "action.send.scene.control";

    private static final String ACTION_SET_LIGHTS_ONOFF = "action.set.lights.onoff";

    private static final String ACTION_BIND_STATIONS_REMOTER = "action.bind.stations.remoter";

    private static final String ACTION_UNBIND_STATIONS_REMOTER = "action.unbind.stations.remoter";

    //////////////////////// 传递参数 ///////////////////////////////////////
    /**
     * 消息接收器
     */
    private static final String EXTRA_RESULTRECEIVER = "com.everhope.xlight.comm.extra.resultreceiver";
    /**
     * 网关地址对象
     */
    private static final String EXTRA_GATEADDR_PARAM = "com.everhope.xlight.comm.extra.gateaddr";

    /**
     * APP端唯一标示
     */
    private static final String EXTRA_CLIENTID = "com.everhope.xlight.comm.extra.clientid";

    /**
     * 家庭路由器的网络名称
     */
    private static final String EXTRA_SSID = "com.everhope.xlight.comm.extra.ssid";

    /**
     * 家庭路由器网络安全类型 WPAPSK or WPA2PSK or OPEN
     */
    private static final String EXTRA_SECURITY_TYPE = "com.everhope.xlight.comm.extra.security.type";
    /**
     * 网络密码
     */
    private static final String EXTRA_NET_PWD = "com.everhope.xlight.comm.extra.netpwd";

    /**
     * 站点ID
     */
    private static final String EXTRA_STATION_ID = "com.everhope.xlight.comm.extra.station.id";

    /**
     * 站点颜色hsb数组
     */
    private static final String EXTRA_HSB_COLOR = "com.everhope.xlight.comm.extra.hsb.color";

    private static final String EXTRA_MULTI_STATIONS_IDS = "com.everhope.xlight.comm.extra.multi.station.ids";

    private static final String EXTRA_MULTI_STATION_BRIGHT = "com.everhope.xlight.comm.extra.multi.station.bright";

    private static final String EXTRA_MULTI_STATION_COLORS = "extra.multi.station.colors";

    private static final String EXTRA_SEARCH_LAST_SECONDS = "extra.search.last.seconds";

    private static final String EXTRA_REMOTER_ID = "extra.remoter.id";
    private static final String EXTRA_BIND_STATION_REMOTER_NUM = "extra.bind.station.remoter.num";
    private static final String EXTRA_UNBIND_STATION_REMOTER_NUM = "extra.unbind.station.remoter.num";

    private static final String EXTRA_LIGHTS_ONOFF = "extra.lights.onoff";

    /////////////////////////////////// 服务启动入口 /////////////////////////////////////////////

    public static void startActionSetLightsOnOff(Context context, short[] stationIDs, boolean on, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_SET_LIGHTS_ONOFF);

        intent.putExtra(EXTRA_MULTI_STATIONS_IDS, stationIDs);
        intent.putExtra(EXTRA_LIGHTS_ONOFF, on);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    public static void startActionUnBindStationFromRemoter(Context context, short remoterID, byte controlNum, short[] ids, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_UNBIND_STATIONS_REMOTER);

        intent.putExtra(EXTRA_REMOTER_ID, remoterID);
        intent.putExtra(EXTRA_UNBIND_STATION_REMOTER_NUM, controlNum);
        intent.putExtra(EXTRA_MULTI_STATIONS_IDS, ids);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    public static void startActionBindStationToRemoter(Context context, short remoterID, byte controlNum, short[] ids, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_BIND_STATIONS_REMOTER);

        intent.putExtra(EXTRA_REMOTER_ID,remoterID);
        intent.putExtra(EXTRA_BIND_STATION_REMOTER_NUM,controlNum);
        intent.putExtra(EXTRA_MULTI_STATIONS_IDS, ids);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    public static void startActionSendSceneControlCmd(Context context, short[] stationIDs, int[] colors, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_SEND_SCENE_CONTROL);

        intent.putExtra(EXTRA_MULTI_STATIONS_IDS, stationIDs);
        intent.putExtra(EXTRA_MULTI_STATION_COLORS, colors);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    /**
     * 启动设置站点颜色活动
     *
     * @param context
     * @param stationID
     * @param hsb
     * @param receiver
     */
    public static void startActionSetStationColor(Context context, short stationID, byte[] hsb, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_SET_STATION_COLOR);

        intent.putExtra(EXTRA_STATION_ID, stationID);
        intent.putExtra(EXTRA_HSB_COLOR, hsb);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    public static void startActionDeleteStationForce(Context context, short stationID, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_DELETE_STATION_FORCE);
        intent.putExtra(EXTRA_STATION_ID, stationID);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    public static void startActionDeleteStation(Context context, short stationID, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_DELETE_STATION);
        intent.putExtra(EXTRA_STATION_ID, stationID);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    public static void startActionSearchNewStations(Context context, byte lastSecs, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_SEARCH_NEW_STATIONS);

        intent.putExtra(EXTRA_SEARCH_LAST_SECONDS, lastSecs);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    public static void startActionSetMultiStationsBright(Context context, short[] stationIDs, byte[] stationsBright, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_SET_MULTI_STATION_BRIGHT);

        intent.putExtra(EXTRA_MULTI_STATIONS_IDS, stationIDs);
        intent.putExtra(EXTRA_MULTI_STATION_BRIGHT, stationsBright);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    public static void startActionExitStationId(Context context, ResultReceiver receiver, String stationID) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_EXIT_STATION_ID);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);
        intent.putExtra(EXTRA_STATION_ID, stationID);

        context.startService(intent);
    }

    /**
     * 进入站点识别状态
     *
     * @param context
     * @param receiver
     * @param stationID 所要识别的站点ID
     */
    public static void startActionEnterStationId(Context context, ResultReceiver receiver, String stationID) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_ENTER_STATION_ID);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);
        intent.putExtra(EXTRA_STATION_ID, stationID);

        context.startService(intent);
    }

    /**
     * 开始网络连接
     *
     * @param context
     * @param address
     * @param receiver
     */
    public static void startActionConnect(Context context, String address, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_CONNECT);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);
        intent.putExtra(EXTRA_GATEADDR_PARAM, address);

        context.startService(intent);
    }

    /**
     * 发送服务发现命令
     * @param context
     */
    public static void startActionServiceDiscover(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_SERVICE_DISCOVER);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);
        context.startService(intent);
    }

    /**
     * 启动网关登录服务
     *
     * @param context
     * @param clientID
     */
    public static void startActionLoginToGate(Context context, ResultReceiver receiver, String clientID) {

        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_LOGIN_GATE);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);
        intent.putExtra(EXTRA_CLIENTID, clientID);

        context.startService(intent);
    }

    /**
     * 启动网关侦测操作
     * @param context
     */
    public static void startActionDetectGate(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_DETECT_GATE);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    /**
     * 设置网关连接信息
     *
     * @param context
     * @param receiver
     * @param ssid
     * @param pwd
     */
    public static void startActionSetGateNetwork(Context context, ResultReceiver receiver, String ssid, String securityType, String pwd) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_SET_NETWORK);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);
        intent.putExtra(EXTRA_SSID, ssid);
        intent.putExtra(EXTRA_SECURITY_TYPE, securityType);
        intent.putExtra(EXTRA_NET_PWD, pwd);

        context.startService(intent);
    }

    /**
     * 启动获取所有站点列表的服务
     * @param context
     * @param receiver
     */
    public static void startActionGetAllLights(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_GET_ALL_LIGHTS);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }

    /**
     * 启动UDP广播检测
     * TODO 暂不确定网关是否支持 未实现
     * @param context
     * @param receiver
     */
    public static void startActionBroadcastDetectGate(Context context, ResultReceiver receiver) {

    }
//    public static void startActionRecData(Context context, ResultReceiver receiver) {
//        Intent intent = new Intent(context, CommIntentService.class);
//        intent.setAction(ACTION_REC_DATA);
//        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);
//
//        context.startService(intent);
//    }

    public CommIntentService() {
        super("CommIntentService");
    }

    ////////////////////////////////////////////// 服务分发 //////////////////////////////////////
    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver receiver = null;
        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        if (!dataAgent.isConnected()) {
            receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
            if (receiver != null) {
                receiver.send(Constants.COMMON.EC_NETWORK_NO_CONNECTED, null);
            }
            return;
        }
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_SEND_SCENE_CONTROL:
                    //发送场景控制命令
                    short[] ids = intent.getShortArrayExtra(EXTRA_MULTI_STATIONS_IDS);
                    int[] colors = intent.getIntArrayExtra(EXTRA_MULTI_STATION_COLORS);
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    handleActionSendSceneControl(ids, colors, receiver);
                    break;
                case ACTION_SET_STATION_COLOR:
                    //设置场景中站点颜色
                    short stationIDSetClr = intent.getShortExtra(EXTRA_STATION_ID, (short)0);
                    byte[] colorHSB = intent.getByteArrayExtra(EXTRA_HSB_COLOR);
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    handleActionSetStationColor(stationIDSetClr, colorHSB, receiver);
                    break;
                case ACTION_SET_MULTI_STATION_BRIGHT:
                    //设置场景中的整体亮度
                    ids = intent.getShortArrayExtra(EXTRA_MULTI_STATIONS_IDS);
                    byte[] brightBytesArr = intent.getByteArrayExtra(EXTRA_MULTI_STATION_BRIGHT);
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    handleActionSetMultiStationsBright(ids,brightBytesArr, receiver);
                    break;
                case ACTION_SET_LIGHTS_ONOFF:
                    //设置多个站点on off 状态
                    ids = intent.getShortArrayExtra(EXTRA_MULTI_STATIONS_IDS);
                    boolean onb = intent.getBooleanExtra(EXTRA_LIGHTS_ONOFF, true);
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    handleActionSetLightsOnOff(ids, onb, receiver);
                    break;
                case ACTION_LOGIN_GATE:
                    //登录网关
                    String clientID = intent.getStringExtra(EXTRA_CLIENTID);
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    handleActionLoginToGate(clientID, receiver);
                    break;
                case ACTION_GET_ALL_LIGHTS:
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    handleActionGetAllLights(receiver);
                    break;
                case ACTION_SERVICE_DISCOVER:
                    //服务发现
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    handleActionServiceDis(receiver);
                    break;
                case ACTION_DETECT_GATE:
                    //网关侦测
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
//                    handleActionDetectGate(receiver);
                    handleActionDetectGateBroadcast(receiver);
                    break;
                case ACTION_SEARCH_NEW_STATIONS:
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    byte secs = intent.getByteExtra(EXTRA_SEARCH_LAST_SECONDS, (byte)30);
                    handleActionSearchStations(secs, receiver);
                    break;
                case ACTION_ENTER_STATION_ID:
                    //进入站点识别
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    String stationID = intent.getStringExtra(EXTRA_STATION_ID);
                    handleActionEnterStationID(stationID, receiver);
                    break;
                case ACTION_EXIT_STATION_ID:
                    //退出站点识别
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    String stationIDToExit = intent.getStringExtra(EXTRA_STATION_ID);
                    handleActionExitStationID(stationIDToExit, receiver);
                    break;
                case ACTION_CONNECT:
                    //连接网关
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    String address = intent.getStringExtra(EXTRA_GATEADDR_PARAM);
                    handleActionConnect(address, receiver);
                    break;
                case ACTION_SET_NETWORK:
                    //设置网关的网络密码
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    String ssid = intent.getStringExtra(EXTRA_SSID);
                    String securityType = intent.getStringExtra(EXTRA_SECURITY_TYPE);
                    String pwd = intent.getStringExtra(EXTRA_NET_PWD);
                    handleActionSetNetwork(ssid,securityType, pwd, receiver);
                    break;
                case ACTION_DELETE_STATION:
                    //删除站点
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    short stationIDToDelete = intent.getShortExtra(EXTRA_STATION_ID, (short)0);
                    handleActionDeleteStation(stationIDToDelete, receiver);
                    break;
                case ACTION_DELETE_STATION_FORCE:
                    //强制删除站点
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    stationIDToDelete = intent.getShortExtra(EXTRA_STATION_ID, (short)0);
                    handleActionDeleteStationForce(stationIDToDelete, receiver);
                    break;
                case ACTION_BIND_STATIONS_REMOTER:
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    short remoterID = intent.getShortExtra(EXTRA_REMOTER_ID,(short)0);
                    byte controlNum = intent.getByteExtra(EXTRA_BIND_STATION_REMOTER_NUM, (byte)0);
                    ids = intent.getShortArrayExtra(EXTRA_MULTI_STATIONS_IDS);
                    handleActionBindStationToRemoter(remoterID, controlNum, ids, receiver);
                    break;
                case ACTION_UNBIND_STATIONS_REMOTER:
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    remoterID = intent.getShortExtra(EXTRA_REMOTER_ID, (short)0);
                    controlNum = intent.getByteExtra(EXTRA_UNBIND_STATION_REMOTER_NUM, (byte)0);
                    ids = intent.getShortArrayExtra(EXTRA_MULTI_STATIONS_IDS);
                    handleActionUnbindStationFromRemoter(remoterID, controlNum, ids, receiver);
                    break;
                default:
                    break;
            }

        }
    }

    /////////////////////////////// 服务处理 ///////////////////////////////////

    private void handleActionSetLightsOnOff(short[] ids, boolean on, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        SetLightsOnOffMsg setLightsOnOffMsg = MessageUtils.composeSetLightsOnOffMsg(ids, on);

        Log.d(TAG, String.format("多站点开关-消息[%s]", setLightsOnOffMsg.toString()));

        byte[] bytes = setLightsOnOffMsg.toMessageByteArray();
        short msgID = setLightsOnOffMsg.getMessageID();

        try {
            //clear
            is.skip(is.available());

            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionUnbindStationFromRemoter(short remoterID, byte controlNum, short[] ids, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        UnBindStationsFromRemoterMsg unBindStationsFromRemoterMsg = MessageUtils.composeUnBindStationsFromRemoterMsg(remoterID, controlNum, ids);

        Log.d(TAG, String.format("解绑定站点遥控器-消息[%s]", unBindStationsFromRemoterMsg.toString()));

        byte[] bytes = unBindStationsFromRemoterMsg.toMessageByteArray();
        short msgID = unBindStationsFromRemoterMsg.getMessageID();

        try {
            //clear
            is.skip(is.available());

            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionBindStationToRemoter(short remID, byte controlNum,short[] ids, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        BindStationsToRemoterMsg bindStationsToRemoterMsg = MessageUtils.composeBindStationsToRemoterMsg(remID,controlNum,ids);

        Log.d(TAG, String.format("绑定站点到遥控器-消息[%s]", bindStationsToRemoterMsg.toString()));

        byte[] bytes = bindStationsToRemoterMsg.toMessageByteArray();
        short msgID = bindStationsToRemoterMsg.getMessageID();

        try {
            //clear
            is.skip(is.available());

            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionSendSceneControl(short[] ids, int[] colors, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        MultiStationColorControlMsg multiStationColorControlMsg = MessageUtils.composeMultiStationColorControlMsg(ids, colors);

        Log.d(TAG, String.format("多站点控制-消息[%s]", multiStationColorControlMsg.toString()));

        byte[] bytes = multiStationColorControlMsg.toMessageByteArray();
        short msgID = multiStationColorControlMsg.getMessageID();

        try {
            //clear
            is.skip(is.available());

            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionDeleteStationForce(short id, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        DeleteStationForceMsg deleteStationForceMsg = MessageUtils.composeDeleteStationForceMsg(id);

        Log.d(TAG, String.format("强制删除站点-消息[%s]", deleteStationForceMsg.toString()));

        byte[] bytes = deleteStationForceMsg.toMessageByteArray();
        short msgID = deleteStationForceMsg.getMessageID();

        try {
            //clear
            is.skip(is.available());

            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionDeleteStation(short id, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        DeleteStationMsg deleteStationMsg = MessageUtils.composeDeleteStationMsg(id);

        Log.d(TAG, String.format("删除站点-消息[%s]", deleteStationMsg.toString()));

        byte[] bytes = deleteStationMsg.toMessageByteArray();
        short msgID = deleteStationMsg.getMessageID();

        try {
            //clear
            is.skip(is.available());

            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionSearchStations(byte secs, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        SearchStationsMsg searchStationMsg = MessageUtils.composeSearchStationsMsg(secs);

        Log.d(TAG, String.format("搜索新站点-消息[%s]", searchStationMsg.toString()));

        byte[] bytes = searchStationMsg.toMessageByteArray();
        short msgID = searchStationMsg.getMessageID();

        try {
            //clear
            is.skip(is.available());

            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        //搜寻新站点 线程暂停
        try {
            Thread.sleep(Constants.SYSTEM_SETTINGS.SEARCH_STATIONS_LAST_SECONDS*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receiver.send(resultCode, resultData);
    }

    private void handleActionSetMultiStationsBright(short[] ids, byte[] brights, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        MultiStationBrightControlMsg multiStationBrightControlMsg = MessageUtils.composeMultiStationBrightControlMsg(ids, brights);

        Log.d(TAG, String.format("批量调节站点亮度-消息[%s]", multiStationBrightControlMsg.toString()));

        byte[] bytes = multiStationBrightControlMsg.toMessageByteArray();
        short msgID = multiStationBrightControlMsg.getMessageID();

        try {

            is.skip(is.available());

            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    /**
     * 设置站点颜色
     * @param stationID
     * @param hsb
     * @param receiver
     */
    private void handleActionSetStationColor(short stationID, byte[] hsb, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        StationColorControlMsg stationColorControlMsg = MessageUtils.composeStationColorControlMsg(stationID,hsb);

        Log.i(TAG, String.format("调节站点颜色-消息[%s]", stationColorControlMsg.toString()));

        byte[] bytes = stationColorControlMsg.toMessageByteArray();
        short msgID = stationColorControlMsg.getMessageID();

        try {
            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    /**
     * 退出站点识别
     * @param stationIDToExit
     * @param receiver
     */
    private void handleActionExitStationID(String stationIDToExit, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        ExitStationIdentifyMsg exitStationMsg = MessageUtils.composeExitStationIdentifyMsg(Short.parseShort(stationIDToExit));

        Log.i(TAG, String.format("退出站点识别-消息[%s]", exitStationMsg.toString()));

        byte[] bytes = exitStationMsg.toMessageByteArray();
        short msgID = exitStationMsg.getMessageID();

        try {
            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    /**
     * 进入站点识别
     * @param stationID
     * @param receiver
     */
    private void handleActionEnterStationID(String stationID, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        EnterStationIdentifyMsg enterStationIdMsg = MessageUtils.composeEnterStationIdentifyMsg(Short.parseShort(stationID));

        Log.i(TAG, String.format("进入站点识别 消息[%s]", enterStationIdMsg.toString()));

        byte[] bytes = enterStationIdMsg.toMessageByteArray();
        short msgID = enterStationIdMsg.getMessageID();

        try {
            //清空管道
            is.skip(is.available());

            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionGetAllLights(ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        GetAllStationsMsg getAllLightsMsg = MessageUtils.composeGetAllStationsMsg();

        Log.i(TAG, String.format("获取所有设备列表消息[%s]", getAllLightsMsg.toString()));
//        Log.i(TAG, String.format("获取所有灯列表消息字节数组[%s]", Hex.encodeHexString(getAllLightsMsg.toMessageByteArray())));

        byte[] bytes = getAllLightsMsg.toMessageByteArray();
        short msgID = getAllLightsMsg.getMessageID();

        for(int i = 0; i < Constants.SYSTEM_SETTINGS.SEND_RETRY_TIMES; i++) {
            try {
                //clear tunnel
                is.skip(is.available());
                os.write(bytes);
                os.flush();

                byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
                byte[] readedBytes;
                int readedNum = is.read(tempBytes);
                readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

                resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
                resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
                resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
                //正常发送和接收 则退出
                break;
            } catch (IOException e) {
                resultCode = Constants.COMMON.EC_NETWORK_ERROR;
                e.printStackTrace();
                Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
                //出错则重试
                //continue
                //TODO 所有出错情况下处理
            }
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionSetNetwork(String ssid,String securityType, String pwd, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        SetGateNetworkMsg setGateNetworkMsg = MessageUtils.composeSetGateMsg(ssid,securityType, pwd);

        Log.i(TAG, String.format("设置网关网络密码消息[%s]", setGateNetworkMsg.toString()));

        byte[] bytes = setGateNetworkMsg.toMessageByteArray();
        short msgID = setGateNetworkMsg.getMessageID();

        try {
            os.write(bytes);
            os.flush();

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionLoginToGate(String clientID, ResultReceiver receiver) {

        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();
        ClientLoginMsg loginMsg = MessageUtils.composeLogonMsg(clientID);

        Log.i(TAG, String.format("登录网关发送消息 [%s]", loginMsg.toString()));

        byte[] bytes = loginMsg.toMessageByteArray();
        short msgID = loginMsg.getMessageID();
        try {
            //清空管道
            is.skip(is.available());

            os.write(bytes);
            os.flush();
            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = is.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);

            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }

        receiver.send(resultCode, resultData);
    }

    /**
     * 进行网关连接
     * @param receiver
     */
    private void handleActionConnect(String address, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        try {
            dataAgent.buildConnection(address, Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
            //连接成功
            Log.i(TAG, String.format("连接[%s]成功", address));
            receiver.send(resultCode, resultData);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, e.getMessage());
        }

        resultCode = Constants.COMMON.EC_NETWORK_CONNET_FAIL;
        receiver.send(resultCode, resultData);

        return;
    }

//    private void handleActionRecData(ResultReceiver receiver) {
//        InputStream istream = DataAgent.getSingleInstance().getInputStream();
//        Bundle bundle = new Bundle();
//
//        byte[] dataBuf = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
//        try {
//            int actualReadedBytes = istream.read(dataBuf);
//
//            bundle.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, actualReadedBytes);
//            bundle.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, dataBuf);
//
//            receiver.send(Constants.COMMON.RESULT_CODE_OK, bundle);
//        } catch (IOException e) {
//            Log.e(TAG, "读取数据出错");
//            Log.e(TAG, e.getMessage());
//            e.printStackTrace();
//            //网络错误
//            receiver.send(Constants.COMMON.EC_NETWORK_ERROR, new Bundle());
//            return;
//        } finally {
//            try {
//                if (istream != null) {
//                    istream.close();
//                }
//            } catch (IOException e) {
//                Log.e(TAG, "关闭输入流出错");
//                Log.e(TAG, e.getMessage());
//            }
//        }
//
//    }

    /**
     * 发送服务发现报文
     */
    private void handleActionServiceDis(ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        OutputStream os = XLightApplication.getInstance().getDataAgent().getOutputStream();
        InputStream inputStream = XLightApplication.getInstance().getDataAgent().getInputStream();
        ServiceDiscoverMsg serviceDiscoverMsg = MessageUtils.composeServiceDiscoverMsg();
        byte[] sendBytes = serviceDiscoverMsg.toMessageByteArray();
        short msgID = serviceDiscoverMsg.getMessageID();

        try {
            os.write(sendBytes);
            os.flush();

            //开始监听接收数据

            byte[] tempBytes = new byte[Constants.SYSTEM_SETTINGS.NETWORK_PKG_LENGTH];
            byte[] readedBytes;
            int readedNum = inputStream.read(tempBytes);
            readedBytes = ArrayUtils.subarray(tempBytes, 0, readedNum);
            resultData.putByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT, readedBytes);
            resultData.putInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT, readedNum);
            resultData.putShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID, msgID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_ERROR;
        }
        receiver.send(resultCode, resultData);
    }

    /**
     * UDP广播方式检测网关服务
     * @param receiver
     */
    private void handleActionDetectGateBroadcast(ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.setBroadcast(true);

            InetAddress inetAddress = InetAddress.getByName(AppUtils.getSubnetBroadcaseAddr(CommIntentService.this));

            ServiceDiscoverMsg serviceDiscoverMsg = MessageUtils.composeServiceDiscoverMsg();
            byte[] data = serviceDiscoverMsg.toMessageByteArray();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress,Constants.SYSTEM_SETTINGS.GATE_BROADCAST_PORT);
            datagramSocket.send(datagramPacket);

            datagramSocket.close();

            DatagramSocket datagramReceiveSocket = new DatagramSocket(Constants.SYSTEM_SETTINGS.GATE_BROADCAST_PORT);
            datagramReceiveSocket.setSoTimeout(Constants.SYSTEM_SETTINGS.NETWORK_DATA_SOTIMEOUT);

            byte[] readedBytes = new byte[1024];
            DatagramPacket recPacket = new DatagramPacket(readedBytes, readedBytes.length);
            datagramReceiveSocket.receive(recPacket);

            //解析消息
            try {
                MessageUtils.decomposeServiceDiscoverMsg(readedBytes, readedBytes.length, serviceDiscoverMsg.getMessageID());
            } catch (Exception e) {
                resultCode = Constants.COMMON.EC_MESSAGE_RESOLVE_FAILED;
            }

            InetAddress gateAddr = recPacket.getAddress();
            String gateIP = gateAddr.getHostAddress();

            resultData.putString(Constants.KEYS_PARAMS.GATE_STA_IP, gateIP);
        } catch (SocketException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));

            resultCode = Constants.COMMON.EC_NETWORK_NOFOUND_STA_GATE;

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_NOFOUND_STA_GATE;
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
            resultCode = Constants.COMMON.EC_NETWORK_NOFOUND_STA_GATE;
        }

        receiver.send(resultCode, resultData);
    }

    /**
     * TCP方式检测网关服务
     */
    private void handleActionDetectGate(ResultReceiver receiver) {

        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        String[] addresses = AppUtils.getSubnetAddresses(getApplicationContext());

        for(int i = 0; i < addresses.length; i++) {
            try {
                Log.i(TAG, String.format("尝试连接[%s]", addresses[i]));

                dataAgent.buildConnection(addresses[i], Constants.SYSTEM_SETTINGS.GATE_TALK_PORT);
                //连接成功 接下来进行服务发现 如果服务发现成功，则获取到了网关地址
                Log.i(TAG, String.format("[%s]连接成功，开始发现服务", addresses[i]));

                final InputStream inputStream = dataAgent.getInputStream();
                final String disKey = "discovered";
                final String flag = "flag";
                final Bundle tmpBundle = new Bundle();
                tmpBundle.putBoolean(disKey, false);
                tmpBundle.putBoolean(flag, false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] bytes = new byte[1024];
                        try {
                            inputStream.read(bytes);
                            Log.i(TAG, "服务发现成功");
                            tmpBundle.putBoolean(disKey, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                            tmpBundle.putBoolean(disKey, false);
                        }
                        tmpBundle.putBoolean(flag, true);
                    }
                }).start();

                OutputStream outputStream = dataAgent.getOutputStream();
                outputStream.write(MessageUtils.composeServiceDiscoverMsg().toMessageByteArray());
                outputStream.flush();

                while(true) {
                    if (tmpBundle.getBoolean(flag)) {
                        //接收消息结束
                        if (tmpBundle.getBoolean(disKey)) {
                            //收到回应消息
                            //说明找到了网关
                            Log.i(TAG, "返回上层调用者");
                            resultCode = 0;
                            resultData.putString(Constants.KEYS_PARAMS.GATE_STA_IP, addresses[i]);
                            receiver.send(resultCode, resultData);
                            return;
                        }
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, String.format("连接地址[%s]失败", addresses[i]));
            }
        }

        //未找到网关
        resultCode = Constants.COMMON.EC_NETWORK_NOFOUND_STA_GATE;
        receiver.send(resultCode, resultData);
        return;
    }
}
