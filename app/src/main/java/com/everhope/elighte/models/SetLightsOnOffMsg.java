package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by kongxiaoyang on 2015/4/30.
 */
public class SetLightsOnOffMsg extends Message {
    private boolean on;
    private short[] opIDs;

    public SetLightsOnOffMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //设置消息特征码
        setMessageSignature(MessageUtils.messageSign);

        //设置对象类型
        setObjectType(MessageObjectTypes.STATION.getObjectType());

        //设置功能码
        setFunctionCode(FunctionCodes.RemoteControl.REMOTE_CONTROL_OP.getFuncCode());

        setObjectID((short)-2);      //0xFFFE

        ////////////////////////// 设置数据域 /////////////////////////////////
        short stationCount = (short)opIDs.length;
        int dataRegionLength = stationCount*8 + 2;
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataRegionLength);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byteBuffer.putShort(stationCount);
        for (int i = 0; i < stationCount; i++) {
            //设置站点ID
            short stationID = opIDs[i];
            byteBuffer.putShort(stationID);
            //设置站点子命令数目
            short stationSubCmdsCount = 1;
            byteBuffer.putShort((short)1);
            //设置站点开关子命令
            StationDeviceSwitchCmd stationDeviceSwitchCmd = new StationDeviceSwitchCmd();
            stationDeviceSwitchCmd.setSwitchStatus(on);
            byte[] switchCmdData = new byte[4];
            switchCmdData[3] = on ? (byte)1 : (byte)0;
            switchCmdData[2] = 0;
            switchCmdData[1] = 0;
            switchCmdData[0] = stationDeviceSwitchCmd.getSubFunctionCode().getFuncCode();

            byteBuffer.put(switchCmdData);
        }

        setData(byteBuffer.array());
        //////////////////////////////////////////////////////////////////////

        short crc = 0;
        setCrc(crc);

        short length = (short)(18 + dataRegionLength);      //18(消息固定长度)+（数据域长度）
        setMessageLength(length);
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public short[] getOpIDs() {
        return opIDs;
    }

    public void setOpIDs(short[] opIDs) {
        this.opIDs = opIDs;
    }
}
