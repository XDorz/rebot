package com.xxj.qqbot.util.botconfig.functioncompent;

import cn.hutool.core.img.FontUtil;
import lombok.Data;

import java.awt.Font;
import java.io.File;
import java.util.List;

/**
 * 主菜单图片设置
 */
@Data
public class MenuImageConfig {

    private int x;
    private int y;
    private float xPercent;
    private float yPercent;
    private int width;
    private int height;
    private int colSpace;
    private int rowSpace;
    private Font titleFont;
    private float titleFontSize;
    private Font menuValFont;
    private float menuValFontSize;
    private List<File> menuBackground;

    public static MenuImageConfig DEFAULT=new MenuImageConfig();

    public MenuImageConfig(){
        this.x=50;
        this.y=50;
        this.xPercent=-1f;
        this.yPercent=-1f;
        this.width=800;
        this.height=300;
        this.colSpace=5;
        this.rowSpace=10;
        this.titleFontSize=50f;
        this.titleFont=FontUtil.createFont("宋体",(int)titleFontSize);
        this.menuValFontSize=35f;
        this.menuValFont=FontUtil.createFont("宋体",(int)menuValFontSize);
    }

    public void setTileFont(String fontName){
        this.titleFont= FontUtil.createFont(fontName,(int)titleFontSize);
    }

    public void setMenuValFont(String fontName){
        this.menuValFont=FontUtil.createFont(fontName,(int)menuValFontSize);
    }

    public void setTileFont(File fontFile){
        this.titleFont=FontUtil.createFont(fontFile);
    }

    public void setMenuValFont(File fontFile){
        this.menuValFont=FontUtil.createFont(fontFile);
    }

    public void setTileFont(File fontFile,float size){
        this.titleFont=FontUtil.createFont(fontFile);
        this.titleFontSize=size;
    }

    public void setMenuValFont(File fontFile,float size){
        this.menuValFont=FontUtil.createFont(fontFile);
        this.menuValFontSize=size;
    }

    public void setTileFont(String fontName,int size){
        this.titleFont=FontUtil.createFont(fontName,size);
        this.titleFontSize=size;
    }

    public void setMenuValFont(String fontName,int size){
        this.menuValFont=FontUtil.createFont(fontName,size);
        this.menuValFontSize=size;
    }
}
