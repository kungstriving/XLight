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
import com.everhope.elighte.models.ClientLoginMsg;
import com.everhope.elighte.models.EnterStationIdentifyMsg;
import com.everhope.elighte.models.GetAllStationsMsg;
import com.everhope.elighte.models.SetGateNetworkMsg;

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
    private static final String ACTION_ENTER_STATION_ID = "com.everhope.xlight.comm.action.enter_stat_id";

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
     * 设置网关的网络名称
     */
    private static final String EXTRA_SSID = "com.everhope.xlight.comm.extra.ssid";
    /**
     * 网络密码
     */
    private static final String EXTRA_NET_PWD = "com.everhope.xlight.comm.extra.netpwd";

    /**
     * 站点ID
     */
    private static final String EXTRA_STATION_ID = "com.everhope.xlight.comm.extra.station.id";

    /////////////////////////////////// 服务启动入口 /////////////////////////////////////////////

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
    public static void startActionServiceDiscover(Context context) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_SERVICE_DISCOVER);

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
     * @param name
     * @param pwd
     */
    public static void startActionSetGateNetwork(Context context, ResultReceiver receiver, String name, String pwd) {
        Intent intent = new Intent(context, CommIntentService.class);
        intent.setAction(ACTION_SET_NETWORK);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);
        intent.putExtra(EXTRA_SSID, name);
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
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
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
                case ACTION_DETECT_GATE:
                    //网关侦测
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
//                    handleActionDetectGate(receiver);
                    handleActionDetectGateBroadcast(receiver);
                    break;
                case ACTION_SERVICE_DISCOVER:
                    //服务发现
                    handleActionServiceDis();
                    break;
                case ACTION_ENTER_STATION_ID:
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    String stationID = intent.getStringExtra(EXTRA_STATION_ID);
                    handleActionEnterStationID(stationID, receiver);
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
                    String pwd = intent.getStringExtra(EXTRA_NET_PWD);
                    handleActionSetNetwork(ssid, pwd, receiver);
                    break;
                default:
                    break;
            }

        }
    }

    /////////////////////////////// 服务处理 ///////////////////////////////////

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

        Log.i(TAG, String.format("获取所有灯列表消息[%s]", getAllLightsMsg.toString()));

        byte[] bytes = getAllLightsMsg.toMessageByteArray();
        short msgID = getAllLightsMsg.getMessageID();

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
        }

        receiver.send(resultCode, resultData);
    }

    private void handleActionSetNetwork(String ssid, String pwd, ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        OutputStream os = dataAgent.getOutputStream();
        InputStream is = dataAgent.getInputStream();

        SetGateNetworkMsg setGateNetworkMsg = MessageUtils.composeSetGateMsg(ssid, pwd);

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
    private void handleActionServiceDis() {
        OutputStream os = XLightApplication.getInstance().getDataAgent().getOutputStream();
        byte[] bytes = MessageUtils.composeServiceDiscoverMsg();

        try {
            os.write(bytes);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));
        }
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

            byte[] data = MessageUtils.composeServiceDiscoverMsg();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress,Constants.SYSTEM_SETTINGS.GATE_BROADCAST_PORT);
            datagramSocket.send(datagramPacket);

            datagramSocket.close();

            DatagramSocket datagramReceiveSocket = new DatagramSocket(Constants.SYSTEM_SETTINGS.GATE_BROADCAST_PORT);
            datagramReceiveSocket.setSoTimeout(Constants.SYSTEM_SETTINGS.NETWORK_DATA_SOTIMEOUT);

            byte[] readedBytes = new byte[1024];
            DatagramPacket recPacket = new DatagramPacket(readedBytes, readedBytes.length);
            datagramReceiveSocket.receive(recPacket);

            //解析消息
            MessageUtils.decomposeServiceDiscoverMsg(readedBytes, readedBytes.length);

            InetAddress gateAddr = recPacket.getAddress();
            String gateIP = gateAddr.getHostAddress();

            resultCode = 0;
            resultData.putString(Constants.KEYS_PARAMS.GATE_STA_IP, gateIP);
            receiver.send(resultCode, resultData);

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
                outputStream.write(MessageUtils.composeServiceDiscoverMsg());
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
