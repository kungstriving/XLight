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
