package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;
import com.everhope.elighte.constants.MessageObjectTypes;
import com.everhope.elighte.helpers.MessageUtils;

/**
 * Created by kongxiaoyang on 2015/2/13.
 */
public class StationColorControlMsg extends Message{
    private static final String TAG = "StationColorControlMsg@Light";

    //TODO 对象ID目前无法获取 需要首先获取当前所有灯设备 再来构造拓扑结构和场景、分组等信息
    private short R;
    private short G;
    private short B;

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


    }

    public short getR() {
        return R;
    }

    public void setR(short r) {
        R = r;
    }

    public short getG() {
        return G;
    }

    public void setG(short g) {
        G = g;
    }

    public short getB() {
        return B;
    }

    public void setB(short b) {
        B = b;
    }
}
