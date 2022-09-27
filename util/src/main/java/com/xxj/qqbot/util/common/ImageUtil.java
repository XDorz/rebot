package com.xxj.qqbot.util.common;

import com.xxj.qqbot.util.botconfig.functioncompent.HelpImageConfig;
import com.xxj.qqbot.util.botconfig.functioncompent.MenuImageConfig;
import lombok.extern.slf4j.Slf4j;

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
import java.util.List;
import java.util.Map;

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
        char[] chars = val.toCharArray();
        int max = chars.length-1;
        int now=-1;
        FontMetrics metrics = graphics.getFontMetrics();
        int h=metrics.getHeight();
        int standardWidth = metrics.charWidth('你');
        y+=metrics.getMaxAscent();
        if(isHorizontal){
            int space=0;
            if(isCenter||isLeft||isRight||isDown){
                int i=0;
                //文本预计的行数
                int num=0;
                while (true){
                    i+=standardWidth;
                    if(i>width) break;
                    i+=colSpacing;
                    num++;
                }
                space=width-Math.min(num,val.length())*standardWidth-(Math.min(num,val.length())-1)*colSpacing;
                if(val.length()%num==0){
                    num=val.length()/num;
                }else {
                    num=val.length()/num;
                    num++;
                }
                if (isDown){
                    y+=height-num*h-(num-1)*rowSpacing;
                }else if(isCenter&&!isUp){
                    y+=(height-num*h-(num-1)*rowSpacing)/2;
                }
            }
            width-=standardWidth;
            height-=h;
            //横向排列
            if(isPositive){
                if(isRight){
                    x+=space;
                }else if(isCenter&&!isLeft){
                    x+=space/2;
                }
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
                if(isLeft){
                    x-=space;
                }else if(isCenter&&!isRight){
                    x-=space/2;
                }
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
            int space=0;
            if(isCenter||isLeft||isRight||isUp||isDown){
                int i=0;
                //文本预计的列数
                int num=0;
                while (true){
                    i+=h;
                    if(i>height) break;
                    i+=rowSpacing;
                    num++;
                }
                if(isDown){
                    y+=height-Math.min(num,val.length())*h-(Math.min(num,val.length())-1)*rowSpacing;
                }else if(isCenter&&!isUp){
                    y+=(height-Math.min(num,val.length())*h-(Math.min(num,val.length())-1)*rowSpacing)/2;
                }
                if(val.length()%num==0){
                    num=val.length() / num;
                }else {
                    num=val.length() / num;
                    num++;
                }
                space=width-num*standardWidth-(num-1)*colSpacing;
            }
            width-=standardWidth;
            height-=h;
            if(isPositive){
                if(isRight){
                    x+=space;
                }else if(isCenter&&!isLeft){
                    x+=space/2;
                }
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
                if(isLeft){
                    x-=space;
                }else if(isCenter&&!isRight){
                    x-=space/2;
                }
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
     * @param string
     * @param config
     * @return
     */
//    public static BufferedImage drawHelp(String string,HelpImageConfig config,BufferedImage background){
//        //todo 待完成
//    }
//
//    /**
//     * 核心菜单画法
//     */
//    public static BufferedImage drawMenu(Map<String, List<String>> menuMap, MenuImageConfig config, BufferedImage background){
//        //todo 待完成
//    }

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
}
