package com.everhope.elighte.models;

import android.util.Log;

import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 登录网关的回应消息
 *
 * Created by kongxiaoyang on 2015/2/2.
 */
public class ClientLoginMsgResponse extends Message{

    private static final String TAG = "ClientLoginMsgResponse@Light";
    
    public static short RETURN_CODE_OK = 0x0000;
    public static short RETURN_CODE_USERNAME_NOEXIST = 0x1000;
    public static short RETURN_CODE_PWD_WRONG = 0x1001;

    /**
     * 命令返回码
     * 0x0000 —— 成功
     * 0x1000 ——用户名不存在
     * 0x1001 ——密码错误
     */
    private short returnCode;
    /**
     * 登录消息返回的特征码
     */
    private short sign;
    private short gateInfoLength;
    private short gateProtoVer;
    private String gatePhysicalAddr;
    private String gateDesc;

    public ClientLoginMsgResponse(byte[] bytes, short idShould) throws Exception{
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        //取出消息头
        byteBuffer.getShort();
        byteBuffer.getShort();
        //消息长度
        short messageLength = byteBuffer.getShort();
        setMessageLength(messageLength);

        //消息特征码
        short messageSign = byteBuffer.getShort();
        setMessageSignature(messageSign);

        //报文序号
        short messageID = byteBuffer.getShort();
        setMessageID(messageID);

        if (idShould != messageID) {
            Log.w(TAG, String.format("收到消息特征码[%s] 发送消息特征码[%s] 收到消息ID[%s] 发送消息ID[%s]",
                    messageSign + "",
                    MessageUtils.messageSign + "",
                    messageID + "",
                    idShould + ""));
            throw new Exception("Message id or sign not match");
        }

        //报文属性域
        short props = byteBuffer.getShort();
        setPropertiesRegion(props);

        //操作对象类型
        byte objectType = byteBuffer.get();
        setObjectType(objectType);
        //功能码
        byte funcCode = byteBuffer.get();
        setFunctionCode(funcCode);
        //对象ID
        short objectID = byteBuffer.getShort();
        setObjectID(objectID);

        //数据
        byte[] data = new byte[messageLength - 18];
        byteBuffer.get(data);
            //处理数据
        handleData(data);
        //crc
        short crc = byteBuffer.getShort();
        setCrc(crc);
    }

    private void handleData(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //命令返回码
        short returnCode = byteBuffer.getShort();
        setReturnCode(returnCode);
        short sign = byteBuffer.getShort();
        setSign(sign);
        short length = byteBuffer.getShort();
        setGateInfoLength(length);
        //版本号
        short ver = byteBuffer.getShort();
        setGateProtoVer(ver);

        byte[] gateMac = new byte[8];
        byteBuffer.get(gateMac);
//        setGatePhysicalAddr(new String(gateMac));

        setGatePhysicalAddr(AppUtils.getMACString(gateMac));
        byte[] gateDesc = new byte[length - 10];        //-12 or -14
        byteBuffer.get(gateDesc);
        setGateDesc(new String(gateDesc));
    }

    public String getGateDesc() {
        return gateDesc;
    }

    public void setGateDesc(String gateDesc) {
        this.gateDesc = gateDesc;
    }

    public String getGatePhysicalAddr() {
        return gatePhysicalAddr;
    }

    public void setGatePhysicalAddr(String gatePhysicalAddr) {
        this.gatePhysicalAddr = gatePhysicalAddr;
    }

    public short getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(short returnCode) {
        this.returnCode = returnCode;
    }

    /**
     * 获取消息返回的特征码
     * @return
     */
    public short getSign() {
        return sign;
    }

    public void setSign(short sign) {
        this.sign = sign;
    }

    public short getGateInfoLength() {
        return gateInfoLength;
    }

    public void setGateInfoLength(short gateInfoLength) {
        this.gateInfoLength = gateInfoLength;
    }

    public String getGateProtoVer() {
        StringBuilder verBuilder = new StringBuilder();
        short ver = this.gateProtoVer;

        short temp = (short)(ver & 0x0100);
        temp = (short)(temp >> 8);
        verBuilder.append(temp).append(".");
        temp = (short)(ver & 0x0010);
        temp = (short)(temp >> 4);
        verBuilder.append(temp).append(".");
        temp = (short)(ver & 0x0001);
        verBuilder.append(temp);

        return verBuilder.toString();
    }

    public void setGateProtoVer(short gateProtoVer) {
        this.gateProtoVer = gateProtoVer;
    }
}
