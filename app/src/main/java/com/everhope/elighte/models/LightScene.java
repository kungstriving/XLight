package com.everhope.elighte.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

/**
 * 场景 灯 关系表
 * Created by kongxiaoyang on 2015/2/9.
 */
@Table(name="LIGHTSCENES")
public class LightScene extends Model{
    @Column(name="LIGHT")
    public Light light;
    @Column(name="SCENE")
    public Scene scene;
    /**
     * R
     */
    @Column(name="R")
    public int R;

    /**
     * G
     */
    @Column(name = "G")
    public int G;

    /**
     * B
     */
    @Column(name = "B")
    public int B;

    @Column(name = "x")
    public int x;
    @Column(name = "y")
    public int y;

    public List<Light> lights() {
        return getMany(Light.class, "LightScene");
    }
    public List<Scene> scenes() {
        return getMany(Scene.class, "LightScene");
    }

    public LightScene() {
        super();
    }
}
