package com.everhope.elighte.comm;

import com.everhope.elighte.constants.LogonRespStatus;

/**
 * 登录网关后的返回消息
 *
 * Created by kongxiaoyang on 2015/1/10.
 */
public class LogonResponseMsg {
    private LogonRespStatus logonRespStatus;

    public LogonRespStatus getLogonRespStatus() {
        return logonRespStatus;
    }

    public void setLogonRespStatus(LogonRespStatus logonRespStatus) {
        this.logonRespStatus = logonRespStatus;
    }
}
