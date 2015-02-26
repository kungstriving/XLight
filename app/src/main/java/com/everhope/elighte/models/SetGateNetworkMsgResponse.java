package com.everhope.elighte.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 设置网关连接信息返回消息
 * Created by kongxiaoyang on 2015/2/24.
 */
public class SetGateNetworkMsgResponse extends Message {

    public static short RETURN_CODE_OK = 0x0000;
    public static short RETURN_CODE_CONF_GATE_FAIL = 0x1000;

    private short returnCode;

    public SetGateNetworkMsgResponse(byte[] bytes) {
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
    }

    public short getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(short returnCode) {
        this.returnCode = returnCode;
    }
}
