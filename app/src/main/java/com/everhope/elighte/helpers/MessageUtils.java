package com.everhope.elighte.helpers;

import android.util.Log;

import com.everhope.elighte.models.ClientLoginMsg;
import com.everhope.elighte.models.ClientLoginMsgResponse;
import com.everhope.elighte.models.CommonMsgResponse;
import com.everhope.elighte.models.EnterStationIdentifyMsg;
import com.everhope.elighte.models.ExitStationIdentifyMsg;
import com.everhope.elighte.models.GetAllLightsStatusMsg;
import com.everhope.elighte.models.GetAllLightsStatusMsgResponse;
import com.everhope.elighte.models.GetAllStationsMsg;
import com.everhope.elighte.models.GetAllStationsMsgResponse;
import com.everhope.elighte.models.GetStationsStatusMsg;
import com.everhope.elighte.models.GetStationsStatusMsgResponse;
import com.everhope.elighte.models.MultiStationBrightControlMsg;
import com.everhope.elighte.models.ServiceDiscoverMsg;
import com.everhope.elighte.models.SetGateNetworkMsg;
import com.everhope.elighte.models.SetGateNetworkMsgResponse;
import com.everhope.elighte.models.StationColorControlMsg;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.math.RandomUtils;

/**
 * 消息工具类
 *
 * 负责生成和解析用于与网关通信的消息内容
 *
 * Created by kongxiaoyang on 2015/1/10.
 */
public class MessageUtils {
    private static final String TAG = "MessageUtils@Light";

    //报文特征码
    public static short messageSign = 0;

    ////////////////////////////////////// 消息生成与解析 /////////////////////////////

    /**
     * 组织生成 退出站点识别 消息
     * @param objectID
     * @return
     */
    public static ExitStationIdentifyMsg composeExitStationIdentifyMsg(short objectID) {
        ExitStationIdentifyMsg exitStationIdentifyMsg = new ExitStationIdentifyMsg();
        exitStationIdentifyMsg.setObjectID(objectID);
        exitStationIdentifyMsg.buildUp();

        return exitStationIdentifyMsg;
    }

    /**
     * 解析 退出站点识别的返回消息
     * @param data
     * @param length
     * @return
     */
    public static CommonMsgResponse decomposeExitStationIdReturnMsg(byte[] data, int length) {
        CommonMsgResponse commonMsgResponse = new CommonMsgResponse(data);
        return commonMsgResponse;
    }

    /**
     * 生成服务发现报文
     * @return
     */
    public static ServiceDiscoverMsg composeServiceDiscoverMsg() {

        ServiceDiscoverMsg serviceDiscoverMsg = new ServiceDiscoverMsg();
        serviceDiscoverMsg.buildUp();
        Log.i(TAG, String.format("服务发现发送消息 [%s]", serviceDiscoverMsg.toString()));
        return serviceDiscoverMsg;

        /*
        服务发现
fe fe fe 7e 22 00 00 00 00 00 00 80 00 00 00 00 48 6f 6d 65 20 67 61 74 65 77 61 79 00 00 00 00 13 7f
         */

//        String bytesArr = "fefefe7e220000000000008000000000486f6d65206761746577617900000000137f";
//        byte[] bytes = getByteArrayFromString(bytesArr);
//
//        return bytes;
    }

    public static GetStationsStatusMsg composeGetStationsStatusMsg(short[] ids) {
        GetStationsStatusMsg getStationsStatusMsg = new GetStationsStatusMsg();
        getStationsStatusMsg.setOpIDs(ids);

        getStationsStatusMsg.buildUp();

        return getStationsStatusMsg;
    }

    public static GetStationsStatusMsgResponse decomposeGetStationsStatusMsgResponse(byte[] data, int length) {
        GetStationsStatusMsgResponse getStationsStatusMsgResponse = new GetStationsStatusMsgResponse(data);
        return getStationsStatusMsgResponse;
    }

    /**
     * 解析服务发现回应报文
     * @param data
     * @param count
     * @return
     */
    public static ServiceDiscoverMsg decomposeServiceDiscoverMsg(byte[] data, int count, short idShould) throws Exception {
        ServiceDiscoverMsg serviceDiscoverMsg = new ServiceDiscoverMsg(data, idShould);
        return serviceDiscoverMsg;
    }

    /**
     * 组织生成登录消息报文
     * @param clientID
     * @return
     */
    public static ClientLoginMsg composeLogonMsg(String clientID) {
        ClientLoginMsg clientLoginMsg = new ClientLoginMsg();
        clientLoginMsg.setClientID(clientID);
        clientLoginMsg.buildUp();
        return clientLoginMsg;
    }

    /**
     * 解析登录回应报文
     * @param data
     * @param count
     * @return
     */
    public static ClientLoginMsgResponse decomposeLogonReturnMsg(byte[] data, int count) throws Exception {
        ClientLoginMsgResponse clientLoginMsgResponse = new ClientLoginMsgResponse(data);
        return clientLoginMsgResponse;
    }

    /**
     * 组织生成设置网关wifi信息 报文
     * @param ssid
     * @param pwd
     * @return
     */
    public static SetGateNetworkMsg composeSetGateMsg(String ssid,String securityType, String pwd) {
        SetGateNetworkMsg setGateNetworkMsg = new SetGateNetworkMsg();
        setGateNetworkMsg.setSsid(ssid);
        setGateNetworkMsg.setSecurityType(securityType);
        setGateNetworkMsg.setPwd(pwd);

        setGateNetworkMsg.buildUp();

        return setGateNetworkMsg;
    }

    /**
     * 解析设置网关wifi信息回应报文
     * @param data
     * @param length
     * @return
     */
    public static SetGateNetworkMsgResponse decomposeSetGateReturnMsg(byte[] data, int length) {
        SetGateNetworkMsgResponse setGateNetworkMsgResponse = new SetGateNetworkMsgResponse(data);
        return setGateNetworkMsgResponse;
    }

    /**
     * 组织生成进入站点识别消息
     * @param objectID
     * @return
     */
    public static EnterStationIdentifyMsg composeEnterStationIdentifyMsg(short objectID) {
        EnterStationIdentifyMsg enterStationIdentifyMsg = new EnterStationIdentifyMsg();
        enterStationIdentifyMsg.setObjectID(objectID);
        enterStationIdentifyMsg.buildUp();

        return enterStationIdentifyMsg;
    }

    /**
     * 解析进入站点识别的返回消息
     * @param data
     * @param length
     * @return
     */
    public static CommonMsgResponse decomposeEnterStationIdReturnMsg(byte[] data, int length) {
        CommonMsgResponse commonMsgResponse = new CommonMsgResponse(data);
        return commonMsgResponse;
    }

    /**
     * 组织生成获取所有站点列表报文
     * @return
     */
    public static GetAllStationsMsg composeGetAllStationsMsg() {
        GetAllStationsMsg getAllLightsMsg = new GetAllStationsMsg();
        getAllLightsMsg.buildUp();

        return getAllLightsMsg;
    }

    /**
     * 解析获取所有站点列表回应报文
     * @param data
     * @param length
     * @return
     */
    public static GetAllStationsMsgResponse decomposeGetAllStationsMsgResponse(byte[] data, int length, short idShould) throws Exception {
        GetAllStationsMsgResponse getAllStationsMsgResponse = new GetAllStationsMsgResponse(data, idShould);
        return getAllStationsMsgResponse;
    }

    /**
     * 获取所有灯状态消息
     *
     * @return
     */
    public static GetAllLightsStatusMsg composeGetAllLightsStatusMsg() {
        GetAllLightsStatusMsg getAllLightsStatusMsg = new GetAllLightsStatusMsg();
        getAllLightsStatusMsg.buildUp();
        return getAllLightsStatusMsg;
    }

    public static CommonMsgResponse decomposeMultiStationBrightControlResponse(byte[] data,int length) {
        CommonMsgResponse commonMsgResponse = new CommonMsgResponse(data);
        return commonMsgResponse;
    }

    public static MultiStationBrightControlMsg composeMultiStationBrightControlMsg(short[] ids, byte[] brightArr) {
        MultiStationBrightControlMsg multiStationBrightControlMsg = new MultiStationBrightControlMsg();
        multiStationBrightControlMsg.setOpIDs(ids);
        multiStationBrightControlMsg.setBrightArr(brightArr);

        multiStationBrightControlMsg.buildUp();
        return multiStationBrightControlMsg;
    }

    /**
     * 组织生成调节站点颜色消息
     * @param stationID
     * @param hsb
     * @return
     */
    public static StationColorControlMsg composeStationColorControlMsg(short stationID, byte[] hsb) {
        StationColorControlMsg stationColorControlMsg = new StationColorControlMsg();
        stationColorControlMsg.setObjectID(stationID);
        stationColorControlMsg.setH(hsb[0]);
        stationColorControlMsg.setS(hsb[1]);
        stationColorControlMsg.setB(hsb[2]);

        stationColorControlMsg.buildUp();

        return stationColorControlMsg;
    }

    /**
     * 解析所有灯状态消息
     * @param data
     * @param length
     * @return
     */
    public static GetAllLightsStatusMsgResponse decomposeGetAllLightsStatusResponse(byte[] data, int length) {
        GetAllLightsStatusMsgResponse getAllLightsStatusMsgResponse = new GetAllLightsStatusMsgResponse(data);
        return getAllLightsStatusMsgResponse;
    }

    /**
     * 解析 调节站点颜色返回消息
     * @param data
     * @param length
     * @return
     */
    public static CommonMsgResponse decomposeStationColorControlMsg(byte[]data, int length) {
        CommonMsgResponse commonMsgResponse = new CommonMsgResponse(data);
        return commonMsgResponse;
    }

    ////////////////////////////////////// 通用方法 ////////////////////////////////////
    public static short getRandomMessageID() {
        //0~3000 作为随机ID
        return (short)RandomUtils.nextInt(3000);
    }

    //////////////////////////////////// 测试工具方法 ///////////[//////////////////////

    /**
     * 返回null出错
     * @param stringBytes
     * @return
     */
    private static byte[] getByteArrayFromString(String stringBytes) {
        try {
            return Hex.decodeHex(stringBytes.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
            Log.w(TAG, e.getMessage());
        }

        return null;
    }
}
