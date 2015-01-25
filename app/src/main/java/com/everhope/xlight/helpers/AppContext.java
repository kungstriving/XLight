package com.everhope.xlight.helpers;

import com.everhope.xlight.constants.ConnectGateMode;
import com.everhope.xlight.constants.ConnectGateStatus;

/**
 * 系统运行中状态存储
 *
 * Created by kongxiaoyang on 2015/1/10.
 */
public class AppContext {

    private ConnectGateMode connectGateMode;
    private ConnectGateStatus connectGateStatus;

    public ConnectGateMode getConnectGateMode() {
        return connectGateMode;
    }

    public void setConnectGateMode(ConnectGateMode connectGateMode) {
        this.connectGateMode = connectGateMode;
    }

    public ConnectGateStatus getConnectGateStatus() {
        return connectGateStatus;
    }

    public void setConnectGateStatus(ConnectGateStatus connectGateStatus) {
        this.connectGateStatus = connectGateStatus;
    }
}
