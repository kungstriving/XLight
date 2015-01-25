package com.everhope.xlight.constants;

/**
 * 系统的常量类
 *
 * Created by kongxiaoyang on 2015/1/9.
 */
public class Constants {

    public static class COMMON {
        /**
         * 处理错误码 0
         */
        public static final int RESULT_CODE_OK = 0;

        public static final int RESULT_CODE_NETWORK_ERROR = 1000;
    }
    /**
     * 系统设置
     */
    public static class SYSTEM_SETTINGS {
        /**
         * 是否已经加载过
         */
        public static final String IS_LOADED_YET = "ss_is_loaded_yet";

        /**
         * 网关sta地址
         */
        public static final String GATE_STA_IP = "ss_gate_sta_ip";

        /**
         * 网关固定AP地址
         */
        public static final String GATE_AP_IP = "10.10.100.254";

        /**
         * 网关监听端口 8899
         */
        public static final int GATE_TALK_PORT = 8899;
        /**
         * 网络接收数据包的缓存大小
         * 协议中设定最大报文长度为530字节
         */
        public static final int NETWORK_PKG_LENGTH = 1024;

        /**
         * 读取数据超时8秒
         */
        public static final int NETWORK_DATA_SOTIMEOUT = 8000;
    }

    /**
     * 系统各个组件之间传值的key和参数定义
     */
    public static class KEYS_PARAMS {
        /**
         * 网关sta地址
         */
        public static final String GATE_STA_IP = "kp_gate_sta_ip";

        /**
         * 读到的字节数
         */
        public static final String NETWORK_READED_BYTES_COUNT = "kp_bytes_count";

        /**
         * 读到的字节内容
         */
        public static final String NETWORK_READED_BYTES_CONTENT = "kp_bytes_content";
    }

    /**
     * 消息相关
     */
    public static class MESSAGES_CONSTS {
        /**
         * 消息报文头
         * 0xFE  0xFE  0xFE  0x7E
         */
        public static final int MESSAGE_HEAD = -16843138;
    }
}
