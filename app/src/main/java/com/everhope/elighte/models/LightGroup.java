package com.everhope.elighte.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

/**
 * 灯 分组 关系表
 * Created by kongxiaoyang on 2015/2/25.
 */
@Table(name="LIGHTGROUPS")
public class LightGroup extends Model {
    @Column(name="LIGHT")
    public Light light;
    @Column(name="SUBGROUP")
    public SubGroup subgroup;

    public List<Light> lights() {
        return getMany(Light.class, "LightGroup");
    }

    public List<SubGroup> groups() {
        return getMany(SubGroup.class, "LightGroup");
    }

    public LightGroup() {
        super();
    }
}
