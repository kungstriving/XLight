package com.everhope.xlight.comm;

import android.content.Context;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * 数据通信全局操作类
 * 所有数据通信通过该类完成，且该类全系统单例
 *
 * Created by kongxiaoyang on 2015/1/6.
 */
public class DataAgent {

    private static DataAgent dataAgent = null;

    /**
     * 侦测网关在局域网范围内
     * @return
     */
    public void detectGateInLan(Context context, ResultReceiver receiver) {
        UDPReceiveIntentService.startActionListenDetectBack(context, receiver);
        CommIntentService.startActionDetectGate(context);
    }

    public void logonToGate(Context context, ResultReceiver receiver) {

    }

    /**
     * 私有构造函数
     */
    private DataAgent() {

    }

    /**
     * 获取单例数据操作类
     * @return
     */
    public static DataAgent getSingleInstance() {
        if (dataAgent == null) {
            dataAgent = new DataAgent();
        }
        return dataAgent;
    }
}
