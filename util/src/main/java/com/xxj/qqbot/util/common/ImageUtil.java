package com.xxj.qqbot.util.common;

import com.xxj.qqbot.util.botconfig.functioncompent.HelpImageConfig;
import lombok.Data;
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
import java.util.ArrayList;
import java.util.List;
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
    private static final Pattern helpMenuPattern=Pattern.compile("(\\((.+?)\\))?\\{(.+?)\\}");
    private static final Pattern numPattern=Pattern.compile("\\d+");
    private static final String colorNumPattern="1[0-9][0-9]|2[0-4][0-9]|25[0-5]|[0-9][0-9]|[0-9]";
    private static final Pattern colorPattern=Pattern.compile("("+colorNumPattern+")[,，.。]("+colorNumPattern+")[,，.。]("+colorNumPattern+")([,，.。]([0-9][0-9]|100|[0-9]))?");
    private static final Color DEFAULTIMPORTANTCOLOR=Color.RED;
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
        int standardFontWidth=board.getFontMetrics(config.getHelpFont()).charWidth('你');
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
                false,
                false,
                false);
        x+=distribution.getWidthSpace();
        y+=distribution.getHeightSpace()-amend;
        y+=board.getFontMetrics().getAscent();
        board.setColor(config.getTitleFontDefaultColor());
        board.drawString(config.getTitleValue(),Math.max(0,x-config.getTitleProgress()),y);
        y+=titleFontH+config.getRowSpace();
        width=width-distribution.getWidthSpace()-standardFontWidth+2;
        height=height-distribution.getHeightSpace()-titleFontH-helpFontH-config.getRowSpace()+2;
        board.setFont(config.getHelpFont());
        String str = colorFontInfo.getContext();
        ColorfulFont handler=new ColorfulFont(colorFontInfo,config.getContextFontDefaultColor());
        FontMetrics helpMetrics = board.getFontMetrics();
        int n=0;
        loop:
        for (int j = 0; j < height; ) {
            for (int i = 0; i < width; ) {
                if(n>=str.length()) break loop;
                char c = str.charAt(n);
                if(c=='\n'){
                    n++;
                    break ;
                }
                Color fontColor = handler.getFontColor(n);
                if(fontColor!=null) board.setColor(fontColor);
                board.drawString(String.valueOf(c),x+i,y+j);
                i+=helpMetrics.charWidth(c)+config.getColSpace();
                n++;
            }
            j+=helpFontH+config.getRowSpace();
        }
        board.dispose();
        return background;
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
        ColorfulFont(ColorFontInfo info,Color contextColor){
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
            }
            return re;
        }
    }

//    /**
//     * 核心菜单画法
//     */
//    public static BufferedImage drawMenu(Map<String, List<String>> menuMap, MenuImageConfig config, BufferedImage background){
//        //todo 待完成
//    }



    /**
     * 解析文本获得信息
     */
    @Data
    static class ColorFontInfo{
        private String context;
        private List<Integer> start;
        private List<Integer> end;
        private List<Color> color;
        ColorFontInfo(){
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
        Matcher matcher = helpMenuPattern.matcher(context);
        while (matcher.find()){
            int front=(matcher.group(0).length()-matcher.group(3).length())-1;
            start.add(matcher.start(3)-front);
            end.add(matcher.end(3)-front);
            if(matcher.group(2)!=null){
                String colr = matcher.group(2);
                Matcher colorMatcher = colorPattern.matcher(colr);
                if(numPattern.matcher(colr).matches()){
                    color.add(new Color(Integer.parseInt(colr)));
                }else if(colorMatcher.matches()){
                    if(colorMatcher.group(5)!=null){
                        color.add(new Color(Integer.parseInt(colorMatcher.group(1))
                                ,Integer.parseInt(colorMatcher.group(2)),Integer.parseInt(colorMatcher.group(3))
                                ,Integer.parseInt(colorMatcher.group(5))));
                    }else {
                        color.add(new Color(Integer.parseInt(colorMatcher.group(1))
                                ,Integer.parseInt(colorMatcher.group(2)),Integer.parseInt(colorMatcher.group(3))));
                    }

                }else{
                    switch (colr.toLowerCase()){
                        case "red":
                            color.add(Color.RED);
                            break;
                        case "green":
                            color.add(Color.GREEN);
                            break;
                        case "yellow":
                            color.add(Color.YELLOW);
                            break;
                        case "blue":
                            color.add(Color.BLUE);
                            break;
                        default:
                            log.error("不支持的颜色定义，将使用默认颜色！");
                            color.add(DEFAULTIMPORTANTCOLOR);
                            break;
                    }
                }
            }else {
                color.add(DEFAULTIMPORTANTCOLOR);
            }
            context = matcher.replaceFirst(matcher.group(3));
            matcher=helpMenuPattern.matcher(context);
        }
        colorFontInfo.setContext(context);
        return colorFontInfo;
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
}
