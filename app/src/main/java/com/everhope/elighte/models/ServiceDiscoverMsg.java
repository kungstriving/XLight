package com.everhope.elighte.models;

import android.util.Log;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 服务发现消息定义
 * Created by kongxiaoyang on 2015/1/25.
 */
public class ServiceDiscoverMsg extends Message{

    private static final String TAG = "ServiceDiscoverMsg@Light";

    public ServiceDiscoverMsg() {}

    /**
     * 通过字节数组构造消息体
     * @param msgBytes
     */
    public ServiceDiscoverMsg(byte[] msgBytes, short idShould) throws Exception {
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
        if (messageID != idShould) {
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
