package com.everhope.elighte.constants;

/**
 * 报文 对象类型
 * 0x00	智能网关对象
 * 0x04	站对象
 * 0x05	组对象
 * 0x06	区对象
 * 0x07	场景对象
 * Created by kongxiaoyang on 2015/1/25.
 */
public enum MessageObjectTypes {

    GATE(Byte.decode("0x00")),
    /**
     * 站点对象 04
     */
    STATION(Byte.decode("0x04")),
    GROUP(Byte.decode("0x05")),
    REGION(Byte.decode("0x06")),
    SCENE(Byte.decode("0x07"));

    private byte objectType;

    private MessageObjectTypes(byte type) {
        this.objectType = type;
    }

    public byte getObjectType() {
        return this.objectType;
    }
}
