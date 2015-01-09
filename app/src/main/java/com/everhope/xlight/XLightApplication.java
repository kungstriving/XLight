package com.everhope.xlight;

import android.app.Application;

import com.everhope.xlight.comm.DataAgent;

/**
 * Created by kongxiaoyang on 2015/1/9.
 */
public class XLightApplication extends Application{

    private final static String TAG = "XLightApplication";

    private static XLightApplication xLightApplication = null;

    private DataAgent dataAgent = null;

    @Override
    public void onCreate() {
        super.onCreate();
        XLightApplication.xLightApplication = this;
        dataAgent = DataAgent.getSingleInstance();
    }

    public DataAgent getDataAgent() {
        return this.dataAgent;
    }
    public static XLightApplication getInstance() {
        return xLightApplication;
    }

}
