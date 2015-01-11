package com.everhope.xlight.comm;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * 通用结果接收器
 *
 * Created by kongxiaoyang on 2015/1/9.
 */
public class CommonReceiver extends ResultReceiver{

    private Receiver receiver;
    public CommonReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }
    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }
}
