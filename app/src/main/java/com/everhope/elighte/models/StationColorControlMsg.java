package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 站点颜色调节消息
 * Created by kongxiaoyang on 2015/2/13.
 */
public class StationColorControlMsg extends Message{

    private byte H;
    private byte S;
    private byte B;

    public StationColorControlMsg() {}

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
        //由外围调用者设置

        ////////////////////////// 设置数据域 /////////////////////////////////
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //设置子命令数目
        short subCmdCount = 2;
        byteBuffer.putShort(subCmdCount);
        //设置颜色调节子命令
        StationColorTurnCmd stationColorTurnCmd = new StationColorTurnCmd();
        stationColorTurnCmd.setH(H);
        stationColorTurnCmd.setS(S);
        stationColorTurnCmd.setB(B);

        byte[] hsbData = new byte[4];
        hsbData[3] = stationColorTurnCmd.getB();
        hsbData[2] = stationColorTurnCmd.getS();
        hsbData[1] = stationColorTurnCmd.getH();
        hsbData[0] = stationColorTurnCmd.getSubFunctionCode().getFuncCode();
        byteBuffer.put(hsbData);

        //设置亮度调节子命令
        StationBrightTurnCmd stationBrightTurnCmd = new StationBrightTurnCmd();
        stationBrightTurnCmd.setBrightValue(hsbData[3]);
        byte[] brightBytes = new byte[4];
        Arrays.fill(brightBytes, (byte)0);
        brightBytes[0] = stationBrightTurnCmd.getSubFunctionCode().getFuncCode();
        brightBytes[3] = stationBrightTurnCmd.getBrightValue();
        byteBuffer.put(brightBytes);

        setData(byteBuffer.array());
        //////////////////////////////////////////////////////////////////////

        short crc = 0;
        setCrc(crc);

        short length = 18 + 10;      //18(消息固定长度)+6（数据域长度）
        setMessageLength(length);
    }

    public byte getB() {
        return B;
    }

    public void setB(byte b) {
        B = b;
    }

    public byte getS() {
        return S;
    }

    public void setS(byte s) {
        S = s;
    }

    public byte getH() {
        return H;
    }

    public void setH(byte h) {
        H = h;
    }
}
