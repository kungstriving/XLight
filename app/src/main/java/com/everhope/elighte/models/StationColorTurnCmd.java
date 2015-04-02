package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;

/**
 * 站点颜色调节子命令
 * Created by kongxiaoyang on 2015/3/21.
 */
public class StationColorTurnCmd extends StationSubCmd {
    public StationColorTurnCmd() {
        setSubFunctionCode(FunctionCodes.SubFunctionCodes.COLOR_TURN);
    }

    private byte h;
    private byte s;
    private byte b;

    public byte getH() {
        return h;
    }

    public void setH(byte h) {
        this.h = h;
    }

    public byte getB() {
        return b;
    }

    public void setB(byte b) {
        this.b = b;
    }

    public byte getS() {
        return s;
    }

    public void setS(byte s) {
        this.s = s;
    }
}
