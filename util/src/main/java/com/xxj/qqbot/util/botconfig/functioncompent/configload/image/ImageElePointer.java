package com.xxj.qqbot.util.botconfig.functioncompent.configload.image;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Field;

/**
 * 单个image信息元素
 */
@Data
@ToString
@AllArgsConstructor
public class ImageElePointer {

    /**
     * image所在的属性
     */
    Field targetField;

    /**
     * 图片本地路径
     */
    String path;

    /**
     * 获取image对象的key
     */
    String key;

    /**
     * 在本地缓存中的key
     */
    String jsonKey;

    /**
     * 在list中的位置
     */
    Integer location;

    /**
     * 更新周期
     */
    Integer cacheDay;
}
