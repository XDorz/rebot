package com.xxj.qqbot.util.botconfig.functioncompent;

import cn.hutool.core.img.FontUtil;
import lombok.Data;

import java.awt.Color;
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
    private int titleFontSize;
    private Font helpFont;
    private int helpFontSize;
    private List<File> helpBackground;
    private String titleValue;
    private int titleProgress;
    private Color titleFontDefaultColor;
    private Color contextFontDefaultColor;

    private static final int TITLELARGED=5;

    public static HelpImageConfig DEFAULT=new HelpImageConfig(){
        {
            setXPercent(0.1f);
            setYPercent(0f);
            setAutoHeight(true);
        }
    };

    public HelpImageConfig(){
        this.x=-1;
        this.y=-1;
        this.xPercent=-1f;
        this.yPercent=-1f;
        this.width=-1;
        this.height=-1;
        this.colSpace=3;
        this.rowSpace=5;
        this.helpFontSize=20;
        this.autoHeight=false;
        this.helpFont=FontUtil.createFont("宋体",helpFontSize);
        this.titleValue="---usage";
        this.titleProgress=30;
        this.titleFontDefaultColor=Color.BLACK;
        this.contextFontDefaultColor=Color.BLACK;
    }

    public void setTileFont(String fontName){
        this.titleFont=FontUtil.createFont(fontName,getTitleFontSize());
    }

    public void setHelpFont(String fontName){
        this.helpFont=FontUtil.createFont(fontName,helpFontSize);
    }

    public void setTileFont(File fontFile){
        this.titleFont=FontUtil.createFont(fontFile);
        this.helpFontSize=titleFont.getSize();
    }

    public void setHelpFont(File fontFile){
        this.helpFont=FontUtil.createFont(fontFile);
        this.helpFontSize=helpFont.getSize();
    }

    public void setTileFont(File fontFile,int size){
        this.titleFont=FontUtil.createFont(fontFile);
        this.titleFontSize=size;
    }

    public void setHelpFont(File fontFile,int size){
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

    public Font getTitleFont(){
        if(titleFont!=null){
            return titleFont;
        }else {
            Font font=null;
            try {
                if(titleFontSize>0){
                    font=FontUtil.createFont(helpFont.getFontName(),titleFontSize);
                }else {
                    font=FontUtil.createFont(helpFont.getFontName(),helpFont.getSize()+TITLELARGED);
                }
                return font;
            }catch (Exception e){
                return helpFont;
            }
        }
    }

    public int getTitleFontSize(){
        if(titleFontSize>0){
            return titleFontSize;
        }else {
            if(titleFont!=null){
                return titleFont.getSize();
            }else if(helpFont!=null){
                return helpFont.getSize()+TITLELARGED;
            }else{
                return helpFontSize+TITLELARGED;
            }
        }
    }
}
