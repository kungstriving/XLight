package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;

/**
 * 站点亮度调节子命令
 * Created by kongxiaoyang on 2015/3/21.
 */
public class StationBrightTurnCmd extends StationSubCmd {
    public StationBrightTurnCmd() {
        setSubFunctionCode(FunctionCodes.SubFunctionCodes.BRIGHTNESS_TURN);
    }

    private byte brightValue = 0;

    public byte getBrightValue() {
        return brightValue;
    }

    public void setBrightValue(byte brightValue) {
        this.brightValue = brightValue;
    }
}
