package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 搜索站点消息
 * Created by kongxiaoyang on 2015/4/2.
 */
public class SearchStationsMsg extends Message {

    private byte lastSeconds = 30;

    public SearchStationsMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //设置消息特征码
        setMessageSignature(MessageUtils.messageSign);

        //设置对象类型
        setObjectType(MessageObjectTypes.GATE.getObjectType());

        //设置功能码
        setFunctionCode(FunctionCodes.Gate.SEARCH_NEW_STATIONS.getFuncCode());

        //设置对象ID
        short gateID = 0;
        setObjectID(gateID);

        ////////////////////////// 设置数据域 /////////////////////////////////

        byte[] bytes = new byte[2];
        bytes[0] = lastSeconds;
        bytes[1] = lastSeconds;
        setData(bytes);
        //////////////////////////////////////////////////////////////////////

        short crc = 0;
        setCrc(crc);

        short length = (short)(18 + 2);      //18(消息固定长度)+（数据域长度）
        setMessageLength(length);
    }

    public byte getLastSeconds() {
        return lastSeconds;
    }

    public void setLastSeconds(byte lastSeconds) {
        this.lastSeconds = lastSeconds;
    }
}
