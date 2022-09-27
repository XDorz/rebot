package com.xxj.qqbot.util.botconfig.functioncompent.configload.image;

import java.util.List;

/**
 * 储存需要更新image信息
 */
public class ImageHolder {

    /**
     * 储存需要更新image信息的容器
     */
    public static List<List<ImageElePointer>> holder;

    /**
     * 下次即将更新的链表的位置
     */
    public static Integer updateOrder;
}
