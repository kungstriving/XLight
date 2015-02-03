package com.everhope.xlight.models;

import android.util.Log;

import com.everhope.xlight.constants.FunctionCodes;
import com.everhope.xlight.constants.MessageObjectTypes;
import com.everhope.xlight.helpers.MessageUtils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.Arrays;

/**
 * Created by kongxiaoyang on 2015/2/2.
 */
public class ClientLoginMsg extends Message{
    private static final String TAG = "ClientLoginMsg@Light";

    private String clientID = "";

    public ClientLoginMsg() {}
    public ClientLoginMsg(byte[] bytes) {}

    @Override
    public void buildUp() {
        super.buildUp();

        //set sign
        short sign = 0;     //登录消息 暂时还没有签名字段
        setMessageSignature(sign);

        //object type
        setObjectType(MessageObjectTypes.GATE.getObjectType());
        //func code
        setFunctionCode(FunctionCodes.Gate.LOGIN.getFuncCode());
        //object id
        short gateID = 0;
        setObjectID(gateID);

        //set client id
        byte[] tempBytes = new byte[6];
        try {
            tempBytes = Hex.decodeHex(this.clientID.toCharArray());
        } catch (DecoderException e) {
            Log.w(TAG, ExceptionUtils.getFullStackTrace(e));

        }
        byte[] data = new byte[6];
        Arrays.fill(data, Byte.parseByte("0"));
        System.arraycopy(tempBytes,0,data,0,tempBytes.length);
        setData(data);

        short crc = 0;
        setCrc(crc);

        short length = 18 + 6;      //消息固定长度18+6=clientid长度
        setMessageLength(length);
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }
}
