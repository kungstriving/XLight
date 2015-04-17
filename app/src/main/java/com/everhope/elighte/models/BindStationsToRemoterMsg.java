package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 绑定站点到遥控器消息
 * Created by kongxiaoyang on 2015/4/16.
 */
public class BindStationsToRemoterMsg extends Message {

    private byte controlNum;
    private short[] opIDs;

    public BindStationsToRemoterMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //设置消息特征码
        setMessageSignature(MessageUtils.messageSign);

        //设置对象类型
        setObjectType(MessageObjectTypes.STATION.getObjectType());

        //设置功能码
        setFunctionCode(FunctionCodes.RemoteControl.REMOTE_CONTROL_OP.getFuncCode());

        //设置对象ID 外围调用设置
        //setObjectID((short)-2);      //0xFFFE

        ////////////////////////// 设置数据域 /////////////////////////////////
        short stationCount = (short)opIDs.length;
        int dataRegionLength = stationCount*4 + 2;
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataRegionLength);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //设置子命令数目
        byteBuffer.putShort(stationCount);
        for (int i = 0; i < stationCount; i++) {
            //设置绑定站点子命令
            byte[] subCmdBytes = new byte[4];
            subCmdBytes[0] = FunctionCodes.SubFunctionCodes.BIND_REMOTER.getFuncCode();
            subCmdBytes[1] = controlNum;
            ByteBuffer buffer = ByteBuffer.allocate(2);
            buffer.putShort(opIDs[i]);
            byte[] opidBytes = buffer.array();
            subCmdBytes[2] = opidBytes[0];
            subCmdBytes[3] = opidBytes[1];

            byteBuffer.put(subCmdBytes);
        }

        setData(byteBuffer.array());
        //////////////////////////////////////////////////////////////////////

        short crc = 0;
        setCrc(crc);

        short length = (short)(18 + dataRegionLength);      //18(消息固定长度)+（数据域长度）
        setMessageLength(length);
    }

    public byte getControlNum() {
        return controlNum;
    }

    public void setControlNum(byte controlNum) {
        this.controlNum = controlNum;
    }

    public short[] getOpIDs() {
        return opIDs;
    }

    public void setOpIDs(short[] opIDs) {
        this.opIDs = opIDs;
    }
}
