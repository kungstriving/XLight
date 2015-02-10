package com.everhope.elighte.constants;

/**
 * 功能码
 *
 * Created by kongxiaoyang on 2015/1/25.
 */
public class FunctionCodes {

    /**
     * 0x00	智能网关服务发现
     * 0x01	登录
     * 0x02	退出登录
     * 0x03	心跳报文
     * 0x04	通知路由器米用户密码
     *
     * 0x10	向网关发送设备管理文件
     * 0x11	获取网关设备管理文件
     *
     * 0x20	搜索新站点设备
     * 0x21	删除所有站点设备
     *
     * 0x30	列表所有站点信息
     *
     */
    public static enum Gate {

        SERVICE_DISCOVER(Byte.decode("0x00")),
        LOGIN(Byte.decode("0x01")),
        LOGOUT(Byte.decode("0x02")),
        HEARTBEAT(Byte.decode("0x03")),
        SET_UN_PW(Byte.decode("0x04")),

        SEND_DEV_FILES(Byte.decode("0x10")),
        GET_DEV_FILES(Byte.decode("0x11")),

        SEARCH_NEW_STATIONS(Byte.decode("0x20")),
        REMOVE_ALL_STATIONS(Byte.decode("0x21")),

        LIST_ALL_STATIONS(Byte.decode("0x30"));

        private byte funcCode;

        private Gate(byte code) {
            this.funcCode = code;
        }

        public byte getFuncCode() {
            return this.funcCode;
        }
    }
}
