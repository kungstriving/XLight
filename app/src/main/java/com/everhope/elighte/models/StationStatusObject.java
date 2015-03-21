package com.everhope.elighte.models;

import java.util.List;

/**
 * 站点状态对象
 * Created by kongxiaoyang on 2015/3/21.
 */
public class StationStatusObject {
    private short id;
    private short subCmdCount;
    private List<StationSubCmd> stationSubCmdList;

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public short getSubCmdCount() {
        return subCmdCount;
    }

    public void setSubCmdCount(short subCmdCount) {
        this.subCmdCount = subCmdCount;
    }

    public List<StationSubCmd> getStationSubCmdList() {
        return stationSubCmdList;
    }

    public void setStationSubCmdList(List<StationSubCmd> stationSubCmdList) {
        this.stationSubCmdList = stationSubCmdList;
    }
}
