package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 多个站点颜色调节消息
 * Created by kongxiaoyang on 2015/4/8.
 */
public class MultiStationColorControlMsg extends Message {

    private short[] opIDs;
    private int[] colorArr;

    public MultiStationColorControlMsg() {}

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
        int dataRegionLength = stationCount*12 + 2;
        ByteBuffer byteBuffer = ByteBuffer.allocate(dataRegionLength);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //设置站点对象数目
        byteBuffer.putShort(stationCount);
        for (int i = 0; i < stationCount; i++) {
            //设置站点ID
            short stationID = opIDs[i];
            byteBuffer.putShort(stationID);
            //设置站点子命令数目
            short stationSubCmdsCount = 2;
            byteBuffer.putShort(stationSubCmdsCount);
            //设置站点颜色调节子命令
            StationColorTurnCmd stationColorTurnCmd = new StationColorTurnCmd();
            int colorInt = colorArr[i];
            byte[] hsb = AppUtils.rgbColorValueToHSB(colorInt);
            stationColorTurnCmd.setH(hsb[0]);
            stationColorTurnCmd.setS(hsb[1]);
            stationColorTurnCmd.setB(hsb[2]);

            byte[] hsbData = new byte[4];
            hsbData[3] = stationColorTurnCmd.getB();
            hsbData[2] = stationColorTurnCmd.getS();
            hsbData[1] = stationColorTurnCmd.getH();
            hsbData[0] = stationColorTurnCmd.getSubFunctionCode().getFuncCode();
            byteBuffer.put(hsbData);
            //设置亮度调节命令
            StationBrightTurnCmd stationBrightTurnCmd = new StationBrightTurnCmd();
            byte[] brightData = new byte[4];
            brightData[3] = stationColorTurnCmd.getB();
            brightData[2] = 0x00;
            brightData[1] = 0x00;
            brightData[0] = stationBrightTurnCmd.getSubFunctionCode().getFuncCode();

            byteBuffer.put(brightData);
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

    public int[] getColorArr() {
        return colorArr;
    }

    public void setColorArr(int[] colorArr) {
        this.colorArr = colorArr;
    }
}
