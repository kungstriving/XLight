package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

/**
 * 退出站点识别
 * Created by kongxiaoyang on 2015/3/3.
 */
public class ExitStationIdentifyMsg extends Message {
    private static final String TAG = "ExitStationIdentifyMsg@Light";

    public ExitStationIdentifyMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //set sign
        short sign = MessageUtils.messageSign;
        setMessageSignature(sign);

        //object type
        setObjectType(MessageObjectTypes.STATION.getObjectType());
        //func code
        setFunctionCode(FunctionCodes.Station.EXIT_STATION_IDENTIFY.getFuncCode());
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
