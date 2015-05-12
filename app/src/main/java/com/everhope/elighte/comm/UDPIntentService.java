package com.everhope.elighte.comm;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.ServiceDiscoverMsg;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * UDP消息服务类
 */
public class UDPIntentService extends IntentService {

    private static final String TAG = "UPDIntentService@Light";
    /////////////////////// 操作类型 /////////////////////////////////
    /**
     * 侦测网关
     */
    private static final String ACTION_DETECT_GATE = "com.everhope.elighte.comm.action.udpis.detect.gate";

    //////////////////////// 参数key //////////////////////////////////
    /**
     * 消息接收器
     */
    private static final String EXTRA_RESULTRECEIVER = "com.everhope.xlight.comm.extra.udpis.resultreceiver";

    public static void startActionDetectGate(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, UDPIntentService.class);
        intent.setAction(ACTION_DETECT_GATE);
        intent.putExtra(EXTRA_RESULTRECEIVER, receiver);

        context.startService(intent);
    }


    public UDPIntentService() {
        super("UDPIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver receiver = null;
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {
                case ACTION_DETECT_GATE:
                    receiver = intent.getParcelableExtra(EXTRA_RESULTRECEIVER);
                    handleActionDetectGate(receiver);
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 侦测网络
     * @param receiver
     */
    private void handleActionDetectGate(ResultReceiver receiver) {
        int resultCode = 0;
        Bundle resultData = new Bundle();

        for (int i = 0; i < Constants.SYSTEM_SETTINGS.BROADCAST_SERVICE_DISCOVER_RETRY_TIMES; i++) {
            try {
                DatagramSocket datagramSocket = new DatagramSocket();
                datagramSocket.setBroadcast(true);

                InetAddress inetAddress = InetAddress.getByName(AppUtils.getSubnetBroadcaseAddr(UDPIntentService.this));

                ServiceDiscoverMsg serviceDiscoverMsg = MessageUtils.composeServiceDiscoverMsg();
//                byte[] data = serviceDiscoverMsg.toMessageByteArray();
                byte[] data = "HF-A11ASSISTHREAD".getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, Constants.SYSTEM_SETTINGS.GATE_BROADCAST_PORT);
                datagramSocket.send(datagramPacket);

                byte[] readedBytes = new byte[1024];
                DatagramPacket recPacket = new DatagramPacket(readedBytes, readedBytes.length);
                datagramSocket.setSoTimeout(Constants.SYSTEM_SETTINGS.BROADCASE_SERVICE_DISCOVER_SOTIMEOUT);
                datagramSocket.receive(recPacket);
                //解析消息
//                ServiceDiscoverMsg serviceDiscoverMsgResp = null;
//                try {
//                    serviceDiscoverMsgResp = MessageUtils.decomposeServiceDiscoverMsg(readedBytes, readedBytes.length, serviceDiscoverMsg.getMessageID());
//                } catch (Exception e) {
//                    resultCode = Constants.COMMON.EC_MESSAGE_RESOLVE_FAILED;
//                    continue;
//                }
//                Log.i(TAG, String.format("服务发现回应消息 [%s]", serviceDiscoverMsgResp.toString()));
                Log.i(TAG, String.format("服务发现回应消息 [%s]", new String(readedBytes)));

                InetAddress gateAddr = recPacket.getAddress();
                String gateIP = gateAddr.getHostAddress();

                resultCode = 0;
                resultData.putString(Constants.KEYS_PARAMS.GATE_STA_IP, gateIP);

                datagramSocket.close();
                break;
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
        }

        receiver.send(resultCode, resultData);
    }

}
