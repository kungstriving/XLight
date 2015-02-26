package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 获取所有灯列表消息
 * Created by kongxiaoyang on 2015/2/25.
 */
public class GetAllStationsMsg extends Message {
    private static final String TAG = "GetAllLightsMsg@Light";

    public GetAllStationsMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //set sign
        short sign = MessageUtils.messageSign;
        setMessageSignature(sign);

        //object type
        setObjectType(MessageObjectTypes.GATE.getObjectType());
        //func code
        setFunctionCode(FunctionCodes.Gate.LIST_ALL_STATIONS.getFuncCode());
        //object id
        short gateID = 0;
        setObjectID(gateID);

        ////////////////////////// set data region /////////////////////////////
        setData(new byte[0]);
        ////////////////////////////////////////////////////////////////////////

        short crc = 0;
        setCrc(crc);

        short length = 18 + 0;      //18(消息固定长度)+64（数据域长度）
        setMessageLength(length);
    }
}
