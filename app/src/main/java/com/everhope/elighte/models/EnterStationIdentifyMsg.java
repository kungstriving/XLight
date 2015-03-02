package com.everhope.elighte.models;

import android.graphics.Color;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

/**
 * 进入站点识别状态
 * Created by kongxiaoyang on 2015/3/2.
 */
public class EnterStationIdentifyMsg extends Message{
    private static final String TAG = "EnterStationIdentifyMsg@Light";

    public EnterStationIdentifyMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //set sign
        short sign = MessageUtils.messageSign;
        setMessageSignature(sign);

        //object type
        setObjectType(MessageObjectTypes.STATION.getObjectType());
        //func code
        setFunctionCode(FunctionCodes.Station.ENTER_STATION_IDENTIFY.getFuncCode());
        //object id
//        short gateID = 0;
//        setObjectID(gateID);

        ////////////////////////// set data region /////////////////////////////
        setData(new byte[0]);
        ////////////////////////////////////////////////////////////////////////

        short crc = 0;
        setCrc(crc);

        short length = 18 + 0;      //18(消息固定长度)+64（数据域长度）
        setMessageLength(length);
    }
}
