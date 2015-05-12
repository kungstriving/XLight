package com.everhope.elighte.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * 遥控器站点定义
 * Created by kongxiaoyang on 2015/4/14.
 */
@Table(name="REMOTERS")
public class Remoter extends Model {
    /**
     * 遥控器名称
     */
    @Column(name="NAME")
    public String name;

    /**
     * 遥控器ID
     */
    @Column(name="REMOTER_ID")
    public String remoterID;

    /**
     * 遥控器MAC
     */
    @Column(name="REMOTER_MAC")
    public String remoterMac;
    /**
     * 告警
     */
    @Column(name="TRIGGER_ALARM")
    public boolean triggerAlarm = false;

    /**
     * 失联
     */
    @Column(name="LOST_CONNECTION")
    public boolean lostConnection = false;

    public List<LightRemoter> lightRemoters() {
        return getMany(LightRemoter.class, "REMOTER");
    }

    public List<Light> groupLights(String groupNum) {

        List<Light> lights = new ArrayList<>();
        List<LightRemoter> lightRemoters = getMany(LightRemoter.class, "REMOTER");

        for (LightRemoter lightRemoter : lightRemoters) {
            if (lightRemoter.groupName.equals(groupNum)) {
                lights.add(lightRemoter.light);
            }
        }
        return lights;
    }

    public List<LightRemoter> groupLightRemoters(String groupNum) {
        List<LightRemoter> lightRemoterList = new ArrayList<>();
        List<LightRemoter> lightRemoters = getMany(LightRemoter.class, "REMOTER");

        for (LightRemoter lightRemoter : lightRemoters) {
            if (lightRemoter.groupName.equals(groupNum)) {
                lightRemoterList.add(lightRemoter);
            }
        }

        return lightRemoterList;
    }

    public static Remoter getRemoterByID(String id) {
        return new Select().from(Remoter.class)
                .where("REMOTER_ID = ?", id)
                .executeSingle();
    }

    public static List<Remoter> getAll() {
        return new Select()
                .from(Remoter.class)
                .execute();
    }
}
