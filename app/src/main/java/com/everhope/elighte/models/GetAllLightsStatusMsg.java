package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

/**
 * 获取所有灯状态消息
 * Created by kongxiaoyang on 2015/3/21.
 */
public class GetAllLightsStatusMsg extends Message{

    private static final String TAG = "GetAllLightsStatusMsg@Light";

    public GetAllLightsStatusMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //set sign
        short sign = MessageUtils.messageSign;
        setMessageSignature(sign);

        //object type
        setObjectType(MessageObjectTypes.STATION.getObjectType());
        //func code
        setFunctionCode(FunctionCodes.RemoteSignal.REMOTE_SIGNAL_OP.getFuncCode());
        //object id
        short allLightsID = Short.decode("0xffff");
        setObjectID(allLightsID);

        ////////////////////////// set data region /////////////////////////////
        setData(new byte[0]);
        ////////////////////////////////////////////////////////////////////////

        short crc = 0;
        setCrc(crc);

        short length = 18 + 0;      //18(消息固定长度)+64（数据域长度）
        setMessageLength(length);
    }
}
