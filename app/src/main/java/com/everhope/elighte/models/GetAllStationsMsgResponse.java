package com.everhope.elighte.models;

import com.everhope.elighte.constants.StationTypes;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * 获取所有灯列表 回应消息
 * Created by kongxiaoyang on 2015/2/25.
 */
public class GetAllStationsMsgResponse extends Message{

    private short stationsCount;

    private StationObject[] stationObjects;

    public GetAllStationsMsgResponse(byte[] bytes) {
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

        stationObjects = new StationObject[stationCount];
        //循环读取所有站点信息
        for(int i = 0; i < stationCount; i++) {
            short stationID = byteBuffer.getShort();
            short stationType = byteBuffer.getShort();
            byte[] macBytes = new byte[8];
            byteBuffer.get(macBytes);
            String stationMAC = new String(Hex.encodeHex(macBytes));

            StationObject stationObject = new StationObject();
            stationObject.setId(stationID);
            stationObject.setStationTypes(StationTypes.fromStationTypeShort(stationType));
            stationObject.setMac(stationMAC);
            stationObjects[i] = stationObject;
        }
    }

    public short getStationsCount() {
        return stationsCount;
    }

    public void setStationsCount(short stationsCount) {
        this.stationsCount = stationsCount;
    }

    public StationObject[] getStationObjects() {
        return stationObjects;
    }

    public void setStationObjects(StationObject[] stationObjects) {
        this.stationObjects = stationObjects;
    }
}
