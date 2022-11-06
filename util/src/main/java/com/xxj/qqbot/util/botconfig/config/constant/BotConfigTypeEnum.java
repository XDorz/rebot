package com.xxj.qqbot.util.botconfig.config.constant;

public enum BotConfigTypeEnum {

    LIST_TYPE("listType","链表类型"),

    MAP_TYPE("mapType","哈希表类型"),

    ENTITY_TYPE("entityType","领域类型/字符串类型 配置文件与领域属性一一对应"),

    MAP_LIST_TYPE("mapListType","链表指针组成哈希表类型"),

    IMAGE_ID_TYPE("imageType","图像上传后的id"),

    IMAGE_PATH_TYPE("imageType","图像本地路径"),

    DEFAULT_TYPE("defaultType","默认的注入类型，根据需要注入的属性选择"),

    FILE_PATH_TYPE("fileType","将文件的路径注入String类型"),
    ;

    /**
     * 装载配置的类型
     */
    String code;

    /**
     * 装载的配置的描述
     */
    String desc;

    BotConfigTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
