package com.everhope.elighte.models;

import android.util.Log;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 设置网关的网络连接消息
 * Created by kongxiaoyang on 2015/2/24.
 */
public class SetGateNetworkMsg extends Message{
    private static final String TAG = "SetGateNetworkMsg@Light";

    private String ssid = "";
    private String securityType = "";
    private String pwd = "";

    public SetGateNetworkMsg() {}
    public SetGateNetworkMsg(byte[] bytes) {}

    @Override
    public void buildUp() {
        super.buildUp();

        //set sign
        short sign = MessageUtils.messageSign;
        setMessageSignature(sign);

        //object type
        setObjectType(MessageObjectTypes.GATE.getObjectType());
        //func code
        setFunctionCode(FunctionCodes.Gate.SET_UN_PW.getFuncCode());
        //object id
        short gateID = 0;
        setObjectID(gateID);

        //set data region
        ByteBuffer byteBuffer = ByteBuffer.allocate(160);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //设置网络id
        byte[] ssidBytes = this.ssid.getBytes();
        byte[] ssidData = new byte[32];
        Arrays.fill(ssidData, Byte.parseByte("0"));
        System.arraycopy(ssidBytes, 0, ssidData, 0, ssidBytes.length);
        byteBuffer.put(ssidData);

        //设置认证方式
        byte[] secTypeBytes = this.securityType.getBytes();
        byte[] secTypeData = new byte[32];
        Arrays.fill(secTypeData, Byte.parseByte("0"));
        System.arraycopy(secTypeBytes, 0, secTypeData, 0, secTypeBytes.length);
        byteBuffer.put(secTypeData);

        //设置网络密码
        byte[] pwdBytes = this.pwd.getBytes();
        byte[] pwdData = new byte[64];
        Arrays.fill(pwdData, Byte.parseByte("0"));
        System.arraycopy(pwdBytes,0,pwdData,0,pwdBytes.length);
        byteBuffer.put(pwdData);

        //设置保留域
        byte[] reserveData = new byte[32];
        Arrays.fill(reserveData, Byte.parseByte("0"));
        byteBuffer.put(reserveData);

        //设置整个数据域
        setData(byteBuffer.array());

        short crc = 0;
        setCrc(crc);

        short length = 18 + 160;      //18(消息固定长度)+64（数据域长度）
        setMessageLength(length);
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }
}
