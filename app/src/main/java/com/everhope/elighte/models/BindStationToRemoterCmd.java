package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;

/**
 * 绑定站点到遥控器
 * Created by kongxiaoyang on 2015/4/17.
 */
public class BindStationToRemoterCmd extends StationSubCmd {

    private byte controlNum;

    private short stationID;

    public BindStationToRemoterCmd() {
        setSubFunctionCode(FunctionCodes.SubFunctionCodes.BIND_REMOTER);
    }

    public byte getControlNum() {
        return controlNum;
    }

    public void setControlNum(byte controlNum) {
        this.controlNum = controlNum;
    }

    public short getStationID() {
        return stationID;
    }

    public void setStationID(short stationID) {
        this.stationID = stationID;
    }
}
