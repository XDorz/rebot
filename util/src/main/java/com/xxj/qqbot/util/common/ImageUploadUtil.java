package com.xxj.qqbot.util.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ImageUploadUtil {

    private static Random random=new Random(System.currentTimeMillis());

    private static Pattern urlPtn=Pattern.compile("(http[^±━ ]+)");

    public static String getUrlRealVal(String url){
        Matcher matcher = urlPtn.matcher(url);
        if(matcher.find()){
            return matcher.group(1);
        }
        return url;
    }

    /**
     * 解决部分网络接口返回网址带其他分隔符号问题
     */
    public static String getHttpValue(String url){
        if(!StringUtils.hasText(url)) return null;
        HttpRequest request = HttpUtil.createGet(url);
        request.setFollowRedirects(true);
        request.setMaxRedirectCount(10);
        HttpResponse response = request.execute();
        String body = response.body();
        return getUrlRealVal(body);
    }

    /**
     * 部分网络接口返回网址，需要二次跳转
     */
    public static Image uploadWithRedirectUrl(String url){
        if(!StringUtils.hasText(url)) return null;
        return uploadByUrl(getHttpValue(url));
    }

    public static Image uploadByUrl(String url){
        if(!StringUtils.hasText(url)) return null;
        HttpRequest request = HttpUtil.createGet(url);
        request.setFollowRedirects(true);
        request.setMaxRedirectCount(10);
        HttpResponse response = request.execute();
        InputStream inputStream = response.bodyStream();
        return upload(inputStream);
    }


    public static Image upload(String path){
        if(!StringUtils.hasText(path)) return null;
        File file=new File(path);
        return upload(file);
    }

    public static Image upload(File file){
        if(!file.exists()) return null;
        FileInputStream fileInputStream=null;
        try {
            fileInputStream=new FileInputStream(file);
            return upload(fileInputStream);
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

    public static Image upload(BufferedImage bufferedImage){
        ByteArrayOutputStream bout=null;
        ByteArrayInputStream bin=null;
        try {
            bout=new ByteArrayOutputStream();
            ImageIO.write(bufferedImage,"png",bout);
            bin=new ByteArrayInputStream(bout.toByteArray());
            return upload(bin);
        } catch (IOException e) {
            log.error("无法将图片写出",e);
        }finally {
            try {
                if(bout!=null) bout.close();
                if(bin!=null) bin.close();
            }catch (IOException e){
                log.error("无法关闭流！",e);
            }
        }
        return null;
    }

    /**
     * 核心上传代码
     */
    public static Image upload(InputStream inputStream){
        Image image=null;
        ExternalResource externalResource=null;
        try {
            externalResource=ExternalResource.create(inputStream);
            image=ExternalResource.uploadAsImage(externalResource, BotFrameworkConfig.bot.getAsFriend());
        } catch (IOException e) {
            log.error("图像上传流创建失败",e);
            return null;
        }finally {
            try {
                if(inputStream!=null) inputStream.close();
                if(externalResource!=null) externalResource.close();
            } catch (IOException e) {
                log.error("文件流无法关闭",e);
                return null;
            }
        }
        return image;
    }

    public static Image uploadAntiKill(InputStream inputStream){
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            log.error("无法从输入流读取Buffered图像！！！",e);
        }
        return uploadAntiKill(bufferedImage);
    }

    /**
     * 核心防封代码
     */
    public static Image uploadAntiKill(BufferedImage bufferedImage){
        bufferedImage.setRGB(1,1,random.nextInt(1694498560));
//        bufferedImage.setRGB(4,4,random.nextInt(1694498560));
        return upload(bufferedImage);
    }

    public static Image uploadAntiKill(File file){
        if(!file.exists()) return null;
        FileInputStream fileInputStream=null;
        try {
            fileInputStream=new FileInputStream(file);
            return uploadAntiKill(fileInputStream);
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

    public static Image uploadAntiKill(String path){
        if(!StringUtils.hasText(path)) return null;
        File file=new File(path);
        return uploadAntiKill(file);
    }

    public static String uploadForId(String path){
        return Objects.requireNonNull(upload(path)).getImageId();
    }

    public static String uploadForId(File file){
        return Objects.requireNonNull(upload(file)).getImageId();
    }

    public static String upLoadForId(File file){
        return Objects.requireNonNull(upload(file)).getImageId();
    }

    public static String uploadForId(BufferedImage bufferedImage){
        return Objects.requireNonNull(upload(bufferedImage)).getImageId();
    }

    public static String uploadByUrlForId(String url){
        return Objects.requireNonNull(uploadByUrl(url)).getImageId();
    }

    public static String uploadWithRedirectUrlForId(String url){
        return Objects.requireNonNull(uploadWithRedirectUrl(url)).getImageId();
    }
}
