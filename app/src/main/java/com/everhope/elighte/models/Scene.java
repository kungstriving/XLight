package com.everhope.elighte.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * 场景定义
 * 对接ActiveAndroid模型
 * Created by kongxiaoyang on 2015/2/9.
 */
@Table(name="SCENES")
public class Scene extends Model {
    /**
     * 场景名称
     */
    @Column(name="NAME")
    public String name;

    /**
     * 场景对应的图片名称
     */
    @Column(name="IMG_NAME")
    public String imgName;

    /**
     * 场景状态
     * 0-关闭
     * 1-开启
     */
    @Column(name="STATUS")
    public int status;

    /**
     * 场景亮度
     */
    @Column(name="BRIGHTNESS")
    public int brightness;

    public List<LightScene> lightScenes () {
        return getMany(LightScene.class, "SCENE");
    }

    public Scene() {
        super();
    }

    /**
     * 获取所有场景
     * @return
     */
    public static List<Scene> getAll() {
        return new Select()
                .from(Scene.class)
                .execute();
    }
}
