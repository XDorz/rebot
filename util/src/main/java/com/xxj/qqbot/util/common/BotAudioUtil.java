package com.xxj.qqbot.util.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.OfflineAudio;
import net.mamoe.mirai.utils.ExternalResource;
import org.springframework.util.StringUtils;

import java.io.*;

@Slf4j
public class BotAudioUtil {

    public static OfflineAudio upLoad(String filePath, MessageEvent event){
        if(!StringUtils.hasText(filePath)) return null;
        File file=new File(filePath);
        return upLoad(file,event);
    }

    public static OfflineAudio upLoad(File file, MessageEvent event){
        if(!file.exists()) return null;
        FileInputStream fileInputStream=null;
        try {
            fileInputStream=new FileInputStream(file);
            return upLoad(fileInputStream,event);
        } catch (FileNotFoundException e) {
            log.error("没有此文件！",e);
        }finally {
            if(fileInputStream!=null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.error("文件流关闭失败！",e);
                }
            }
        }
        return null;
    }

    public static OfflineAudio upLoadByUrl(String url, MessageEvent event){
        if(!StringUtils.hasText(url)) return null;
        HttpRequest request = HttpUtil.createGet(url);
        request.setFollowRedirects(true);
        InputStream stream = request.execute().bodyStream();
        return upLoad(stream,event);
    }

    /**
     * 核心上传代码
     */
    public static OfflineAudio upLoad(InputStream inputStream,MessageEvent event){
        if(inputStream==null) return null;
        ExternalResource resource=null;
        if(inputStream==null){
            return null;
        }
        try {
            resource = ExternalResource.create(inputStream);
            if(resource==null){
                return null;
            }
            OfflineAudio audio=null;
            if (event.getSubject() instanceof Friend) {
                audio=((Friend) event.getSubject()).uploadAudio(resource);
            }
            if(event.getSubject() instanceof Group){
                audio=((Group) event.getSubject()).uploadAudio(resource);
            }
            return  audio;
        } catch (Throwable e) {
            log.error("文件上传失败！",e);
        }finally {
            try {
                if(resource!=null) resource.close();
                if(inputStream!=null) inputStream.close();
            } catch (IOException e) {
                log.error("流关闭失败！",e);
            }
        }
        return null;
    }

    public static Integer uploadForId(String filePath, MessageEvent event){
        return upLoad(filePath,event).getCodec().getId();
    }

    public static Integer uploadForId(File file, MessageEvent event){
        return upLoad(file,event).getCodec().getId();
    }

    public static Integer uploadByUrlForId(String url, MessageEvent event){
        return upLoadByUrl(url,event).getCodec().getId();
    }

    public static Integer upload(InputStream inputStream,MessageEvent event){
        return upLoad(inputStream,event).getCodec().getId();
    }
}
