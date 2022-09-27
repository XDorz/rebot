package com.xxj.qqbot.util.botconfig.functioncompent;

import cn.hutool.core.img.FontUtil;
import lombok.Data;

import java.awt.Font;
import java.io.File;
import java.util.List;

/**
 * 单项功能描述图形设置
 */
@Data
public class HelpImageConfig {

    private int x;
    private int y;
    private float xPercent;
    private float yPercent;
    private int width;
    private int height;
    private int colSpace;
    private int rowSpace;
    private boolean autoHeight;
    private Font titleFont;
    private float titleFontSize;
    private Font helpFont;
    private float helpFontSize;
    private List<File> helpBackground;

    public static HelpImageConfig DEFAULT=new HelpImageConfig();

    public HelpImageConfig(){
        this.x=50;
        this.y=50;
        this.xPercent=-1f;
        this.yPercent=-1f;
        this.width=800;
        this.height=300;
        this.colSpace=5;
        this.rowSpace=10;
        this.titleFontSize=40f;
        this.titleFont=FontUtil.createFont("宋体",(int)titleFontSize);
        this.helpFontSize=30f;
        this.helpFont=FontUtil.createFont("宋体",(int)helpFontSize);
    }

    public void setTileFont(String fontName){
        this.titleFont=FontUtil.createFont(fontName,(int)titleFontSize);
    }

    public void setHelpFont(String fontName){
        this.helpFont=FontUtil.createFont(fontName,(int)helpFontSize);
    }

    public void setTileFont(File fontFile){
        this.titleFont=FontUtil.createFont(fontFile);
    }

    public void setHelpFont(File fontFile){
        this.helpFont=FontUtil.createFont(fontFile);
    }

    public void setTileFont(File fontFile,float size){
        this.titleFont=FontUtil.createFont(fontFile);
        this.titleFontSize=size;
    }

    public void setHelpFont(File fontFile,float size){
        this.helpFont=FontUtil.createFont(fontFile);
        this.helpFontSize=size;
    }

    public void setTileFont(String fontName,int size){
        this.titleFont=FontUtil.createFont(fontName,size);
        this.titleFontSize=size;
    }

    public void setHelpFont(String fontName,int size){
        this.helpFont=FontUtil.createFont(fontName, size);
        this.helpFontSize=size;
    }
}
