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
 * 单项功能描述图形设置
 */
@Data
public class HelpImageConfig {

    //内容绘制起始水平位置(二选一)
    private int x;
    //内容绘制起始垂直位置
    private int y;
    //内容绘制起始水平位置百分比(二选一)
    private float xPercent;
    //内容绘制起始垂直位置百分比
    private float yPercent;
    //内容可绘制宽度
    private int width;
    //内容可绘制长度
    private int height;
    //内容字据
    private int colSpace;
    //内容行距
    private int rowSpace;
    //是否设置自动高度
    private boolean autoHeight;
    //标题字体
    private Font titleFont;
    //标题字体大小
    private int titleFontSize;
    //内容字体
    private Font helpFont;
    //内容字体大小
    private int helpFontSize;
    //帮助功能背景图(随机)
    private List<File> helpBackground;
    //标题
    private String titleValue;
    //标题缩进大小
    private int titleProgress;
    //标题字体默认色彩
    private Color titleFontDefaultColor;
    //内容字体默认色彩
    private Color contextFontDefaultColor;

    //标题字体默认比内容大的大小
    private static final int TITLELARGED=5;

    public static HelpImageConfig DEFAULT=getInstance();

    public static HelpImageConfig getInstance(){
        return new HelpImageConfig(){
            {
                setXPercent(0.1f);
                setYPercent(0f);
                setAutoHeight(true);
            }
        };
    }

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

    public void setHelpFont(Font helpFont) {
        this.helpFont = helpFont;
        this.helpFontSize=helpFont.getSize();
    }

    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
        this.titleFontSize=titleFont.getSize();
    }

    public void setTileFont(String fontName){
        this.titleFont=FontUtil.createFont(fontName,getTitleFontSize());
    }

    public void setHelpFont(String fontName){
        this.helpFont=FontUtil.createFont(fontName,helpFontSize);
    }

    public void setTileFont(File fontFile){
        this.titleFont=FontUtil.createFont(fontFile).deriveFont((float)getTitleFontSize());
        this.titleFontSize=titleFont.getSize();
    }

    public void setHelpFont(File fontFile){
        this.helpFont=FontUtil.createFont(fontFile).deriveFont((float)helpFontSize);
    }

    public void setTileFont(File fontFile,int size){
        this.titleFont=FontUtil.createFont(fontFile).deriveFont((float)size);
        this.titleFontSize=size;
    }

    public void setHelpFont(File fontFile,int size){
        this.helpFont=FontUtil.createFont(fontFile).deriveFont((float)size);
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
            int fontSize = getTitleFontSize();
            if(titleFont.getSize()!=fontSize){
                titleFont=titleFont.deriveFont((float)fontSize);
            }
            return titleFont;
        }else {
            Font font=null;
            try {
                if(titleFontSize>0){
                    font=helpFont.deriveFont((float)titleFontSize);
                }else {
                    font=helpFont.deriveFont((float)(helpFontSize+TITLELARGED));
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

    public List<File> getHelpBackground(){
        if(helpBackground!=null && helpBackground.size()>0){
            return helpBackground;
        }
        File file=new File(BotFrameworkConfig.tempFile+"/"+"defaultHelpBackground.png");
        if(!file.exists()){
            BufferedImage bufferedImage=new BufferedImage(650,270,BufferedImage.TYPE_INT_ARGB);
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
        this.helpBackground= Collections.singletonList(file);
        return this.helpBackground;
    }
}
