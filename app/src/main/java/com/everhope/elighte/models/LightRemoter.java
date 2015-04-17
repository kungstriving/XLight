package com.everhope.elighte.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

/**
 * 灯和遥控器关系
 * Created by kongxiaoyang on 2015/4/14.
 */
@Table(name="LIGHTREMOTERS")
public class LightRemoter extends Model {
    @Column(name="LIGHT")
    public Light light;
    @Column(name="REMOTER")
    public Remoter remoter;
    @Column(name="GROUP_NAME")
    public String groupName;

}
