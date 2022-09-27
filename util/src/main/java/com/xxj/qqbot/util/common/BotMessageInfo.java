package com.xxj.qqbot.util.common;

import lombok.Data;
import net.mamoe.mirai.message.data.*;

import java.util.Map;

/**
 * 消息拆分体
 */
@Data
public class BotMessageInfo {

    /**
     * 收到的文字消息
     */
    private String messageContext;

    /**
     * 收到消息参数
     * 无论是否有参数
     * map中都有一项key为command(指令)
     * 代表文字消息的第一句
     * 如果没有文字消息则为“”
     */
    private ParamMap<String,String> params;

    /**
     * 消息中带的图片
     */
    private Image[] images;

    /**
     * 消息中带的语言信息
     */
    private Audio audio;

    /**
     * 消息中带的emoji表情
     */
    private Face[] emojis;

    /**
     * 消息中带的转发消息
     */
    private ForwardMessage forwardMessage;

    /**
     *  消息中带的音乐分享
     */
    private MusicShare musicShare;

    /**
     * 消息中带的@全体成员
     */
    private AtAll atAll;

    /**
     * 消息中带的所有@
     */
    private At[] ats;

    /**
     * 消息中带的@bot
     */
    private At atBot;

    /**
     * 消息中带的app内容分享
     */
    private ServiceMessage appShare;

    public BotMessageInfo() {
        params=new ParamMap<>();
        params.put("command","");
    }

    public void setVal(BotMessageInfo info){
        this.setEmojis(info.getEmojis());
        this.setAts(info.getAts());
        this.setImages(info.getImages());
        this.setAtAll(info.getAtAll());
        this.setForwardMessage(info.getForwardMessage());
        this.setAudio(info.getAudio());
        this.setAtBot(info.atBot);
        this.setAppShare(info.appShare);
        this.setMessageContext(info.messageContext);
        this.setParams(info.params);
        this.setMusicShare(info.musicShare);
    }

    public boolean hasMessageContext(){
        return messageContext!=null;
    }

    public boolean hasParam(){
        if(params==null) return false;
        return params.size()>1;
    }

    public boolean hasImage(){
        if(images==null) return false;
        return images.length!=0;
    }

    public boolean hasAudio(){
        return audio!=null;
    }

    public boolean hasEmoji(){
        if(emojis==null) return false;
        return emojis.length!=0;
    }

    public boolean hasForwardMessage(){
        return forwardMessage!=null;
    }

    public boolean hasMusicShare(){
        return musicShare!=null;
    }

    public boolean hasAtAll(){
        return atAll!=null;
    }

    public boolean hasAt(){
        if(ats==null) return false;
        return ats.length!=0;
    }

    public boolean hasAtBot(){
        return atBot!=null;
    }

    public boolean hasAppShare(){
        return appShare!=null;
    }

}
