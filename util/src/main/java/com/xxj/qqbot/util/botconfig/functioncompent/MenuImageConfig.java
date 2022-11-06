package com.xxj.qqbot.util.botconfig.functioncompent;

import cn.hutool.core.img.FontUtil;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.common.MoriBotException;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 主菜单图片设置
 */
@Data
public class MenuImageConfig {

    //菜单内容行距
    private int valRowSpace;
    //菜单内容字距
    private int valColSpace;
    //分类标题字距
    private int titleColSpace;
    //提示行距
    private int tipRowSpace;
    //提示字距
    private int tipColSpace;
    //功能块水平间距
    private int blockHorizontalMargin;
    //功能块垂直间距
    private int blockVerticalMargin;
    //功能文字水平边距
    private int blockHorizontalPadding;
    //功能文字垂直边距，同时也是底边和功能块的最小边距
    private int blockVerticalPadding;
    //最大行字数限制
    private int maxValRowLength;
    //功能分类标题字体
    private Font titleFont;
    //功能分类标题大小
    private int titleFontSize;
    //功能分类标题是否粗体
    private boolean titleBold;
    //默认标题文字色彩
    private Color defaultTitleFontColor;
    //功能名称字体
    private Font menuValFont;
    //功能名称字体大小
    private int menuValFontSize;
    //默认功能名称字体色彩
    private Color defaultMenuFontColor;
    //提示字体
    private Font tipFont;
    //提示字体代销
    private int tipFontSize;
    //提示字体是否粗体
    private boolean tipBold;
    //功能块整体是否居中(是否添加整体偏移)
    private boolean menuBlockOffset;
    //功能块长度最大浮动
    private float menuMaxTolerancePercentH;
    //功能块长度最小浮动
    private float menuMinTolerancePercentH;
    //功能块背景快透明度
    private int menuContextBlockAlpha;
    //如果背景不够完整绘制所有功能是否报错
    private boolean throwExpWhileOverflow;
    //提示文字
    private String tip="欢迎使用机器人,以下是宝宝的功能列表：\n" +
            "     输入『功能名称 help』可获取功能详细介绍哦\n" +
            "     (red){注：标红的功能为本群暂时禁用}管理员可输入『功能名称 on』开启该功能\n" +
            "     (green){标绿字体为因某些原因下架修复的功能}";
    //菜单使用的背景，随机选择一个
    private List<File> menuBackground;

    //标题默认比文字大的大小
    private static final int TITLELARGED=3;
    //提示默认比文字大的大小
    private static final int TIPLARGED=6;

    public static MenuImageConfig DEFAULT=getInstance();

    public static MenuImageConfig getInstance(){
        return new MenuImageConfig();
    }

    public MenuImageConfig(){
        this.valRowSpace=10;
        this.valColSpace=0;
        this.titleColSpace=5;
        this.tipRowSpace=8;
        this.tipColSpace=3;
        this.blockHorizontalMargin=30;
        this.blockVerticalMargin=15;
        this.blockHorizontalPadding=12;
        this.blockVerticalPadding=15;
        this.maxValRowLength=10;
        this.defaultTitleFontColor=Color.BLACK;
        this.defaultMenuFontColor=null;
        this.menuValFontSize=25;
        this.menuValFont=FontUtil.createFont("宋体",menuValFontSize);
        this.titleBold=true;
        this.tipBold=true;
        this.menuBlockOffset=false;
        this.menuMaxTolerancePercentH=0.2f;
        this.menuMinTolerancePercentH=menuMaxTolerancePercentH;
        this.menuContextBlockAlpha=80;
        this.throwExpWhileOverflow=true;
    }

    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
        this.titleFontSize=titleFont.getSize();
    }

    public void setMenuValFont(Font menuValFont) {
        this.menuValFont = menuValFont;
        this.menuValFontSize=menuValFont.getSize();
    }

    public void setTipFont(Font tipFont) {
        this.tipFont = tipFont;
        this.tipFontSize=tipFont.getSize();
    }

    public void setTileFont(String fontName){
        this.titleFont=FontUtil.createFont(fontName,getTitleFontSize());
    }

    public void setMenuValFont(String fontName){
        this.menuValFont=FontUtil.createFont(fontName,menuValFontSize);
    }

    public void setTipFont(String fontName){
        this.tipFont=FontUtil.createFont(fontName,getTipFontSize());
    }

    public void setTitleFont(File fontFile){
        this.titleFont=FontUtil.createFont(fontFile).deriveFont((float)getTitleFontSize());
        this.titleFontSize=titleFont.getSize();
        if(titleBold) this.titleFont=titleFont.deriveFont(Font.BOLD);
    }

    public void setTipFont(File fontFile){
        this.tipFont=FontUtil.createFont(fontFile).deriveFont((float)getTipFontSize());
        this.tipFontSize=tipFont.getSize();
    }

    public void setMenuValFont(File fontFile){
        this.menuValFont=FontUtil.createFont(fontFile).deriveFont((float)menuValFontSize);
    }

    public void setTileFont(File fontFile,int size){
        this.titleFont=FontUtil.createFont(fontFile).deriveFont((float)size);
        this.titleFontSize=size;
        if(titleBold) this.titleFont=tipFont.deriveFont(Font.BOLD);
    }

    public void setTileFont(File fontFile,int size,boolean bold){
        this.titleFont=FontUtil.createFont(fontFile).deriveFont((float)size);
        this.titleFontSize=size;
        if(bold){
            this.titleFont=tipFont.deriveFont(Font.BOLD);
            this.titleBold=true;
        } else {
            this.titleBold=false;
        }
    }

    public void setTipFont(File fontFile,int size){
        this.tipFont=FontUtil.createFont(fontFile).deriveFont((float)size);
        this.tipFontSize=size;
    }

    public void setMenuValFont(File fontFile,int size){
        this.menuValFont=FontUtil.createFont(fontFile).deriveFont((float)size);
        this.menuValFontSize=size;
    }

    public void setTileFont(String fontName,int size){
        this.titleFont=FontUtil.createFont(fontName,size);
        this.titleFontSize=size;
        if(titleBold) this.titleFont=tipFont.deriveFont(Font.BOLD);
    }

    public void setTipFont(String fontName,int size){
        this.tipFont=FontUtil.createFont(fontName,size);
        this.tipFontSize=size;
    }

    public void setMenuValFont(String fontName,int size){
        this.menuValFont=FontUtil.createFont(fontName, size);
        this.menuValFontSize=size;
    }

    public Font getTipFont(){
        if(tipFont!=null){
            if(tipFont.getSize()!=getTipFontSize()){
                tipFont=tipFont.deriveFont((float)getTipFontSize());
            }
            return tipFont;
        }else {
            Font font=null;
            try {
                if(tipFontSize<=0){
                    tipFontSize=menuValFontSize+TIPLARGED;
                }
                font=menuValFont.deriveFont((float)tipFontSize);
                if(tipBold) font=font.deriveFont(Font.BOLD);
                tipFont=font;
                return font;
            }catch (Exception e){
                return tipBold?menuValFont.deriveFont(Font.BOLD):menuValFont;
            }
        }
    }

    public int getTipFontSize(){
        if(tipFontSize>0){
            return tipFontSize;
        }else {
            if(tipFont!=null){
                return tipFont.getSize();
            }else if(menuValFont!=null){
                return menuValFont.getSize()+TIPLARGED;
            }else{
                return menuValFontSize+TIPLARGED;
            }
        }
    }

    public Font getTitleFont(){
        if(titleFont!=null){
            if(titleFont.getSize()!=getTitleFontSize()){
                titleFont=titleFont.deriveFont((float) getTitleFontSize());
            }
            return titleFont;
        }else {
            Font font=null;
            try {
                if(titleFontSize<=0){
                    titleFontSize=menuValFontSize+TITLELARGED;
                }
                font=menuValFont.deriveFont((float)titleFontSize);
                if(titleBold) font=font.deriveFont(Font.BOLD);
                titleFont=font;
                return font;
            }catch (Exception e){
                return titleBold?menuValFont.deriveFont(Font.BOLD):menuValFont;
            }
        }
    }

    public int getTitleFontSize(){
        if(titleFontSize>0){
            return titleFontSize;
        }else {
            if(titleFont!=null){
                return titleFont.getSize();
            }else if(menuValFont!=null){
                return menuValFont.getSize()+TITLELARGED;
            }else{
                return menuValFontSize+TITLELARGED;
            }
        }
    }

    public List<File> getMenuBackground(){
        if(menuBackground!=null && menuBackground.size()>0){
            return menuBackground;
        }
        File file=new File(BotFrameworkConfig.tempFile+"/"+"defaultMenuBackground.png");
        if(!file.exists()){
            BufferedImage bufferedImage=new BufferedImage(1440,1440,BufferedImage.TYPE_INT_ARGB);
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                for (int i = 0; i < bufferedImage.getWidth(); i++) {
                    bufferedImage.setRGB(i,j,Color.white.getRGB());
                }
            }
            try{
                ImageIO.write(bufferedImage,"png",file);
            }catch (IOException e){
                throw new MoriBotException("无法将默认背景图保存为临时文件！",e);
            }
        }
        this.menuBackground= Collections.singletonList(file);
        return this.menuBackground;
    }
}
