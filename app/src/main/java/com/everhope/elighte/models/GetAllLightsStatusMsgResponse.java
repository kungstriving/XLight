package com.everhope.elighte.models;

import com.everhope.elighte.constants.StationTypes;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取所有灯状态回应消息
 * Created by kongxiaoyang on 2015/3/21.
 */
public class GetAllLightsStatusMsgResponse extends Message{

    private short stationsCount;

    private StationStatusObject[] stationStatusObjects;

    public GetAllLightsStatusMsgResponse(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        //取出消息头
        byteBuffer.getShort();
        byteBuffer.getShort();
        //消息长度
        short messageLength = byteBuffer.getShort();
        setMessageLength(messageLength);
        //消息特征码
        short messageSign = byteBuffer.getShort();
        setMessageSignature(messageSign);
        //报文序号
        short messageID = byteBuffer.getShort();
        setMessageID(messageID);
        //报文属性域
        short props = byteBuffer.getShort();
        setPropertiesRegion(props);

        //操作对象类型
        byte objectType = byteBuffer.get();
        setObjectType(objectType);
        //功能码
        byte funcCode = byteBuffer.get();
        setFunctionCode(funcCode);
        //对象ID
        short objectID = byteBuffer.getShort();
        setObjectID(objectID);

        //数据
        byte[] data = new byte[messageLength - 18];
        byteBuffer.get(data);
        //处理数据
        handleData(data);
        //crc
        short crc = byteBuffer.getShort();
        setCrc(crc);
    }

    private void handleData(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //站点个数 如果为0 则结束直接返回
        short stationCount = byteBuffer.getShort();
        setStationsCount(stationCount);
        if (stationCount == 0) {
            return;
        }

        stationStatusObjects = new StationStatusObject[stationCount];
        //循环读取所有站点信息
        for(int i = 0; i < stationCount; i++) {

            StationStatusObject stationStatusObject = new StationStatusObject();
            List<StationSubCmd> listSubCmd = new ArrayList<>();

            short stationID = byteBuffer.getShort();
            //站点子命令数目
            short stationSubCmdCount = byteBuffer.getShort();

            for(int j = 0; j < stationSubCmdCount; j++) {
                byte[] stationSubCmdBytes = new byte[4];
                byteBuffer.get(stationSubCmdBytes);

                StationSubCmd stationSubCmd = StationSubCmd.getStationSubCmdFromBytes(stationSubCmdBytes);
                listSubCmd.add(stationSubCmd);
            }

            stationStatusObject.setId(stationID);
            stationStatusObject.setSubCmdCount(stationSubCmdCount);
            stationStatusObject.setStationSubCmdList(listSubCmd);

            stationStatusObjects[i] = stationStatusObject;
        }
    }

    public short getStationsCount() {
        return stationsCount;
    }

    public void setStationsCount(short stationsCount) {
        this.stationsCount = stationsCount;
    }

    public StationStatusObject[] getStationStatusObjects() {
        return stationStatusObjects;
    }

    public void setStationObjects(StationStatusObject[] stationObjects) {
        this.stationStatusObjects = stationObjects;
    }
}
