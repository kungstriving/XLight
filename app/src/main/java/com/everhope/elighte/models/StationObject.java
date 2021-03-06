package com.everhope.elighte.models;

import com.everhope.elighte.constants.StationTypes;

import org.apache.commons.codec.binary.Hex;

/**
 * Created by kongxiaoyang on 2015/2/25.
 */
public class StationObject {

    private short id;
    private StationTypes stationTypes;
    private String mac;

    public short getId() {
        return id;
    }

    public String getIdStr() {
        return Integer.toHexString(id).hashCode() + "";
    }

    public void setId(short id) {
        this.id = id;
    }

    public StationTypes getStationTypes() {
        return stationTypes;
    }

    public void setStationTypes(StationTypes stationTypes) {
        this.stationTypes = stationTypes;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
