package com.everhope.xlight.helpers;

import android.util.Log;

import com.everhope.xlight.comm.LogonResponseMsg;
import com.everhope.xlight.constants.Constants;
import com.everhope.xlight.models.Message;
import com.everhope.xlight.models.ServiceDiscoverMsg;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.math.RandomUtils;

/**
 * 消息工具类
 *
 * 负责生成和解析用于与网关通信的消息内容
 *
 * Created by kongxiaoyang on 2015/1/10.
 */
public class MessageUtils {
    private static final String TAG = "MessageUtils@Light";

    /**
     * 生成服务发现报文
     * @return
     */
    public static byte[] composeServiceDiscoverMsg() {

        ServiceDiscoverMsg serviceDiscoverMsg = new ServiceDiscoverMsg();
        return serviceDiscoverMsg.toMessageByteArray();

        /*
        服务发现
fe fe fe 7e 22 00 00 00 00 00 00 80 00 00 00 00 48 6f 6d 65 20 67 61 74 65 77 61 79 00 00 00 00 13 7f
         */

//        String bytesArr = "fefefe7e220000000000008000000000486f6d65206761746577617900000000137f";
//        byte[] bytes = getByteArrayFromString(bytesArr);
//
//        return bytes;
    }

    /**
     * 返回null出错
     * @param stringBytes
     * @return
     */
    private static byte[] getByteArrayFromString(String stringBytes) {
        try {
            return Hex.decodeHex(stringBytes.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
            Log.w(TAG, e.getMessage());
        }

        return null;
    }

    public static byte[] composeLogonMsg(String clientID) {
        byte[] bytes = new byte[1024];
        return bytes;
    }

    public static LogonResponseMsg decomposeLogonReturnMsg(byte[] data, int count) {
        return new LogonResponseMsg();
    }

    ////////////////////////////////////// 通用方法 ////////////////////////////////////
    public static short getRandomMessageID() {
        //0~3000 作为随机ID
        return (short)RandomUtils.nextInt(3000);
    }
}
