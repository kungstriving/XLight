package com.everhope.xlight;

import android.app.Application;

import com.everhope.xlight.app.AppContext;
import com.everhope.xlight.comm.DataAgent;

/**
 * 系统应用类
 * Created by kongxiaoyang on 2015/1/9.
 */
public class XLightApplication extends Application{

    private final static String TAG = "XLightApplication";

    private static XLightApplication xLightApplication = null;

    private DataAgent dataAgent = null;
    private AppContext appContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        XLightApplication.xLightApplication = this;
        dataAgent = DataAgent.getSingleInstance();
        appContext = new AppContext();
    }

    public DataAgent getDataAgent() {
        return this.dataAgent;
    }
    public static XLightApplication getInstance() {
        return xLightApplication;
    }

}
