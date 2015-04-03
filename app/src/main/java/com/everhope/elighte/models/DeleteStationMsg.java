package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

/**
 * 删除站点
 * Created by kongxiaoyang on 2015/4/3.
 */
public class DeleteStationMsg extends Message {
    private static final String TAG = "DeleteStationMsg@Light";

    public DeleteStationMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //set sign
        short sign = MessageUtils.messageSign;
        setMessageSignature(sign);

        //object type
        setObjectType(MessageObjectTypes.STATION.getObjectType());
        //func code
        setFunctionCode(FunctionCodes.Station.DEL_STATION.getFuncCode());
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
