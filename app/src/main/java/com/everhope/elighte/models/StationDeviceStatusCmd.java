package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;

/**
 * 站点设备状态子命令
 * Created by kongxiaoyang on 2015/3/21.
 */
public class StationDeviceStatusCmd extends StationSubCmd{
    public StationDeviceStatusCmd() {
        setSubFunctionCode(FunctionCodes.SubFunctionCodes.DEVICE_STATUS);
    }

    private byte flags;

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }
}
