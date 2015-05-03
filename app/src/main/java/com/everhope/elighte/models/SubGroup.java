package com.everhope.elighte.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * 灯分组对象
 * Created by kongxiaoyang on 2015/2/25.
 */
@Table(name="GROUPS")
public class SubGroup extends Model {
    @Column(name="NAME")
    public String name;

    /**
     * 分组亮度
     */
    @Column(name="BRIGHTNESS")
    public int brightness;

    /**
     * R
     */
    @Column(name="R_COLOR")
    public int rColor;

    /**
     * G
     */
    @Column(name="G_COLOR")
    public int gColor;

    /**
     * B
     */
    @Column(name="B_COLOR")
    public int bColor;

    @Column(name="x")
    public int x;
    @Column(name="y")
    public int y;

    /**
     * 场景状态
     * 0-关闭
     * 1-开启
     */
    @Column(name="STATUS")
    public int status;

    public List<LightGroup> lightGroups() {
//        new Select().from(LightGroup.class).where("")
        return getMany(LightGroup.class, "SUBGROUP");
    }

    public SubGroup() {
        super();
    }

    public static List<SubGroup> getAll() {
        return new Select()
                .from(SubGroup.class)
                .execute();
    }
}
