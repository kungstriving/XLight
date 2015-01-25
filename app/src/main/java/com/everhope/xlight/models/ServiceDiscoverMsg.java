package com.everhope.xlight.models;

import com.everhope.xlight.constants.FunctionCodes;
import com.everhope.xlight.constants.MessageObjectTypes;
import com.everhope.xlight.helpers.MessageUtils;

/**
 * 服务发现消息定义
 * Created by kongxiaoyang on 2015/1/25.
 */
public class ServiceDiscoverMsg extends Message{
    public ServiceDiscoverMsg() {
        short sign = 0;
        setMessageSignature(sign);
        setMessageID(MessageUtils.getRandomMessageID());
        setAppToGate(true);
        setAck(false);
        setSliceError(false);
        setSliceMore(false);
        setObjectType(MessageObjectTypes.GATE.getObjectType());
        setFunctionCode(FunctionCodes.Gate.SERVICE_DISCOVER.getFuncCode());
        short gateID = 0;
        setObjectID(gateID);
        byte[] data = new byte[0];
        setData(data);

        short crc = 0;
        setCrc(crc);

        short length = 34;
        setMessageLength(length);
    }
}
