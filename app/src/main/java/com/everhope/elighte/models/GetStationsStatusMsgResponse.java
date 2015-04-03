package com.everhope.elighte.models;

import android.util.Log;

import com.everhope.elighte.helpers.MessageUtils;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量站点遥信回应消息
 * Created by kongxiaoyang on 2015/3/26.
 */
public class GetStationsStatusMsgResponse extends Message {

    private static final String TAG = "GetStationsStatusMsgResponse@Light";
    private Map<Short, List<StationSubCmd>> map = new HashMap<>();


    public GetStationsStatusMsgResponse(byte[] bytes, short idShould) throws Exception {
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

        if (messageSign != MessageUtils.messageSign || messageID != idShould) {
            Log.w(TAG, String.format("收到消息特征码[%s] 发送消息特征码[%s] 收到消息ID[%s] 发送消息ID[%s]",
                    messageSign + "",
                    MessageUtils.messageSign + "",
                    messageID + "",
                    idShould + ""));
            throw new Exception("Message id or sign not match");
        }

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
        //获取返回站点数目
        short stationCount = byteBuffer.getShort();
        for(short i = 0; i < stationCount; i++) {
            //取站点ID
            short stationID = byteBuffer.getShort();
            //取该站点子命令个数
            short subCmdCount = byteBuffer.getShort();
            List<StationSubCmd> list = new ArrayList<>();
            for(short j = 0; j < subCmdCount; j++) {
                //取站点子命令
                byte[] stationSubCmdBytes = new byte[4];
                byteBuffer.get(stationSubCmdBytes);
                StationSubCmd stationSubCmd = StationSubCmd.getStationSubCmdFromBytes(stationSubCmdBytes);
                if (stationSubCmd != null) {
                    list.add(stationSubCmd);
                }
            }
            this.map.put(stationID, list);
        }
    }

    public Map<Short, List<StationSubCmd>> getMap() {
        return map;
    }

    public void setMap(Map<Short, List<StationSubCmd>> map) {
        this.map = map;
    }
}
