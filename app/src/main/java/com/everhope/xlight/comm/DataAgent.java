package com.everhope.xlight.comm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.everhope.xlight.constants.Constants;
import com.everhope.xlight.helpers.MessageUtils;

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
    public void logonToGate(String clientID, Context context, ResultReceiver receiver) {
        //主线程中进行登录
        if (this.socket.isConnected()) {
            OutputStream ostream = null;
            try {
                //发送数据
                ostream = this.socket.getOutputStream();
                byte[] data = MessageUtils.composeLogonMsg(clientID);
                ostream.write(data);
                ostream.flush();

                //接收数据
//                CommIntentService.startActionRecData(context, receiver);
            } catch (IOException e) {
                Log.w(TAG, "获取输出流错误");
                Log.w(TAG, e.getMessage());
                //TODO 重连
            } finally {
                try {
                    if (ostream != null) {
                        ostream.flush();
                        ostream.close();
                    }
                } catch (IOException e) {
                    Log.w(TAG, "关闭输出流错误");
                }
            }
        } else {
            Log.w(TAG, "socket 连接断开");
            //TODO 重连
        }
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
        } catch (IOException e) {
            //连接报错
            throw e;
        }
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
