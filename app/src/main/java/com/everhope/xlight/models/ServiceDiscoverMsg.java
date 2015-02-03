package com.everhope.xlight.models;

import com.everhope.xlight.constants.FunctionCodes;
import com.everhope.xlight.constants.MessageObjectTypes;
import com.everhope.xlight.helpers.MessageUtils;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 服务发现消息定义
 * Created by kongxiaoyang on 2015/1/25.
 */
public class ServiceDiscoverMsg extends Message{

    public ServiceDiscoverMsg() {}

    /**
     * 通过字节数组构造消息体
     * @param msgBytes
     */
    public ServiceDiscoverMsg(byte[] msgBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(msgBytes);
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

        //crc
        short crc = byteBuffer.getShort();
        setCrc(crc);
    }

    public void buildUp() {
        short sign = 0;
        setMessageSignature(sign);
        setMessageID(MessageUtils.getRandomMessageID());
        setAppToGate(true);
        setAck(false);
        setSliceError(false);
        setSliceMore(false);
        setSliceMessageID((short)0);
        setObjectType(MessageObjectTypes.GATE.getObjectType());
        setFunctionCode(FunctionCodes.Gate.SERVICE_DISCOVER.getFuncCode());
        short gateID = 0;
        setObjectID(gateID);

        //消息固定data内容 Home gateway
        byte[] tempBytes = "Home gateway".getBytes();
        byte[] data = new byte[16];
        Arrays.fill(data,Byte.parseByte("0"));
        System.arraycopy(tempBytes,0,data,0,tempBytes.length);
        setData(data);

        short crc = 0;
        setCrc(crc);

        short length = 34;
        setMessageLength(length);
    }
}
