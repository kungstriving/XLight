package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

/**
 * Created by kongxiaoyang on 2015/4/28.
 */
public class DeleteStationForceMsg extends Message {
    private static final String TAG = "DeleteStationForceMsg@Light";

    public DeleteStationForceMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //set sign
        short sign = MessageUtils.messageSign;
        setMessageSignature(sign);

        //object type
        setObjectType(MessageObjectTypes.STATION.getObjectType());
        //func code
        setFunctionCode(FunctionCodes.Station.DEL_STATION_FORCE.getFuncCode());
        //object id
//        short gateID = 0;
//        setObjectID(gateID);

        ////////////////////////// set data region /////////////////////////////
        setData(new byte[0]);
        ////////////////////////////////////////////////////////////////////////

        short crc = 0;
        setCrc(crc);

        short length = 18 + 0;      //18(消息固定长度)+0（数据域长度）
        setMessageLength(length);
    }
}
