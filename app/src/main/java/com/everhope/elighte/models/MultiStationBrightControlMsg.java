package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 多个站点亮度调节消息
 * Created by kongxiaoyang on 2015/3/24.
 */
public class MultiStationBrightControlMsg extends Message {

    private short[] opIDs;
    private byte[] brightArr;

    public MultiStationBrightControlMsg() {}

    @Override
    public void buildUp() {
        super.buildUp();

        //设置消息特征码
        setMessageSignature(MessageUtils.messageSign);

        //设置对象类型
        setObjectType(MessageObjectTypes.STATION.getObjectType());

        //设置功能码
        setFunctionCode(FunctionCodes.RemoteTurn.SINGLE_STATION_OP.getFuncCode());

        //设置对象ID
        setObjectID((short)-2);      //0xFFFE

        ////////////////////////// 设置数据域 /////////////////////////////////
        short stationCount = (short)opIDs.length;
        int dataRegionLength = stationCount*8 + 2;
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataRegionLength);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //设置站点对象数目
        byteBuffer.putShort(stationCount);
        for (int i = 0; i < stationCount; i++) {
            //设置站点ID
            short stationID = opIDs[i];
            byteBuffer.putShort(stationID);
            //设置站点子命令数目
            short stationSubCmdsCount = 1;
            byteBuffer.putShort((short)1);
            //设置站点亮度调节子命令
            StationBrightTurnCmd stationBrightTurnCmd = new StationBrightTurnCmd();
            stationBrightTurnCmd.setBrightValue(brightArr[i]);
            byte[] brightCmdData = new byte[4];
            brightCmdData[3] = stationBrightTurnCmd.getBrightValue();
            brightCmdData[2] = 0;
            brightCmdData[1] = 0;
            brightCmdData[0] = stationBrightTurnCmd.getSubFunctionCode().getFuncCode();

            byteBuffer.put(brightCmdData);
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

    public byte[] getBrightArr() {
        return brightArr;
    }

    public void setBrightArr(byte[] brightArr) {
        this.brightArr = brightArr;
    }
}
