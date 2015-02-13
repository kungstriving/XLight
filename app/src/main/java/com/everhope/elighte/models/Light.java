package com.everhope.elighte.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

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
     * 灯id
     */
    @Column(name="LIGHT_ID")
    public String lightID;



    public List<LightScene> lightScenes () {
        return getMany(LightScene.class, "LIGHT");
    }

    public Light() {
        super();
    }

}
