package com.everhope.elighte.models;

import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.MessageUtils;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 移动端与网关通信报文格式基类
 * Created by kongxiaoyang on 2015/1/25.
 */
public class Message {

    /**
     * 报文头1
     * 报文帧头为0xFE  0xFE
     */
    private final short head1 = Constants.MESSAGES_CONSTS.MESSAGE_HEAD_1;

    /**
     * 报文头2
     * 0xFE 0x7E
     */
    private final short head2 = Constants.MESSAGES_CONSTS.MESSAGE_HEAD_2;

    /**
     * 整个数据包的长度，目前数据域最大长度是512字节，所以总包长度是512+16+2 =530字节
     *
     * 计算方法为 数据域长度+18 字节为单位
     */
    private short messageLength;
    /**
     * 报文特征码
     * 只有服务发现和登录智能网关的报文填0x0000，
     * 在登录智能网关成功后，由网关返回给手机特征码，
     * 以后手机发包需要带上这个特征码，特征码不符的报文将丢弃，
     * 也可用于多台手机检查是否是发给自己的报文。
     */
    private short messageSignature;

    /**
     * 报文序号
     * 主动发送方随机产生，应答方在响应的时候，
     * 将此序号返回，用于检验不同报文的响应。
     * 注意随机产生序号的时候，必须跳过FE FE、7E FE的特殊半字。
     */
    private short messageID;

    /**
     * 报文是否下行
     * 第15 bit指示报文是下行（手机到控制网关，1）还是上行（控制网到关手机，0）
     * true=1=手机到网关 false=0=网关到手机
     */
    private boolean appToGate;

    /**
     * 是否为回应消息
     * 第14 bit ack为0，代表是一端主动发出的报文，为1代表是对端响应报文
     * true=1=响应报文 false=0=主动报文
     */
    private boolean ack;

    /**
     * 第13 bit，E，为分片error bit，表示有错误发生。
     * 仅用于少数支持分片的网关应答命令，从网关向手机传送分片报文出错的指示，
     * 具体错误类型检查报文携带的命令返回码。
     *
     * true=1=出错 false=0=正确
     */
    private boolean sliceError;

    /**
     * 第12 bit，M，代表more报文标志，标志后面还有报文传输。
     * true=1=后续还有报文 false=0=后续没有报文
     */
    private boolean sliceMore;

    /**
     * 报文分片ID
     * 第0~11 bit 为报文分片序号，如果每片都是最大值512字节，则能够传送的最大字节数为512*4096= 2097152 =2 M 字节
     * 每个分片的数据长度可以通过报文长度域减去报文头和CRC长度计算出来
     * 该ID序号范围为 0~(4096-1)
     */
    private short sliceMessageID;

    /**
     * 操作对象类型 指示本命令的操作对象的类型
     * 0x00	智能网关对象
     * 0x04	站对象
     * 0x05	组对象
     * 0x06	区对象
     * 0x07	场景对象
     */
    private byte objectType;

    /**
     * 功能码 各种操作命令
     *
     * 0x00~0x3F	智能网关管理命令
     * 0x40~0x4F	站点管理命令
     * 0x50~0x5F	组管理命令
     * 0x60~0x6F	区管理命令
     * 0x70~0x7F	场景管理命令
     * 0x80	遥控
     * 0x90	遥调
     * 0xA0	遥测
     * 0xB0	遥信
     */
    private byte functionCode;

    /**
     * 操作对象ID，根据不同的功能码，指示不同的操作对象，区/组/站/场景分别对应区/组/站/场景，
     * 控制网关的ID，0x0000
     * 场景ID范围 1~0xF000，其余保留
     * 区ID范围 1~0xF000，其余保留
     * 组ID范围 1~0xF000，其余保留
     * 站ID范围 1~0xFFF7，其余保留，由智能网关生成，app通过“列表所有站点信息”命令，获取站ID
     *
     * 0xFFFF为广播，即某一类对象的所有控制设备，不是所有命令都支持广播，只对部分站点设备命令有效，具体参见命令描述
     * 0xFFFE为多对象控制，即一个报文中操作多个不同的设备，不是所有命令都支持广播，只对部分站点设备命令有效，具体参见命令描述
     */
    private short objectID;

    /**
     * 数据域
     * 512 字节长度
     */
    private byte[] data;

    /**
     * CRC校验
     * 从报文长度域开始计算，一直到数据域最后一个字节，生成多项式为G(X)=X16 +X12+X5+1；
     */
    private short crc;

    //////////////////////////////// methods ///////////////////////////////////////////

    /**
     * 将消息内容转换为字节数组
     *
     * @return
     */
    public byte[] toMessageByteArray() {

        ByteBuffer bb = ByteBuffer.allocate(1024);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        //转换消息头
        bb.putShort(head1);
        bb.putShort(head2);
        //报文长度
        bb.putShort(messageLength);
        //报文特征码
        bb.putShort(messageSignature);
        //报文序号
        bb.putShort(messageID);
        //转换报文属性域
        short tempShort = getPropertiesRegion();
        bb.putShort(tempShort);

        //操作对象类型
        bb.put(objectType);
        //操作功能码
        bb.put(functionCode);
        //操作对象ID
        bb.putShort(objectID);

        //数据域
        bb.put(data);

        //CRC
        bb.putShort(crc);

        int size = bb.position();
        bb.flip();

        byte[] bytes = new byte[size];
        bb.get(bytes);

        return bytes;
    }

    public void setPropertiesRegion(short props) {
        short temp = 0;
        temp = (short)(props & 0x8000);
        if (temp == 0) {
            appToGate = false;
        } else {
            appToGate = true;
        }

        temp = (short)(props & 0x4000);
        if (temp == 0) {
            ack = false;
        } else {
            ack = true;
        }

        temp = (short)(props & 0x2000);
        if (temp == 0) {
            sliceError = false;
        } else {
            sliceError = true;
        }

        temp = (short)(props & 0x1000);
        if (temp == 0) {
            sliceMore = false;
        } else {
            sliceMore = true;
        }

        temp = (short)(props & 0x0fff);
        sliceMessageID = temp;
    }

    /**
     * 获取当前消息的属性域
     * @return
     */
    private short getPropertiesRegion() {
        byte[] props = new byte[2];
        short first = 0x0;
//        short sliceSeq = 0;

        if (appToGate) {
            first = (short)(0x8000 | first);
        }
        if (ack) {
            first = (short)(0x4000 | first);
        }
        if (sliceError) {
            first = (short)(0x2000 | first);
        }
        if (sliceMore) {
            first = (short)(0x1000 | first);
        }
        short s1 = (short)(sliceMessageID | first);

        return s1;
    }

    /**
     * 创建消息的通用部分
     * 报文序号
     * 报文上行/下行（默认下行）
     * 是否响应报文
     * 是否分片错误
     * 是否还有分片数据
     * 分片数据ID
     */
    public void buildUp() {
        setMessageID(MessageUtils.getRandomMessageID());
        //DAEM 的设置 和 分片序号
        setAppToGate(true);
        setAck(false);
        setSliceError(false);
        setSliceMore(false);
        setSliceMessageID((short)0);
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /////////////////////////////// getters and setters /////////////////////////////////

    public int getHead1() {
        return head1;
    }

    public int getHead2() {
        return head2;
    }
    public short getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(short messageLength) {
        this.messageLength = messageLength;
    }

    public short getMessageSignature() {
        return messageSignature;
    }

    public void setMessageSignature(short messageSignature) {
        this.messageSignature = messageSignature;
    }

    public short getMessageID() {
        return messageID;
    }

    /**
     * 设置报文序号 目前随机
     * @param messageID
     */
    public void setMessageID(short messageID) {
        this.messageID = messageID;
    }

    public boolean isAppToGate() {
        return appToGate;
    }

    public void setAppToGate(boolean appToGate) {
        this.appToGate = appToGate;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public boolean isSliceError() {
        return sliceError;
    }

    public void setSliceError(boolean sliceError) {
        this.sliceError = sliceError;
    }

    public boolean isSliceMore() {
        return sliceMore;
    }

    public void setSliceMore(boolean sliceMore) {
        this.sliceMore = sliceMore;
    }

    public short getSliceMessageID() {
        return sliceMessageID;
    }

    public void setSliceMessageID(short sliceMessageID) {
        this.sliceMessageID = sliceMessageID;
    }

    public byte getObjectType() {
        return objectType;
    }

    public void setObjectType(byte objectType) {
        this.objectType = objectType;
    }

    public byte getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(byte functionCode) {
        this.functionCode = functionCode;
    }

    public short getObjectID() {
        return objectID;
    }

    public void setObjectID(short objectID) {
        this.objectID = objectID;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public short getCrc() {
        return crc;
    }

    public void setCrc(short crc) {
        this.crc = crc;
    }

    /*************************** 参考 **************************/
    /**
     *
     * byte：一个字节（8位）（-128~127）（-2的7次方到2的7次方-1）
     * short：两个字节（16位）（-32768~32767）（-2的15次方到2的15次方-1）
     * int：四个字节（32位）（一个字长）（-2147483648~2147483647）（-2的31次方到2的31次方-1）
     * long：八个字节（64位）（-9223372036854774808~9223372036854774807）（-2的63次方到2的63次方-1）
     * float：四个字节（32位）（3.402823e+38 ~ 1.401298e-45）（e+38是乘以10的38次方，e-45是乘以10的负45次方）
     * double：八个字节（64位）（1.797693e+308~ 4.9000000e-324
     */
}
