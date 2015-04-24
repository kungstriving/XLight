package com.everhope.elighte.constants;

/**
 * 功能码
 *
 * Created by kongxiaoyang on 2015/1/25.
 */
public class FunctionCodes {

    /**
     * 站点
     */
    public static enum Station {
        //加入站点操作
        ADD_STATION(Byte.decode("0x40")),

        //删除站点
        DEL_STATION(Byte.decode("0x41")),
        //进入站点识别
        ENTER_STATION_IDENTIFY(Byte.decode("0x42")),
        //退出站点识别
        EXIT_STATION_IDENTIFY(Byte.decode("0x43"));

        private byte funcCode;

        private Station(byte code) {
            this.funcCode = code;
        }

        public byte getFuncCode() {
            return this.funcCode;
        }
    }

    /**
     * 遥控功能码
     */
    public static enum RemoteControl {
        REMOTE_CONTROL_OP((byte)-128),

        //////////////////////// 子功能码

        /**
         * 设备开关
         */
        DEVICE_ONOFF(Byte.decode("0x01")),

        /**
         * 开关绑定站点
         */
        SWITCH_BIND_STATION(Byte.decode("0x10")),

        /**
         * 开关解绑定站点
         */
        SWITCH_UNBIND_STATION(Byte.decode("0x11")),

        /**
         * 遥控器绑定站点
         */
        REMOTER_BIND_STATION(Byte.decode("0x20")),

        /**
         * 遥控器解绑定站点
         */
        REMOTER_UNBIND_STATION(Byte.decode("0x21"));

        private byte funcCode;

        private RemoteControl(byte code) {
            this.funcCode = code;
        }

        public byte getFuncCode() {
            return this.funcCode;
        }
    }

    /**
     * 遥信功能码
     */
    public static enum RemoteSignal {
        //遥信
        REMOTE_SIGNAL_OP((byte)-80),

        //////////////////// 子命令码

        /**
         * 设备状态
         */
        DEVICE_STATUS((byte)-128),
        /**
         * 设备开关
         */
        DEVICE_SWITCH(Byte.decode("0x01")),

        /**
         * 亮度调节
         */
        BRIGHTNESS_TURN(Byte.decode("0x30")),

        /**
         * 颜色调节
         */
        COLOR_TURN(Byte.decode("0x31"));

        private byte funcCode;

        private RemoteSignal(byte code) {
            this.funcCode = code;
        }

        public byte getFuncCode() {
            return this.funcCode;
        }
    }

    /**
     * 遥调功能码
     */
    public static enum RemoteTurn {
        //遥调 0x90
        SINGLE_STATION_OP((byte)-112),

        /////////////////////////// 子命令码

        //子命令-亮度调节
        BRIGHTNESS_TURN(Byte.decode("0x30")),
        //子命令-颜色调节
        COLOR_TURN(Byte.decode("0x31"));

        private byte funcCode;

        private RemoteTurn(byte code) {
            this.funcCode = code;
        }

        public byte getFuncCode() {
            return this.funcCode;
        }
    }

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

    public static enum SubFunctionCodes {

        /**
         * 设备开关
         */
        DEVICE_SWITCH(Byte.decode("0x01")),

        /**
         * 开关设备绑定
         */
        BIND_SWITCH(Byte.decode("0x10")),

        /**
         * 开关设备解绑定
         */
        UNBIND_SWITCH(Byte.decode("0x11")),

        /**
         * 遥控器设备绑定
         */
        BIND_REMOTER(Byte.decode("0x20")),

        /**
         * 遥控器设备解绑定
         */
        UNBIND_REMOTER(Byte.decode("0x21")),

        /**
         * 亮度调节
         */
        BRIGHTNESS_TURN(Byte.decode("0x30")),

        /**
         * 颜色调节
         */
        COLOR_TURN(Byte.decode("0x31")),

        /**
         * 设备状态
         */
        DEVICE_STATUS((byte)-128),

        /**
         * 错误类型
         */
        WRONG_CODE(Byte.decode("0x00"));

        private byte funcCode;
//        private byte deviceStatusByte = (byte)128;
//        private

        private SubFunctionCodes(byte code) {
            this.funcCode = code;
        }

        public byte getFuncCode() {
            return this.funcCode;
        }

        public static SubFunctionCodes fromSubFunctionCodeByte(byte code) {
            switch (code) {
                case (byte)128:
                    //Byte.decode("0x80")

                    return DEVICE_STATUS;
                case 1:
                    //Byte.decode("0x01")
                    return DEVICE_SWITCH;
                case 48:
                    return BRIGHTNESS_TURN;
                case 49:
                    return COLOR_TURN;
                default:
                    return WRONG_CODE;
            }
        }
    }
}
