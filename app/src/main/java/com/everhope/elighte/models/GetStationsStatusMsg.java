package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 获取多个站点状态的消息
 * Created by kongxiaoyang on 2015/3/26.
 */
public class GetStationsStatusMsg extends Message {

    /**
     * 遥信操作的站点对象ID 每次不要超过30个 可以保证回应消息在一个消息中结束
     */
    private short[] opIDs;

    public GetStationsStatusMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //设置消息特征码
        setMessageSignature(MessageUtils.messageSign);

        //设置对象类型
        setObjectType(MessageObjectTypes.STATION.getObjectType());

        //设置功能码
        setFunctionCode(FunctionCodes.RemoteSignal.REMOTE_SIGNAL_OP.getFuncCode());

        //设置对象ID
        setObjectID((short)-2);      //0xFFFE

        ////////////////////////// 设置数据域 /////////////////////////////////
        short stationCount = (short)opIDs.length;
        int dataRegionLength = stationCount*2 + 2;
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataRegionLength);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //设置站点对象数目
        byteBuffer.putShort(stationCount);
        for (int i = 0; i < stationCount; i++) {
            //设置站点ID
            short stationID = opIDs[i];
            byteBuffer.putShort(stationID);
        }

        setData(byteBuffer.array());
        //////////////////////////////////////////////////////////////////////

        short crc = 0;
        setCrc(crc);

        short length = (short)(18 + dataRegionLength);      //18(消息固定长度)+（数据域长度）
        setMessageLength(length);
    }

    public short[] getOpIDs() {
        return opIDs;
    }

    public void setOpIDs(short[] opIDs) {
        this.opIDs = opIDs;
    }
}
