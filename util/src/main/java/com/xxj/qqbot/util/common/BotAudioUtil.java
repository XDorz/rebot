package com.xxj.qqbot.util.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.AudioSupported;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.OfflineAudio;
import net.mamoe.mirai.utils.ExternalResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class BotAudioUtil {

    public static OfflineAudio upLoad(String filePath, Contact contact){
        if(!StringUtils.hasText(filePath)) return null;
        File file=new File(filePath);
        return upLoad(file,contact);
    }

    public static OfflineAudio upLoad(File file, Contact contact){
        if(!file.exists()) log.error("所要上传的音频文件不存在！");
        FileInputStream fileInputStream=null;
        try {
            fileInputStream=new FileInputStream(file);
            return upLoad(fileInputStream,contact);
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

    public static OfflineAudio upLoadByUrl(String url, Contact contact){
        if(!StringUtils.hasText(url)) return null;
        HttpRequest request = HttpUtil.createGet(url);
        request.setFollowRedirects(true);
        InputStream stream = request.execute().bodyStream();
        return upLoad(stream,contact);
    }

    /**
     * 核心上传代码
     */
    public static OfflineAudio upLoad(InputStream inputStream, Contact contact){
        if(!(contact instanceof AudioSupported)){
            throw new MoriBotException("该对象无法发送语音");
        }
        if(inputStream==null) return null;
        ExternalResource resource=null;
        try {
            resource = ExternalResource.create(inputStream);
            OfflineAudio audio=null;
            audio=((AudioSupported) contact).uploadAudio(resource);
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

    public static Integer uploadForId(String filePath, Contact contact){
        return upLoad(filePath,contact).getCodec().getId();
    }

    public static Integer uploadForId(File file, Contact contact){
        return upLoad(file,contact).getCodec().getId();
    }

    public static Integer uploadByUrlForId(String url, Contact contact){
        return upLoadByUrl(url,contact).getCodec().getId();
    }

    public static Integer upload(InputStream inputStream,Contact contact){
        return upLoad(inputStream,contact).getCodec().getId();
    }
}
