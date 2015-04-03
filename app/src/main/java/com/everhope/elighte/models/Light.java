package com.everhope.elighte.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * 灯定义
 *
 * Created by kongxiaoyang on 2015/2/9.
 */
@Table(name="LIGHTS")
public class Light extends Model{

    /**
     * 灯名称
     */
    @Column(name="NAME")
    public String name;

    /**
     * 灯id=station id
     */
    @Column(name="LIGHT_ID")
    public String lightID;

    /**
     * 灯地址 mac
     */
    @Column(name="LIGHT_MAC")
    public String lightMac;

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

    /**
     * 开关状态
     */
    @Column(name="SWITCH_ON")
    public boolean switchOn = true;

    @Column(name="R_COLOR")
    public int rColor;
    @Column(name="G_COLOR")
    public int gColor;
    @Column(name="B_COLOR")
    public int bColor;
    @Column(name = "BRIGHTNESS")
    public int brightness;

    public List<LightGroup> lightGroups() {
        return getMany(LightGroup.class, "LIGHT");
    }

    public List<LightScene> lightScenes () {
        return getMany(LightScene.class, "LIGHT");
    }

    /**
     * 获取所有灯列表
     * @return
     */
    public static List<Light> getAll() {
        return new Select()
                .from(Light.class)
                .execute();
    }

    public static Light getByLightID(String lightID) {
        return new Select().from(Light.class)
                .where("LIGHT_ID = ?", lightID)
                .executeSingle();
    }

    public static Light getByLightMAC(String lightMAC) {
        return new Select().from(Light.class)
                .where("LIGHT_MAC = ?",lightMAC)
                .executeSingle();
    }

    public Light() {
        super();
    }

}
