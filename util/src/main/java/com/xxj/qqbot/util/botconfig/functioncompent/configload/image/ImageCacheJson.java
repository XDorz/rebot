package com.xxj.qqbot.util.botconfig.functioncompent.configload.image;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用于对缓存图片的本地存取
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageCacheJson {

    /**
     * 过期日期
     */
    Date EXPdate;

    /**
     * 图片上传后的id
     */
    String imageId;

    /**
     * 图片的本地路径
     */
    String path;
}
