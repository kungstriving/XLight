package com.everhope.elighte.comm;

import android.content.Context;
import android.os.ResultReceiver;
import android.util.Log;

import com.everhope.elighte.constants.Constants;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * 数据通信全局操作类
 * 所有数据通信通过该类完成，且该类全系统单例
 *
 * Created by kongxiaoyang on 2015/1/6.
 */
public class DataAgent {

    private static final String TAG = "DataAgent@Light";
    private static DataAgent dataAgent = null;

    private Socket socket = null;
    private String serverAddr = "";
    private int serverPort = 0;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    /**
     * 服务发现
     * @param context
     * @param receiver
     */
    public void serviceDiscover(Context context, ResultReceiver receiver) {
        TCPReceiveIntentService.startActionListenBack(context, receiver);
        CommIntentService.startActionServiceDiscover(context);
    }
    /**
     * 启动网关侦测操作，如果获取到网关地址，则返回bundle中包括地址信息；如果没有，则返回错误码
     * @param context
     * @param receiver
     */
    public void detectGateInLan(Context context, ResultReceiver receiver) {
        //启动网关侦测action
        CommIntentService.startActionDetectGate(context, receiver);
    }

    /**
     * 登录网关
     *
     * @param clientID 登录设备ID
     * @param context 全局上下文
     * @param receiver 接收器
     */
    public void loginInGate(Context context, ResultReceiver receiver, String clientID) {
//        TCPReceiveIntentService.startActionListenBack(context, receiver);
        if (!isConnected()) {
            try {
                //TODO 当前网络重连 如果连接失败 应该跳转到设置页面 进行搜寻网关操作
                reconnect();
            } catch (IOException e) {
                e.printStackTrace();
                Log.w(TAG, ExceptionUtils.getFullStackTrace(e));

            }

        } else {
            CommIntentService.startActionLoginToGate(context, receiver, clientID);
        }

    }

    /**
     * 设置网关网络信息
     *
     * @param context
     * @param receiver
     * @param ssid
     * @param pwd
     */
    public void setGateNetwork(Context context, ResultReceiver receiver, String ssid, String pwd) {
        CommIntentService.startActionSetGateNetwork(context, receiver, ssid, pwd);
    }

    public void getAllLights(Context context, ResultReceiver receiver) {
        CommIntentService.startActionGetAllLights(context, receiver);
    }

    /**
     * 创建到网关的TCP连接
     * @param serverHost 服务器地址
     * @param serverPort 服务器端口
     * @throws java.io.IOException 连接报错
     */
    public void buildConnection(String serverHost, int serverPort) throws IOException {
        try {
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(serverHost, serverPort);
            socket.connect(socketAddress, Constants.SYSTEM_SETTINGS.NETWORK_CONNECT_TIMEOUT);
            socket.setSoTimeout(Constants.SYSTEM_SETTINGS.NETWORK_DATA_SOTIMEOUT);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            this.serverAddr = serverHost;
            this.serverPort = serverPort;
        } catch (IOException e) {
            //连接报错
            throw e;
        }
    }

    public void reconnect() throws IOException {
        buildConnection(this.serverAddr, this.serverPort);
    }

    /**
     * 关闭连接
     */
    public void closeConnection() {
        if (this.socket != null && !this.socket.isClosed()) {
            try {
                this.socket.close();
            } catch (IOException e) {
                Log.w(TAG, e.getStackTrace().toString());
                Log.w(TAG, e.getMessage());

            }
        }
    }
    /**
     * 私有构造函数
     */
    private DataAgent() {

    }

    public boolean isConnected() {
        if (this.socket != null) {
            return this.socket.isConnected();
        } else {
            return false;
        }
    }
    /**
     * 获取单例数据操作类
     * @return 数据代理对象
     */
    public static DataAgent getSingleInstance() {
        if (dataAgent == null) {
            dataAgent = new DataAgent();
        }
        return dataAgent;
    }
}
