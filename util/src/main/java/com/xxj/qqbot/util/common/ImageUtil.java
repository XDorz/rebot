package com.xxj.qqbot.util.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.xxj.qqbot.util.botconfig.functioncompent.HelpImageConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.MenuImageConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.Image;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ImageUtil {

    public static final int UPPER_LEFT=1;
    public static final int UPPER_RIGHT=2;
    public static final int UPPER_CENTER=3;
    public static final int DOWN_LEFT=4;
    public static final int DOWN_RIGHT=5;
    public static final int DOWN_CENTER=6;
    public static final int LEFT_CENTER=7;
    public static final int RIGHT_CENTER=8;
    public static final int CENTER=9;
    private static int nonColor=16777215;

    public static final int STRING_HORIZONTAL=10;
    public static final int STRING_VERTICAL=11;
    public static final int STRING_POSITIVE=12;
    public static final int STRING_REVERSE=13;
    public static final int STRING_CENTER=14;
    public static final int STRING_LEFT=15;
    public static final int STRING_RIGHT=16;
    public static final int STRING_UP=17;
    public static final int STRING_DOWN=18;

    //0整个表达式，1包含括号的颜色表达，2颜色表达，3颜色内容
    private static final Pattern colorFontPattern=Pattern.compile("([\\(,（](.+?)[\\),）])?\\{(.+?)\\}");
    private static final Pattern numPattern=Pattern.compile("\\d+");
    private static final String colorNumPattern="1[0-9][0-9]|2[0-4][0-9]|25[0-5]|[0-9][0-9]|[0-9]";
    private static final Pattern colorPattern=Pattern.compile("("+colorNumPattern+")[,，.。]("+colorNumPattern+")[,，.。]("+colorNumPattern+")([,，.。]([0-9][0-9]|100|[0-9]))?");
    private static final Color DEFAULTIMPORTANTCOLOR=Color.RED;

    public static final String globalBanned="green";
    public static final String banned="red";

    /**
     * 核心文字绘制方法
     *
     * @param image                     原始图像
     * @param font                      书写文字
     * @param val                       书写内容
     * @param x                         起始位置
     * @param y                         起始位置
     * @param width                     绘制画布宽度
     * @param height                    绘制画布高度
     * @param rowSpacing                行间距
     * @param colSpacing                列间距
     * @param color                     文字颜色
     * @param isHorizontal              是否横向排列
     * @param isPositive                文字是否反序
     * @param isCenter                  是否居中文字
     * @return
     */
    public static BufferedImage drawString(BufferedImage image, Font font, String val, int x, int y, int width, int height, int rowSpacing, int colSpacing, Color color, boolean isHorizontal, boolean isPositive, boolean isCenter, boolean isLeft, boolean isRight, boolean isUp, boolean isDown){
        BufferedImage newImage = copyImage(image);
        Graphics2D graphics = (Graphics2D)newImage.getGraphics();
        graphics.setFont(font);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_DEFAULT);
        graphics.setPaint(color);
        val=val.trim();
        char[] chars = val.toCharArray();
        int max = chars.length-1;
        int now=-1;
        FontMetrics metrics = graphics.getFontMetrics();
        int h=metrics.getHeight();
        int standardWidth = metrics.charWidth('你');
        y+=metrics.getMaxAscent();
        FontDistribution distribution = calculateFontLocation(val, metrics, width, height, rowSpacing, colSpacing, isHorizontal, isPositive, isCenter, isLeft, isRight, isUp, isDown);
        if(isHorizontal){
            //保证绘制不会越过设定边界
            width-=standardWidth+distribution.getWidthSpace()-2;
            height-=h+distribution.getHeightSpace()-2;
            //横向排列
            if(isPositive){
                x+=distribution.getWidthSpace();
                y+=distribution.getHeightSpace();
                loop1:
                for (int j = 0; j < height;) {
                    for (int i = 0; i < width;) {
                        now++;
                        if(chars[now]=='\n') break ;
                        graphics.drawString(String.valueOf(chars[now]),x+i,y+j);
                        i+=metrics.charWidth(chars[now])+colSpacing;
                        if(now==max) break loop1;
                    }
                    j+=h+rowSpacing;
                }
            }else {
                loop2:
                for (int j = 0; j < height;) {
                    for (int i = width; i > 0;) {
                        now++;
                        if(chars[now]=='\n') break ;
                        graphics.drawString(String.valueOf(chars[now]),x+i,y+j);
                        i-=metrics.charWidth(chars[now])+colSpacing;
                        if(now==max) break loop2;
                    }
                    j+=h+rowSpacing;
                }
            }
        }else {
            width-=standardWidth+Math.abs(distribution.getWidthSpace())-2;
            height-=h+Math.abs(distribution.getHeightSpace())-2;
            if(isPositive){
                x+=distribution.getWidthSpace();
                y+=distribution.getHeightSpace();
                loop3:
                for (int i = 0; i < width;) {
                    for (int j = 0; j < height;) {
                        now++;
                        if(chars[now]=='\n') break;
                        graphics.drawString(String.valueOf(chars[now]),x+i,y+j);
                        j+=h+rowSpacing;
                        if(now==max) break loop3;
                    }
                    i+=standardWidth+colSpacing;
                }
            }else {
                loop4:
                for (int i = width; i > 0;) {
                    for (int j = 0; j < height;) {
                        now++;
                        if(chars[now]=='\n') break;
                        graphics.drawString(String.valueOf(chars[now]),x+i,y+j);
                        j+=h+rowSpacing;
                        if(now==max) break loop4;
                    }
                    i-=standardWidth+colSpacing;
                }
            }
        }
        graphics.dispose();
        return newImage;
    }

    /**
     * widthSpace与heightSpace将不返回负值
     * 如果 非isPositive 即反向排列，应从在x中减去widthSpace值
     */
    @Data
    static class FontDistribution{
        private int widthSpace;
        private int heightSpace;
        private int width;
        private int height;
        private int colNum;
        private int rowNum;
    }
    private static FontDistribution calculateFontLocation(String val,FontMetrics metrics,int width,int height,int rowSpacing, int colSpacing,boolean isHorizontal, boolean isPositive, boolean isCenter, boolean isLeft, boolean isRight, boolean isUp, boolean isDown){
        int h=metrics.getHeight();
        int standardWidth = metrics.charWidth('你');
        FontDistribution distribution=new FontDistribution();
        if(isHorizontal){
            int space=0;
            if(isCenter||isLeft||isRight||isDown){
                int i=0,num=0;
                //每行的列数
                //todo 此处日后需优化   全部模拟获得宽高信息  目前英文和标点会造成文字高度无法正常居中
                while (true){
                    i+=standardWidth;
                    if(i>width) break;
                    i+=colSpacing;
                    num++;
                }
                num=Math.min(num,val.length());
                distribution.setColNum(num);
                int fWidth=num*standardWidth+(num-1)*colSpacing;
                distribution.setWidth(fWidth);
                space=width-fWidth;
                int count=0,mx=num;
                //文本预计行数
                num=1;
                for (int j = 0; j < val.length(); j++) {
                    count++;
                    if(val.charAt(j)=='\n'||count>mx){
                        if(count>mx) j--;
                        count=0;
                        num++;
                    }
                }
                distribution.setRowNum(num);
                int fHeight=num*h+(num-1)*rowSpacing;
                distribution.setHeight(fHeight);
                if (isDown){
//                    distribution.setHeightSpace(height-fHeight);
                    distribution.setHeightSpace(Math.max(0,height-fHeight));
                }else if(isCenter&&!isUp){
//                    distribution.setHeightSpace((height-fHeight)/2);
                    distribution.setHeightSpace(Math.max(0,(height-fHeight)/2));
                }else {
                    distribution.setHeightSpace(0);
                }
            }
            if(isPositive){
                //正向排列
                if(isRight){
//                    distribution.setWidthSpace(space);
                    distribution.setWidthSpace(Math.max(0,space));
                }else if(isCenter&&!isLeft){
//                    distribution.setWidthSpace(space/2);
                    distribution.setWidthSpace(Math.max(0,space/2));
                }else {
                    distribution.setWidthSpace(0);
                }
            }else {
                if(isLeft){
//                    distribution.setWidthSpace(-space);
                    distribution.setWidthSpace(Math.max(0,space));
                }else if(isCenter&&!isRight){
//                    distribution.setWidthSpace(-space/2);
                    distribution.setWidthSpace(Math.max(0,space/2));
                }else {
                    distribution.setWidthSpace(0);
                }
            }
        }else {
            int space=0;
            if(isCenter||isLeft||isRight||isUp||isDown){
                int i=0,num=0;
                //文本预计的行数
                while (true){
                    i+=h;
                    if(i>height) break;
                    i+=rowSpacing;
                    num++;
                }
                num=Math.min(num,val.length());
                distribution.setRowNum(num);
                int fHeight=num*h+(num-1)*rowSpacing;
                distribution.setHeight(fHeight);
                if(isDown){
//                    distribution.setHeightSpace(height-fHeight);
                    distribution.setHeightSpace(Math.max(0,height-fHeight));
                }else if(isCenter&&!isUp){
//                    distribution.setHeightSpace((height-fHeight)/2);
                    distribution.setHeightSpace(Math.max(0,(height-fHeight)/2));
                }else {
                    distribution.setWidthSpace(0);
                }
                int count=0,mx=num;
                //文本预计列数
                num=1;
                for (int j = 0; j < val.length(); j++) {
                    count++;
                    if(val.charAt(j)=='\n'||count>mx){
                        if(count>mx) j--;
                        count=0;
                        num++;
                    }
                }
                distribution.setColNum(num);
                int fWidth=num*standardWidth+(num-1)*colSpacing;
                distribution.setWidth(fWidth);
                space=width-fWidth;
            }
            if(isPositive){
                if(isRight){
//                    distribution.setWidthSpace(space);
                    distribution.setWidthSpace(Math.max(0,space));
                }else if(isCenter&&!isLeft){
//                    distribution.setWidthSpace(space/2);
                    distribution.setWidthSpace(Math.max(0,space/2));
                }else {
                    distribution.setWidthSpace(0);
                }
            }else {
                if(isLeft){
//                    distribution.setWidthSpace(-space);
                    distribution.setWidthSpace(Math.max(0,space));
                }else if(isCenter&&!isRight){
//                    distribution.setWidthSpace(-space/2);
                    distribution.setWidthSpace(Math.max(0,space/2));
                }else {
                    distribution.setWidthSpace(0);
                }
            }
        }
        return distribution;
    }

    /**
     * @param types                     排列类型
     * @return
     */
    public static BufferedImage drawString(BufferedImage image,Font font,String val,int x,int y,int width,int height,int rowSpacing,int colSpacing,Color color,int... types){
        boolean isCenter=false;
        boolean isHorizontal=true;
        boolean isPositive=true;
        boolean isLeft=false;
        boolean isRight=false;
        boolean isUp=false;
        boolean isDown=false;
        for (int type : types) {
            switch (type){
                case STRING_HORIZONTAL:
                    isHorizontal=true;
                    break;
                case STRING_VERTICAL:
                    isHorizontal=false;
                    break;
                case STRING_POSITIVE:
                    isPositive=true;
                    break;
                case STRING_REVERSE:
                    isPositive=false;
                    break;
                case STRING_CENTER:
                    isCenter=true;
                    break;
                case STRING_LEFT:
                    isLeft=true;
                    break;
                case STRING_RIGHT:
                    isRight=true;
                    break;
                case STRING_UP:
                    isUp=true;
                    break;
                case STRING_DOWN:
                    isDown=true;
                    break;
            }
        }
        return drawString(image,font,val,x,y,width,height,rowSpacing,colSpacing,color,isHorizontal,isPositive,isCenter,isLeft,isRight,isUp,isDown);
    }

    /**
     * @param size                  文字大小
     */
    public static BufferedImage drawString(BufferedImage image,Font font,float size,String val,int x,int y,int width,int height,int rowSpacing,int colSpacing,Color color,int... types){
        Font nfont=font.deriveFont(size);
        return drawString(image,nfont,val,x,y,width,height,rowSpacing,colSpacing,color,types);
    }

    public static BufferedImage drawString(BufferedImage image, Font font, float size, String val, int x, int y, int width, int height, Color color, int... types){
        return drawString(image,font,size,val,x,y,width,height,10,10,color,types);
    }

    /**
     * 核心画法
     *
     * @param image             原图
     * @param addImage          待加入的图
     * @param x                 起始位置
     * @param y                 结束位置
     * @param width             绘画宽度
     * @param height            绘画高度
     * @return
     */
    public static BufferedImage draw(BufferedImage image,BufferedImage addImage,int x,int y,int width,int height){
        BufferedImage newImage = copyImage(image);
        if(x+width>image.getWidth()||y+height>image.getHeight()||y<0||x<0){
            throw new IndexOutOfBoundsException("绘制图片越界！！！");
        }
        Graphics2D graphics = (Graphics2D) newImage.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_DEFAULT);
        graphics.drawImage(addImage,x,y,width,height,null);
        graphics.dispose();
//        for (int j = 0; j < height; j++) {
//            for (int i = 0; i < width; i++) {
//                int rgb = addImage.getRGB(i, j);
//                if(judgeRgb(rgb)){
//                    newImage.setRGB(x+i,y+j,rgb);
//                }
//            }
//        }
        return newImage;
    }

    /**
     * 在中心格降低透明度
     */
    public static BufferedImage changeCenterAlpha(BufferedImage image,int x,int y,int alpha,boolean addBackground){
        BufferedImage newImage = copyImage(image);
        if(y<0||x<0||y>image.getHeight()-1||x>image.getWidth()-1||alpha>255||alpha<0){
            throw new IndexOutOfBoundsException("绘制图片越界！！！");
        }
        BufferedImage background=null;
        if(addBackground) background=new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int height=image.getHeight()-2*y;
        int width=image.getWidth()-2*x;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int rgb = newImage.getRGB(x+i, y+j);
                if(!judgeRgb(rgb)) continue;
                if(sameRGB(rgb)) continue;
                rgb=(alpha<<24)|(rgb&0x00ffffff);
                newImage.setRGB(x+i,y+j,rgb);
                if (addBackground) background.setRGB(x+i,y+j,Color.WHITE.getRGB());
            }
        }
        if (addBackground) return draw(background,newImage,0,0);
        return newImage;
    }

    public static BufferedImage changeCenterAlpha(File image,int x,int y,int alpha,boolean addBackground){
        BufferedImage img=null;
        try {
            img=ImageIO.read(image);
        } catch (IOException e) {
            log.error(image.getAbsolutePath()+"无法读取！");
            return null;
        }
        return changeCenterAlpha(img,x,y,alpha,addBackground);
    }

    /**
     * 在原图上从传入位置绘制整幅图
     *
     * @param image                 原图
     * @param addImage              待加入的图
     * @param x                     起始位置
     * @param y                     结束位置
     * @return
     */
    public static BufferedImage draw(BufferedImage image,BufferedImage addImage,int x,int y){
        return draw(image,addImage,x,y,addImage.getWidth(),addImage.getHeight());
    }

    public static BufferedImage draw(File image,File addImage,int x,int y){
        BufferedImage img=null;
        BufferedImage addImg=null;
        try {
            img=ImageIO.read(image);
        } catch (IOException e) {
            log.error(image.getAbsolutePath()+"无法读取！");
            return null;
        }
        try {
            addImg=ImageIO.read(addImage);
        } catch (IOException e) {
            log.error(addImage.getAbsolutePath()+"无法读取！");
            return null;
        }
        return draw(img,addImg,x,y);
    }

    public static BufferedImage draw(File image,BufferedImage addImage,int x,int y){
        BufferedImage img=null;
        try {
            img=ImageIO.read(image);
        } catch (IOException e) {
            log.error(image.getAbsolutePath()+"无法读取！");
            return null;
        }
        return draw(img,addImage,x,y);
    }

    /**
     * 将一幅图绘制到另一幅图上
     * 忽略png格式的空白
     *
     * @param image                 原图
     * @param addImage              待加入的图
     * @param drawType              绘入位置类型
     * @return
     */
    public static BufferedImage draw(BufferedImage image,BufferedImage addImage,int drawType){
        if(image==null||addImage==null) return null;
        int h = image.getHeight();
        int w = image.getWidth();
        int h1 = addImage.getHeight();
        int w1 = addImage.getWidth();
        int x=0,y=0;
        if(drawType==UPPER_RIGHT){
            x=w-w1-1;
        }
        if(drawType==UPPER_CENTER){
            x=(w-w1)/2;
        }
        if(drawType==DOWN_LEFT){
            y=h-h1-1;
        }
        if(drawType==DOWN_RIGHT){
            x=w-w1-1;
            y=h-h1-1;
        }
        if(drawType==DOWN_CENTER){
            x=(w-w1)/2;
            y=h-h1-1;
        }
        if(drawType==LEFT_CENTER){
            y=(h-h1)/2;
        }
        if(drawType==RIGHT_CENTER){
            x=w-w1-1;
            y=(h-h1)/2;
        }
        if(drawType==CENTER){
            x=(w-w1)/2;
            y=(h-h1)/2;
        }
        return draw(image,addImage,x,y,addImage.getWidth(),addImage.getHeight());
    }

    /**
     * @param image                 原图
     * @param addImage              待加入的图
     * @param type                  绘入位置类型
     * @return
     */
    public static BufferedImage draw(File image,File addImage,int type){
        BufferedImage img=null;
        BufferedImage addImg=null;
        try {
            img=ImageIO.read(image);
        } catch (IOException e) {
            log.error(image.getAbsolutePath()+"无法读取！");
            return null;
        }
        try {
            addImg=ImageIO.read(addImage);
        } catch (IOException e) {
            log.error(addImage.getAbsolutePath()+"无法读取！");
            return null;
        }
        return draw(img,addImg,type);
    }

    /**
     * 核心帮助画法
     *
     * @param context
     * @param config
     * @return
     */
    public static BufferedImage drawHelp(String context,HelpImageConfig config,BufferedImage background){
        background=copyImage(background);
        Graphics2D board = (Graphics2D)background.getGraphics();
        board.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        board.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        board.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_DEFAULT);
        int bheight = background.getHeight();
        int bwidth = background.getWidth();
        int height=-1,width=-1;
        int x=50,y=0;
        boolean autoHeight=false;

        //x与width确定
        if(config.getWidth()>0){
            width=config.getWidth();
        }
        if(config.getX()>=0){
            x=config.getX();
            if(width<1) width=bwidth-2*x;
        }else if(config.getXPercent()!=-1f) {
            x=(int)(bwidth*config.getXPercent());
            if(width<1) width=bwidth-2*x;
        }else {
            if(width<1) width=bwidth-2*x;
        }
        //y与height确定
        if(config.getHeight()>0){
            height=config.getHeight();
        }
        if(config.getY()>=0){
            y=config.getY();
            if(height<1) height=bheight-2*y;
        }else if(config.getYPercent()!=-1f) {
            y=(int)(bheight*config.getYPercent());
            if(height<1) height=bheight-2*y;
        }else {
            if(height<1) height=bheight-2*y;
        }
        if(config.isAutoHeight()) autoHeight=true;

        ColorFontInfo colorFontInfo = handleColorFont(context);
        board.setFont(config.getTitleFont());
        String full=config.getTitleValue()+"\n"+colorFontInfo.getContext();
        int titleFontH = board.getFontMetrics().getHeight();
        int helpFontH=board.getFontMetrics(config.getHelpFont()).getHeight();
        int amend=(titleFontH-helpFontH);
        FontDistribution distribution = calculateFontLocation(
                full,
                board.getFontMetrics(config.getHelpFont()),
                width,
                height,
                config.getRowSpace(),
                config.getColSpace(),
                true,
                true,
                autoHeight,
                false,
                true,
                false,
                false);
        x+=distribution.getWidthSpace();
        y+=distribution.getHeightSpace()-amend;
        y+=board.getFontMetrics().getAscent();
        board.setColor(config.getTitleFontDefaultColor());
        board.drawString(config.getTitleValue(),Math.max(0,x-config.getTitleProgress()),y);
        y+=titleFontH+config.getRowSpace()-board.getFontMetrics().getAscent();
        width=width-distribution.getWidthSpace()+1;
//        width-=standardFontWidth;
        height=height-distribution.getHeightSpace()-titleFontH-config.getRowSpace()+1;
//        height-=helpFontH;
        board.setFont(config.getHelpFont());
        drawColorFullString(x,y,width,height,config.getRowSpace(),config.getColSpace(),context,config.getContextFontDefaultColor(),board);
        board.dispose();
        return background;
    }

    /**
     * 在画板上绘制彩色字体，该方法在传入的画板上直接绘制，返回一个绘制结果，注意需要在使用前为board设置好字体
     *
     * @param x                 起始x位
     * @param y                 起始y位
     * @param width             总宽度限制
     * @param height            总高度限制
     * @param rowSpace          行间距
     * @param colSpace          字间距
     * @param context           绘制内容
     * @param defaultColor      默认颜色
     * @param board             绘制画板
     * @return
     */
    private static DrawResult drawColorFullString(int x,int y,int width,int height,int rowSpace,int colSpace
            ,String context,Color defaultColor,Graphics2D board){
        DrawResult drawResult=new DrawResult();
        ColorFontInfo colorFontInfo = handleColorFont(context);
        String str = colorFontInfo.getContext();
        ColorfulFont handler=ColorfulFont.init(colorFontInfo,defaultColor);
        FontMetrics metrics = board.getFontMetrics();
        boolean flag=false;
        int fontHeight=metrics.getHeight();
        int standardWidth=metrics.charWidth('你');
        y+=metrics.getMaxAscent();
        height=height-fontHeight+1;
        width=width-standardWidth+1;
        int n=0,maxWidth=-1,w=0,cn=0,maxColNum=-1,rowNum=0;
        loop:
        for (int j = 0; j < height; ) {
            for (int i = 0; i < width; ) {
                if(n>=str.length()) {
                    rowNum++;
                    flag=true;
                    if(cn>maxColNum) {maxColNum=cn;}
                    if(w>maxWidth) {maxWidth=w;}
                    drawResult.setEndX(x+i-colSpace);
                    break loop;
                }
                char c = str.charAt(n);
                Color fontColor = handler.getFontColor(n);
                if(fontColor!=null) board.setColor(fontColor);
                if(c=='\n'){
                    n++;
                    break ;
                }
                board.drawString(String.valueOf(c),x+i,y+j);
                i+=metrics.charWidth(c)+colSpace;
                cn++;
                w=i;
                n++;
            }
            rowNum++;
            w-=colSpace;
            drawResult.setEndX(w);
            if(cn>maxColNum) {maxColNum=cn;}
            if(w>maxWidth) {maxWidth=w;}
            cn=0;
            w=0;
            j+=fontHeight+rowSpace;
        }
        drawResult.setColNum(maxColNum);
        drawResult.setRowNum(rowNum);
        drawResult.setHeight(rowNum*fontHeight+(rowNum-1)*rowSpace);
        drawResult.setWidth(maxWidth);
        drawResult.setSuccess(flag);
        drawResult.setEndY(y+drawResult.getHeight()-metrics.getMaxAdvance());
        return drawResult;
    }
    @Data
    private static class DrawResult{
        int colNum;
        int rowNum;
        int width;
        int height;
        int endX;
        int endY;
        boolean success;
    }

    /**
     * 给予文字位置，获得该文字的颜色信息
     */
    static class ColorfulFont{
        private ColorFontInfo info;
        private List<Integer> start;
        private List<Integer> end;
        private List<Color> color;
        private int s,e;
        private Color c,nowcolor;
        private final Color defaultColor;
        private int size,now;
        private ColorfulFont(ColorFontInfo info,Color contextColor){
            this.info=info;
            this.start=info.getStart();
            this.end=info.getEnd();
            this.color=info.getColor();
            this.size=info.getStart().size();
            this.now=0;
            defaultColor=contextColor==null?Color.BLACK:contextColor;
            if(size>0){
                s=start.get(now);
                e=end.get(now);
                c=color.get(now);
            }else {
                s=0;
                e=0;
                c=defaultColor;
            }
        }

        static ColorfulFont init(ColorFontInfo info,Color contextColor){
            return new ColorfulFont(info,contextColor);
        }

        public Color getFontColor(int location){
            Color re=null;
            if(nowcolor==null){
                nowcolor=defaultColor;
                re=defaultColor;
            }
            if(location==e){
                now++;
                if(now<size){
                    s=start.get(now);
                    e=end.get(now);
                    c=color.get(now);
                }
                nowcolor=defaultColor;
                re=defaultColor;
            }
            if(location>=s&&location<e){
                if(c.equals(nowcolor)){
                    return null;
                }
                nowcolor=c;
                return nowcolor;
            }else {
                if(!defaultColor.equals(nowcolor)){
                    nowcolor=defaultColor;
                    return defaultColor;
                }
            }

            return re;
        }
    }

    /**
     * 核心菜单画法
     */
    public static BufferedImage drawMenu(Map<String, List<String>> menuMap, MenuImageConfig config, BufferedImage background){
        int x=20,y=30;
        background=copyImage(background);
        Graphics2D board = (Graphics2D)background.getGraphics();
        board.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        board.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        board.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_DEFAULT);
        board.setFont(config.getTipFont());
        DrawResult result = drawColorFullString(x, y, background.getWidth() - 2 * x, background.getHeight() - 2 * y, config.getTipRowSpace(),
                config.getTipColSpace(), config.getTip(), Color.BLACK, board);
        if(menuMap.size()<1) {
            board.dispose();
            return background;
        }

        FontMetrics titleMetrics = board.getFontMetrics(config.getTitleFont());
        FontMetrics valMetrics = board.getFontMetrics(config.getMenuValFont());
        int valH=valMetrics.getHeight();
        Map<String, MenuMapInfo> menuInfoMap = menuMapAnalysis(menuMap, config, titleMetrics, valMetrics);
        int backgroundH=background.getHeight()-result.getEndY()-2*config.getBlockVerticalPadding()-valMetrics.getHeight();
        int backgroundW=background.getWidth()-2*config.getBlockHorizontalMargin()-valMetrics.charWidth('你');
        List<MenuMapInfo> hSortedInfos=new ArrayList<>();
        for (String key : menuInfoMap.keySet()) {
            hSortedInfos.add(menuInfoMap.get(key));
        }
        List<MenuMapInfo> wSortedInfos=new ArrayList<>(hSortedInfos);
        hSortedInfos.sort((before,after) -> after.getTotalHeight()-before.getTotalHeight());
        wSortedInfos.sort((before,after) -> after.getTotalWidth()-before.getTotalWidth());
        int num=0,totalw=0;
        while (totalw<=backgroundW-config.getBlockHorizontalMargin() && num<wSortedInfos.size()){
            totalw+=wSortedInfos.get(num).getTotalWidth();
            totalw+=config.getBlockHorizontalMargin();
            num++;
        }
        num--;
        num=Math.max(1,num);
        int totalH=0,averageH=-1,adviceMaxH=0,adviceMinH=0;
        for (MenuMapInfo hSortedInfo : hSortedInfos) {
            totalH+=hSortedInfo.getTotalHeight();
        }
        averageH=totalH/num;
        adviceMaxH=Math.min(backgroundH-config.getBlockVerticalPadding(),(int)(averageH*(1+config.getMenuMaxTolerancePercentH())));
        adviceMinH=Math.min((int)(backgroundH*(1-config.getMenuMinTolerancePercentH())),(int)(averageH*(1-config.getMenuMinTolerancePercentH())));
        MenuBlockAnalysisResult blockAnalysis = menuBlockAnalysis(backgroundW, backgroundH, hSortedInfos, config, adviceMaxH, adviceMinH, titleMetrics, valMetrics);
        board.drawImage(blockAnalysis.getOverride(),config.getBlockHorizontalMargin(),result.getEndY()+config.getBlockVerticalPadding(),null);
        int offset = blockAnalysis.getOffset();
        Color titleFontColor=config.getDefaultTitleFontColor();
        for (MenuBlockInfo info : blockAnalysis.getInfos()) {
            if(info.getTitleX()>0 && info.getTitleY()>0){
                board.setFont(config.getTitleFont());
                board.setColor(titleFontColor);
                String title = info.getContext().getTitle();
                int titleX=info.getTitleX()+offset+config.getBlockHorizontalMargin();
                int titleY=info.getTitleY()+result.getEndY()+config.getBlockVerticalPadding();
                for (int i = 0; i < title.length(); i++) {
                    char c = title.charAt(i);
                    board.drawString(String.valueOf(c),titleX,titleY);
                    titleX+=titleMetrics.charWidth(c)+config.getTipColSpace();
                }
            }

            MenuMapInfo context = info.getContext();
            List<String> menuVal = context.getContext();
            List<Integer> overflowLocation = context.getOverflowLocation();
            List<Integer> colorFontLocation = context.getColorFontLocation();
            List<Color> fontColor = context.getColorFont();
            Color defaultColor=config.getDefaultMenuFontColor()==null?info.getFontDefaultColor():config.getDefaultMenuFontColor();
            Color nowColor=defaultColor;
            board.setColor(nowColor);

            int start=0,end=menuVal.size()-1;
            int valX=info.getValStartX()+offset+config.getBlockHorizontalMargin(),valY=info.getValStartY()+result.getEndY()+config.getBlockVerticalPadding();
            int[] range = info.getValRange();
            if(range[0]>0 || range[1]>0){
                start=Math.max(0,range[0]);
                if(range[1]>0) {end=range[1];}
            }
            int overL=0,colorL=0;
            while (colorL<colorFontLocation.size()) {
                if(colorFontLocation.get(colorL)>=start) { break; }
                colorL++;
            }
            while (overL<overflowLocation.size()) {
                if(overflowLocation.get(overL)>=start) { break; }
                overL++;
            }
            boolean overb=overL<overflowLocation.size();
            boolean colorb=colorL<colorFontLocation.size();
            board.setFont(config.getMenuValFont());
            for (int i = start; i <= end; i++) {
                String val = menuVal.get(i);

                if(colorb && i==colorFontLocation.get(colorL)){
                    if(!nowColor.equals(fontColor.get(colorL))){
                        board.setColor(fontColor.get(colorL));
                        nowColor=fontColor.get(colorL);
                    }
                    colorL++;
                    colorb= colorL != colorFontLocation.size();
                }else {
                    if(!nowColor.equals(defaultColor)){
                        board.setColor(defaultColor);
                        nowColor=defaultColor;
                    }
                }
                String numString=(i+1)+".";
                board.drawString(numString,valX,valY);
                valX+=valMetrics.stringWidth(numString)+config.getValColSpace();
                for (int j = 0; j < val.length(); j++) {
                    char c = val.charAt(j);
                    board.drawString(String.valueOf(c),valX,valY);
                    valX+=valMetrics.charWidth(c)+config.getValColSpace();
                }
                if(overb && i==overflowLocation.get(overL)){
                    board.drawString("...",valX-config.getValColSpace(),valY);
                    overL++;
                    overb= overL != overflowLocation.size();
                }
                valY+=valH+config.getValRowSpace();
                valX=info.getValStartX()+offset+config.getBlockHorizontalMargin();
            }
        }
        board.dispose();
        return background;
    }
    private static Map<String,MenuMapInfo> menuMapAnalysis(Map<String, List<String>> menuMap,MenuImageConfig config,FontMetrics titleMetrics,FontMetrics valMetrics){
        Map<String,MenuMapInfo> resultMap=new HashMap<>();
        int titleH = titleMetrics.getHeight();
        int valH = valMetrics.getHeight();
        int maxValW=valMetrics.charWidth('你')*config.getMaxValRowLength()+(config.getMaxValRowLength()-1)*config.getValColSpace();
        for (String key : menuMap.keySet()) {
            MenuMapInfo info=new MenuMapInfo();
            List<String> functions = menuMap.get(key);
            int i=0,maxW=-1,w=0,j=1;
            for (String function : functions) {
                Matcher matcher = colorFontPattern.matcher(function);
                if(matcher.matches()){
                    info.getColorFont().add(getMatcherFontColor(matcher));
                    info.getColorFontLocation().add(i);
                    function=matcher.group(3);
                }
                int numWidth = valMetrics.stringWidth(j + ".")+config.getValColSpace();
//                function=j+"."+function;
                w=valMetrics.stringWidth(function)+(function.length()-1)*config.getValColSpace();
                if(function.length()>config.getMaxValRowLength()&&w>maxValW){
                    function=function.substring(0,config.getMaxValRowLength()-2);
                    info.getOverflowLocation().add(i);
                    w=valMetrics.stringWidth(function)+(function.length()-1)*config.getValColSpace();
                    w+=valMetrics.stringWidth("...");
                }
                w+=numWidth;
                info.getContext().add(function);
                if(w>maxW){
                    maxW=w;
                }
                i++;
                j++;
            }
            info.setTitle(key);
            info.setHeight(valH*functions.size()+config.getValRowSpace()*(functions.size()-1));
            info.setWidth(maxW);
            info.setBlockHeight(info.getHeight()+2*config.getBlockVerticalPadding());
            info.setTotalHeight(info.getBlockHeight()+titleH+2*config.getBlockVerticalMargin());
            info.setTotalWidth(info.getWidth()+2*config.getBlockVerticalPadding());
            resultMap.put(key,info);
        }
        return resultMap;
    }
    @Data
    private static class MenuMapInfo{
        List<Integer> colorFontLocation;
        List<Color> colorFont;
        List<String> context;
        List<Integer> overflowLocation;
        String title;
        int width;
        int height;
        int totalWidth;
        int totalHeight;
        int blockHeight;

        private MenuMapInfo(){
            colorFontLocation=new ArrayList<>();
            colorFont=new ArrayList<>();
            context=new ArrayList<>();
            overflowLocation=new ArrayList<>();
        }
    }
    /**
     * 该方法会将返回值移除出集合！！！
     */
    private static MenuMapInfo getInRangeHeightInfo(Iterable<MenuMapInfo> original,int maxHeight){
        Iterator<MenuMapInfo> ite = original.iterator();
        MenuMapInfo info=null;
        while (ite.hasNext()){
            info=ite.next();
            if(info.getTotalHeight()<=maxHeight){
                ite.remove();
                return info;
            }
        }
        return null;
    }

    /**
     * 自动排布功能描述块，返回一张绘制了黑白背景块的覆盖膜
     * 同时为每块文字标注首个绘制坐标
     */
    private static MenuBlockAnalysisResult menuBlockAnalysis(int width,int height,List<MenuMapInfo> infos,MenuImageConfig config,int maxH,int minH,FontMetrics titleMetrics,FontMetrics valMetrics){
        MenuBlockAnalysisResult result=new MenuBlockAnalysisResult(width,height);
        List<MenuBlockInfo> blockInfos=new ArrayList<>();
        List<ImageBackgroundBlockInfo> imageAlphaInfos=new ArrayList<>();
        result.setInfos(blockInfos);
        BufferedImage image = result.getOverride();
        int maxW=-1,totalW=-config.getBlockHorizontalMargin(),type=0,nowInfo=0,h=minH,rangeH=maxH-minH;
        int titleFontH=titleMetrics.getHeight();
        int valFontH=valMetrics.getHeight();
        int titleMaxAscent=titleMetrics.getMaxAscent();
        int valMaxAscent=valMetrics.getMaxAscent();
        MenuMapInfo menuMapInfo=null;
        int x=0,y=0;
        while (true){
            int[] range=new int[2];
            if(infos.size()==0 && menuMapInfo==null) { break; }
            if(!config.isThrowExpWhileOverflow() && x>width) {break;}
            MenuBlockInfo blockInfo=new MenuBlockInfo();
            MenuMapInfo mapInfo=null;
            int totalHeight = -1;
            if(menuMapInfo==null) {
                mapInfo=infos.get(0);
                infos.remove(0);
                totalHeight=mapInfo.getTotalHeight();
                range[0]=-1;
            }else {
                mapInfo=menuMapInfo;
                int residue = mapInfo.getContext().size() - nowInfo;
                totalHeight=valFontH*(residue)+(residue-1)*config.getValRowSpace()+2*config.getBlockVerticalPadding();
                range[0]=nowInfo;
            }
            int totalWidth = mapInfo.getTotalWidth();
            if(maxW<totalWidth) {maxW=totalWidth;}
            int titleUsedH=0;
            if(menuMapInfo==null){
                String title = mapInfo.getTitle();
                int titleOffset = totalWidth - titleMetrics.stringWidth(title) - config.getTitleColSpace() * (title.length() - 1);
                blockInfo.setTitleX(x+titleOffset/2);
                blockInfo.setTitleY(y+config.getBlockVerticalMargin()+titleMaxAscent);
                blockInfo.setContext(mapInfo);
                titleUsedH=2*config.getBlockVerticalMargin()+titleFontH;
                h-=titleUsedH;
                totalHeight-=titleUsedH;
            }
            //正好在区间内
            if(totalHeight>=h && totalHeight<=h+rangeH){
                imageAlphaInfos.add(new ImageBackgroundBlockInfo(x,y+titleUsedH,totalWidth,totalHeight,
                        config.getMenuContextBlockAlpha(),type%2==0?Color.black:Color.white,image));
                blockInfo.setValStartX(config.getBlockHorizontalPadding()+x);
                blockInfo.setValStartY(y+titleUsedH+config.getBlockVerticalPadding()+valMaxAscent);
                blockInfo.setFontDefaultColor(type%2==0?Color.white:Color.black);
                type++;
                nowInfo=0;
                menuMapInfo=null;
            }else if(totalHeight>h && totalHeight<height-(minH-h)){
                imageAlphaInfos.add(new ImageBackgroundBlockInfo(x,y+titleUsedH,totalWidth,totalHeight,
                        config.getMenuContextBlockAlpha(),type%2==0?Color.black:Color.white,image));
                blockInfo.setValStartX(config.getBlockHorizontalPadding()+x);
                blockInfo.setValStartY(y+titleUsedH+config.getBlockVerticalPadding()+valMaxAscent);
                blockInfo.setFontDefaultColor(type%2==0?Color.white:Color.black);
                type++;
                nowInfo=0;
                menuMapInfo=null;
            }else if(totalHeight<h){
                imageAlphaInfos.add(new ImageBackgroundBlockInfo(x,y+titleUsedH,totalWidth,totalHeight,
                        config.getMenuContextBlockAlpha(),type%2==0?Color.black:Color.white,image));
                blockInfo.setValStartX(config.getBlockHorizontalPadding()+x);
                blockInfo.setValStartY(y+titleUsedH+config.getBlockVerticalPadding()+valMaxAscent);
                y+=totalHeight+titleUsedH;
                h-=totalHeight;
                blockInfo.setFontDefaultColor(type%2==0?Color.white:Color.black);
                type++;
                nowInfo=0;
                menuMapInfo=null;
                blockInfos.add(blockInfo);

                List<MenuMapInfo> copyList=new ArrayList<>(infos);
                int copyh=h;
                while (copyh>0 && infos.size()>0){
                    MenuMapInfo rangedInfo = getInRangeHeightInfo(copyList, h + rangeH);
                    if(rangedInfo==null) { break; }
                    copyh-=rangedInfo.getTotalHeight();
                }
                //填上去之后仍不满最小范围则使用最大的那个
                if(copyh>0 && copyList.size()!=0){
                    continue;
                }
                while (h>=0 && infos.size()>0){
                    MenuBlockInfo innerBlockInfo=new MenuBlockInfo();
                    MenuMapInfo rangedInfo = getInRangeHeightInfo(infos, h + rangeH);
                    String title = rangedInfo.getTitle();
                    int inTotalWidth=rangedInfo.getTotalWidth();
                    int titleOffset = inTotalWidth - titleMetrics.stringWidth(title) - config.getValColSpace() * (title.length() - 1);
                    innerBlockInfo.setTitleX(x+titleOffset/2);
                    innerBlockInfo.setTitleY(y+config.getBlockVerticalMargin()+titleMaxAscent);
                    titleUsedH=2*config.getBlockVerticalMargin()+titleFontH;

                    imageAlphaInfos.add(new ImageBackgroundBlockInfo(x,y+titleUsedH,inTotalWidth,rangedInfo.getBlockHeight(),
                            config.getMenuContextBlockAlpha(),type%2==0?Color.black:Color.white,image));
                    innerBlockInfo.setValStartX(config.getBlockHorizontalPadding()+x);
                    innerBlockInfo.setValStartY(y+titleUsedH+config.getBlockVerticalPadding()+valMaxAscent);
                    innerBlockInfo.setContext(rangedInfo);
                    innerBlockInfo.setFontDefaultColor(type%2==0?Color.white:Color.black);
                    y+=rangedInfo.getTotalHeight();
                    h-=rangedInfo.getTotalHeight();
                    if(maxW<inTotalWidth) {maxW=inTotalWidth;}
                    type++;
                    blockInfos.add(innerBlockInfo);
                }
            }else if(totalHeight>height-(minH-h)){
                range[0]=0;
                int blockH=2*config.getBlockVerticalPadding();
                h+=config.getValRowSpace()-blockH;
                blockH-=config.getValRowSpace();
                while (h>-(rangeH-config.getBlockVerticalPadding())){
                    nowInfo++;
                    h=h-config.getValRowSpace()-valFontH;
                    blockH+=config.getValRowSpace()+valFontH;
                }
                range[1]=nowInfo-1;
                imageAlphaInfos.add(new ImageBackgroundBlockInfo(x,y+titleUsedH,totalWidth,blockH,
                        config.getMenuContextBlockAlpha(),type%2==0?Color.black:Color.white,image));
                blockInfo.setValStartX(config.getBlockHorizontalPadding()+x);
                blockInfo.setValStartY(y+titleUsedH+config.getBlockVerticalPadding()+valMaxAscent);
                blockInfo.setFontDefaultColor(type%2==0?Color.white:Color.black);
                menuMapInfo=mapInfo;
            }
            y=0;
            h=minH;
            x+=maxW+config.getBlockHorizontalMargin();
            totalW+=maxW+config.getBlockHorizontalMargin();

            blockInfo.setValRange(range);
            blockInfo.setContext(mapInfo);
            blockInfos.add(blockInfo);

            maxW=0;
        }
        final int offset=config.isMenuBlockOffset()?(width-totalW)/2:0;
        imageAlphaInfos.forEach(alphaInfo -> {
            lowAlpha(alphaInfo.getX()+offset,
                    alphaInfo.getY(),
                    alphaInfo.getWidth(),
                    alphaInfo.getHeight(),
                    alphaInfo.getAlpha(),
                    alphaInfo.getBackgroundColor(),
                    image,
                    config.isThrowExpWhileOverflow());
        });
        result.setOffset(offset);
        return result;
    }
    @Data
    static class ImageBackgroundBlockInfo{
        int x;
        int y;
        int width;
        int height;
        int alpha;
        Color backgroundColor;
        BufferedImage operateImage;

        public ImageBackgroundBlockInfo(int x, int y, int width, int height, int alpha, Color backgroundColor, BufferedImage operateImage) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.alpha = alpha;
            this.backgroundColor = backgroundColor;
            this.operateImage = operateImage;
        }
    }

    /**
     * 在原图基础上进行矩形透明度改变
     */
    //todo 圆角矩形
    private static void lowAlpha(int x,int y,int width,int height,int alpha,Color backgroundColor,BufferedImage image,boolean throwExp){
        alpha=Math.min(100,alpha);
        alpha=Math.max(0,alpha);
        int color=backgroundColor==null?Color.white.getRGB():backgroundColor.getRGB();
        color=(color&0x00ffffff) | (alpha<<24);
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                if(throwExp){
                    image.setRGB(x+i,y+j,color);
                }else {
                    image.setRGB(Math.min(image.getWidth()-1,x+i),Math.min(image.getHeight()-1,y+j),color);
                }
            }
        }
    }
    @Data
    private static class MenuBlockInfo{
        private int titleX;
        private int titleY;
        private int valStartX;
        private int valStartY;
        private int[] valRange;
        private Color fontDefaultColor;
        private MenuMapInfo context;

        MenuBlockInfo(){
            this.fontDefaultColor=Color.black;
            this.valRange=new int[] {-1,-1};
        }
    }
    @Data
    private static class MenuBlockAnalysisResult{
        List<MenuBlockInfo> infos;
        BufferedImage override;
        int offset;

        private MenuBlockAnalysisResult(int width,int height){
            this.override=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
            this.offset=0;
        }
    }




    /**
     * 解析文本获得信息
     */
    @Data
    static class ColorFontInfo{
        private String context;
        private List<Integer> start;
        private List<Integer> end;
        private List<Color> color;
        private ColorFontInfo(){
            context="";
            start=new ArrayList<>();
            end=new ArrayList<>();
            color=new ArrayList<>();
        }
    }
    public static ColorFontInfo handleColorFont(String context){
        ColorFontInfo colorFontInfo=new ColorFontInfo();
        List<Integer> start = colorFontInfo.getStart();
        List<Integer> end = colorFontInfo.getEnd();
        List<Color> color = colorFontInfo.getColor();
        Matcher matcher = colorFontPattern.matcher(context);
        while (matcher.find()){
            int front=(matcher.group(0).length()-matcher.group(3).length())-1;
            start.add(matcher.start(3)-front);
            end.add(matcher.end(3)-front);
            color.add(getMatcherFontColor(matcher));
            context = matcher.replaceFirst(matcher.group(3));
            matcher=colorFontPattern.matcher(context);
        }
        colorFontInfo.setContext(context);
        return colorFontInfo;
    }

    /**
     * 获取一段文字的颜色
     */
    private static Color getMatcherFontColor(Matcher matcher){
        if(matcher.group(2)!=null){
            String colr = matcher.group(2);
            Matcher colorMatcher = colorPattern.matcher(colr);
            if(numPattern.matcher(colr).matches()){
                return new Color(Integer.parseInt(colr));
            }else if(colorMatcher.matches()){
                if(colorMatcher.group(5)!=null){
                    return new Color(Integer.parseInt(colorMatcher.group(1))
                            ,Integer.parseInt(colorMatcher.group(2)),Integer.parseInt(colorMatcher.group(3))
                            ,Integer.parseInt(colorMatcher.group(5)));
                }else {
                    return new Color(Integer.parseInt(colorMatcher.group(1))
                            ,Integer.parseInt(colorMatcher.group(2)),Integer.parseInt(colorMatcher.group(3)));
                }
            }else{
                try {
                    Field field = Color.class.getField(colr.toLowerCase());
                    if(Color.class.isAssignableFrom(field.getType())){
                        return (Color)field.get(null);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    log.error("无法通过反射获取名为『"+colr+"』的颜色，请检查Color类中是否含有需求颜色！");
                }
//                switch (colr.toLowerCase()){
//                    case "red":
//                        return Color.RED;
//                    case "green":
//                        return Color.GREEN;
//                    case "yellow":
//                        return Color.YELLOW;
//                    case "blue":
//                        return Color.BLUE;
//                    default:
//                        log.error("不支持的颜色定义，将使用默认颜色！");
//                        return DEFAULTIMPORTANTCOLOR;
//                }
            }
        }else {
            return DEFAULTIMPORTANTCOLOR;
        }
        return DEFAULTIMPORTANTCOLOR;
    }

    /**
     * 复制图片
     */
    public static BufferedImage copyImage(BufferedImage image){
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        BufferedImage newImage = graphics.getDeviceConfiguration().createCompatibleImage(image.getWidth(), image.getHeight(), Transparency.TRANSLUCENT);
        graphics.dispose();
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image,0,0,null);
        g.dispose();
        return newImage;
    }

    private static boolean judgeRgb(int i){
        if(i==16777215||i==0||(i>>>24)<15){
            return false;
        }
        return true;
    }

    private static boolean sameRGB(int i){
        int r=(i&0x00ff0000)>>16;
        int g=(i&0x0000ff00)>>8;
        int b=i&0x000000ff;
        if(r==g&&g==b){
            if(r>248){
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 核心图片下载
     */
    public static InputStream getStreamFromImage(Image image){
        String url = Image.queryUrl(image);
        HttpRequest request = HttpUtil.createGet(url);
        HttpResponse response = request.execute();
        return response.bodyStream();
    }

    public static InputStream getStreamFromImage(String imageId){
        return getStreamFromImage(Image.fromId(imageId));
    }
}
