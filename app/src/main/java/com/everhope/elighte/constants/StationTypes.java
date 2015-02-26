package com.everhope.elighte.constants;

/**
 * 站点类型
 * Created by kongxiaoyang on 2015/2/25.
 */
public enum StationTypes {

    GATE(Short.decode("0x0000")),
    LIGHT(Short.decode("0x0100")),
    SWITCH(Short.decode("0x0200")),
    REMOTER(Short.decode("0x0300"));

    private short stationType;

    private static final short gateShort = 0;
    private static final short lightShort = 256;    //Short.decode("0x0100");
    private static final short switchShort = 512;             //Short.decode("0x0200");
    private static final short remoterShort = 768;  //Short.decode("0x0300");

    private StationTypes(short s) {
        this.stationType = s;
    }

    public short getStationType() {
        return this.stationType;
    }

    public static StationTypes fromStationTypeShort(short type) {
        switch (type) {
            case gateShort:
                return GATE;
            case lightShort:
                return LIGHT;
            case switchShort:
                return SWITCH;
            case remoterShort:
                return REMOTER;
            default:
                return LIGHT;
        }
    }
}
