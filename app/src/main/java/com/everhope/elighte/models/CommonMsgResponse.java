package com.everhope.elighte.models;

import android.util.Log;

import com.everhope.elighte.helpers.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 通用返回消息
 * Created by kongxiaoyang on 2015/3/2.
 */
public class CommonMsgResponse extends Message {

    private static final String TAG = "CommonMsgResponse@Light";

    public static short RETURN_CODE_OK = 0x0000;
    public static short RETURN_CODE_FAIL = 0x1000;

    private short returnCode;

    public CommonMsgResponse(byte[] bytes, short idShould) throws Exception {
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

        if (messageSign != MessageUtils.messageSign || messageID != idShould) {
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
    }

    public short getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(short returnCode) {
        this.returnCode = returnCode;
    }
}
