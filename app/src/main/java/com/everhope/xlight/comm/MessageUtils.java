package com.everhope.xlight.comm;

/**
 * 消息工具类
 *
 * 负责生成和解析用于与网关通信的消息内容
 *
 * Created by kongxiaoyang on 2015/1/10.
 */
public class MessageUtils {
    private static final String TAG = "MessageUtils";

    public static byte[] composeLogonMsg(String clientID) {
        byte[] bytes = new byte[1024];
        return bytes;
    }

    public static LogonResponseMsg decomposeLogonReturnMsg(byte[] data, int count) {
        return new LogonResponseMsg();
    }
}
