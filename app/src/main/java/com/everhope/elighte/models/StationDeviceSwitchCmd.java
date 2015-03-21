package com.everhope.elighte.models;

import com.everhope.elighte.constants.FunctionCodes;

/**
 * Created by kongxiaoyang on 2015/3/21.
 */
public class StationDeviceSwitchCmd extends StationSubCmd {
    public StationDeviceSwitchCmd() {
        setSubFunctionCode(FunctionCodes.SubFunctionCodes.DEVICE_SWITCH);
    }

    /**
     * 开关状态
     * true=开
     * false=关
     */
    private boolean switchStatus = false;

    public boolean isSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(boolean switchStatus) {
        this.switchStatus = switchStatus;
    }
}
