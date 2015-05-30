package com.everhope.elighte.constants;

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

        /**
         * 消息返回码 0x0000
         */
        public static final short RETURN_CODE_OK = 0x0000;

        /**
         * 网络错误
         */
        public static final int EC_NETWORK_ERROR = 1000;

        /**
         * 未找到sta地址 1001
         */
        public static final int EC_NETWORK_NOFOUND_STA_GATE = 1001;

        /**
         * 网关连接失败 1002
         */
        public static final int EC_NETWORK_CONNET_FAIL = 1002;

        /**
         * 网络未连接 1003
         */
        public static final int EC_NETWORK_NO_CONNECTED = 1003;

        /**
         * 消息解析错误 1004
         */
        public static final int EC_MESSAGE_RESOLVE_FAILED = 1004;
    }
    /**
     * 系统设置
     */
    public static class SYSTEM_SETTINGS {

        public static final String ELIGHTE = "E-Lighte";

        /**
         * 是否已经加载过
         */
        public static final String IS_LOADED_YET = "ss_is_loaded_yet";

        /**
         * 调试开关 在preference.xml 中定义调试开关 必须与其key对应
         */
        public static final String DEBUG = "pref_debug";

        /**
         * 网关mac
         */
        public static final String GATE_MAC = "ss_gate_mac";

        public static final String GATE_VER = "ss_gate_ver";

        public static final String GATE_DESC = "ss_gate_desc";

        ////////////////////////////////////////////////////////////////////////

        /**
         * 网关SSID
         */
        public static final String[] GATE_AP_SSID_ARR =
                new String[] {"USR-WIFI232-S", "USR-WIFI232-T",
                        "USR-WIFI232-G","USR-WIFI232-G2","USR-WIFI232-H"};

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
         * 网关UDP广播端口 8900
         */
//        public static final int GATE_BROADCAST_PORT = 8900;
        public static final int GATE_BROADCAST_PORT = 48899;

        /**
         * 网络接收数据包的缓存大小
         * 协议中设定最大报文长度为530字节
         */
        public static final int NETWORK_PKG_LENGTH = 1024;

        /**
         * 广播服务发现超时时间 8秒
         */
        public static final int BROADCASE_SERVICE_DISCOVER_SOTIMEOUT = 5*1000;

        /**
         * 广播服务发现重试次数
         */
        public static final int BROADCAST_SERVICE_DISCOVER_RETRY_TIMES = 3;

        /**
         * 读取数据超时3秒
         */
        public static final int NETWORK_DATA_SOTIMEOUT = 3000;

        /**
         * 读取超时30秒
         */
        public static final int NETWORK_DATA_LONG_SOTIMEOUT = 20000;

        /**
         * 同步数据间隔15秒
         */
        public static final int SYNC_INTERVAL = 15000;

        /**
         * 建立连接超时 5秒
         */
        public static final int NETWORK_CONNECT_TIMEOUT = 5*1000;

        /**
         * 发送数据出错情况下 重试次数
         */
        public static final int SEND_RETRY_TIMES = 3;

        /**
         * 数据发送间隔毫秒数
         */
        public static final int SEND_RETRY_INTERVAL_MS = 200;

        /**
         * 连接重试次数
         */
        public static final int CONNECT_RETRY_TIMES = 3;

        /**
         * 网关连接重试间隔毫秒数
         */
        public static final int CONNECT_RETRY_INTERVAL_MS = 500;

        /**
         * 搜索新站点的持续时间
         */
        public static final byte SEARCH_STATIONS_LAST_SECONDS = 30;
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

        /**
         * 发送消息的随机id
         */
        public static final String MESSAGE_RANDOM_ID = "kp_msg_randid";
    }

    /**
     * 消息相关
     */
    public static class MESSAGES_CONSTS {
        /**
         * 消息报文头1
         * 0xFE  0xFE
         */
        public static final short MESSAGE_HEAD_1 = -258;

        /**
         * 消息报文头2
         * 0xFE 0x7E
         */
        public static final short MESSAGE_HEAD_2 = 32510;
    }
}
