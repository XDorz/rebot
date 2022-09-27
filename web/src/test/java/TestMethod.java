
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.luhuiguo.chinese.ChineseUtils;
import com.luhuiguo.chinese.pinyin.PinyinFormat;
import com.luhuiguo.chinese.pinyin.ToneType;
import com.xxj.qqbot.MiraiBotApplication;
import com.xxj.qqbot.util.botconfig.config.Yunshi;
import com.xxj.qqbot.util.botconfig.init.ConfigInit;
import com.xxj.qqbot.util.botconfig.init.EnvironmentInit;
import com.xxj.qqbot.util.common.BeastTransUtil;
import com.xxj.qqbot.util.common.ImageUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = MiraiBotApplication.class)
public class TestMethod {

    public Map<String,List<String>> map;

    private static Pattern pattern=Pattern.compile("\\[.+$");

    @Test
    public void sss(){
        String str="";
        System.out.println((str=BeastTransUtil.encode("中国共产党")));
//        System.out.println(BeastTransUtil.decode(str));
    }

    /**
     * 加上标题
     */
    @Test
    public void drawTitle() throws Exception{
        File title=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\logo\\logo.png");
        File file=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\valcard");
        String pre="C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\final\\";
        BufferedImage add = ImageIO.read(title);
        for (File inFile : file.listFiles()) {
            BufferedImage org = ImageIO.read(inFile);
            BufferedImage image = ImageUtil.draw(org,add,(org.getWidth()-add.getWidth())/2,50);
            ImgUtil.write(image,new File(pre+inFile.getName()));
        }
    }

    /**
     * 加上内容
     */
    @Test
    public void drawVal() throws Exception{
        File val=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\trueval\\火.png");
        File file=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\result");
        String pre="C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\valcard\\";
        BufferedImage add = ImageIO.read(val);
        for (File inFile : file.listFiles()) {
            BufferedImage org = ImageIO.read(inFile);
            BufferedImage image = ImageUtil.draw(org, add, 90, 90);
            ImgUtil.write(image,new File(pre+inFile.getName()));
        }
    }

    /**
     * 融合
     */
    @Test
    public void draw(){
        List<String> list=new ArrayList<>();
        List<File> fList=new ArrayList<>();
        File file=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\people");
        File nFile=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\background");
        String pre="C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\result\\";
        for (File inFile : nFile.listFiles()) {
            list.add(inFile.getName().replace(".png",""));
            fList.add(inFile);
        }
        for (File inFile : file.listFiles()) {
            for (String s : list) {
                if(inFile.getName().contains(s)){
                    BufferedImage image = ImageUtil.draw(fList.get(list.indexOf(s)), inFile, ImageUtil.RIGHT_CENTER);
                    if(inFile.getName().contains("2[")){
                        ImgUtil.write(image,new File(pre+s+"2.png"));
                    }else {
                        ImgUtil.write(image,new File(pre+s+".png"));
                    }
                    break;
                }
            }
        }
    }

    /**
     * 融合logo和val
     */
    @Test
    public void drawlogo() throws Exception{
        File board=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\val\\运势.png");
        File file=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\logo");
        String pre="C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\trueval\\";
        BufferedImage org = ImageIO.read(board);
        for (File inFile : file.listFiles()) {
            BufferedImage add = ImageIO.read(inFile);
            BufferedImage image = ImageUtil.draw(org,add,(org.getWidth()-add.getWidth())/2,520);
            ImgUtil.write(image,new File(pre+inFile.getName()));
        }
    }

    /**
     * 图片和背景比较，寻找无法匹配的
     */
    @Test
    public void compare(){
        List<String> list=new ArrayList<>();
        File file=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\image\\yuanshen\\");
        File nFile=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\image\\mp\\");
        for (File inFile : nFile.listFiles()) {
            list.add(inFile.getName().replace(".png",""));
        }
        for (File inFile : file.listFiles()) {
            boolean f=false;
            for (String s : list) {
                if(inFile.getName().contains(s)){
                    f=true;
                    break;
                }
            }
            if(!f){
                System.out.println(inFile.getName());
            }
        }
    }

    /**
     * 覆盖上透明膜
     */
    @Test
    public void lowAlpha(){
        File file=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\cutbackground");
        String pre="C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\genshin\\background\\";
        for (File inFile : file.listFiles()) {
            BufferedImage image = ImageUtil.changeCenterAlpha(inFile, 40, 40, 185,true);
            ImgUtil.write(image,new File(pre+inFile.getName()));
        }
    }

    /**
     * 覆盖上透明膜
     */
    @Test
    public void lowLogoAlpha(){
        File file=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\buluearchive\\heightbackground");
        String pre="C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\buluearchive\\background\\";
        for (File inFile : file.listFiles()) {
            BufferedImage image = ImageUtil.changeCenterAlpha(inFile, 50, 50, 160,true);
            ImgUtil.write(image,new File(pre+inFile.getName()));
        }
    }

    /**
     * 截取右下角指定大小图片
     */
    @Test
    public void cut() throws Exception{
        File file=new File("c:\\Users\\谢昕捷\\Desktop\\workspace\\source\\buluearchive\\background");
        String sc="c:\\Users\\谢昕捷\\Desktop\\workspace\\source\\buluearchive\\cutbackground\\";
        int width=1180;
        int height=1080;
        for (File inFile : file.listFiles()) {
            BufferedImage image = ImageIO.read(inFile);
            Rectangle rectangle = new Rectangle();
            rectangle.setBounds(image.getWidth()-width,image.getHeight()-height,width,height);
            ImgUtil.cut(image,new File(sc+inFile.getName()),rectangle);
        }
    }

    /**
     * 等比改变图像高度
     */
    @Test
    public void changeHeight() throws Exception{
        File file=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\buluearchive\\obackground");
        String sc="C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\buluearchive\\background\\";
        float h=1080f;
        for (File inFile : file.listFiles()) {
            BufferedImage image = ImageIO.read(inFile);
            int height = image.getHeight();
            float scale = h / height;
            height=(int)h;
            int width=(int)(image.getWidth()*scale);
            ImgUtil.scale(image,new File(sc+rename(inFile.getName(),width+1,height)),scale);
        }
    }


    private boolean judge(int i){
        if(i==16777215||i==0||(i>>>24)<15){
            return false;
        }
        return true;
    }

    /**
     * 文件下图片去边框
     */
    @Test
    public void removeKuang() throws Exception {
        File file=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\buluearchive\\opeople");
        String sc="C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\buluearchive\\people\\";
        for (File inFile : file.listFiles()) {
            int x=-1;
            int y=-1;
            int x1=-1;
            int y1=-1;
            BufferedImage image = ImageIO.read(inFile);
            loop1:
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    if(judge(image.getRGB(i,j))) {
                        x=i;
                        break loop1;
                    }
                }
            }
            loop2:
            for (int i = image.getWidth()-1; i >= 0; i--) {
                for (int j = 0; j < image.getHeight(); j++) {
                    if(judge(image.getRGB(i,j))) {
                        x1=i;
                        break loop2;
                    }
                }
            }
            loop3:
            for (int j = 0; j < image.getHeight(); j++) {
                for (int i = 0; i < image.getWidth(); i++) {
                    if(judge(image.getRGB(i,j))){
                        y=j;
                        break loop3;
                    }
                }
            }
            loop4:
            for (int j = image.getHeight()-1; j >= 0; j--) {
                for (int i = 0; i < image.getWidth(); i++) {
                    if(judge(image.getRGB(i,j))){
                        y1=j;
                        break loop4;
                    }
                }
            }
            int height=y1-y+1;
            int width=x1-x+1;
            Rectangle rectangle = new Rectangle();
            rectangle.setBounds(x,y,width,height);
            ImgUtil.cut(image,new File(sc+rename(inFile.getName(),width,height)),rectangle);
        }
    }


    @Test
    public void move() throws Exception{
        File file=new File("F:\\domain\\temp\\fixtool\\度云\\new\\素材包\\原神\\原神角色立绘\\原神名片");
        String sc="C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\image\\mingpian\\";
        float h=1080.0f;
        for (File inFile : file.listFiles()) {
            BufferedImage image = ImageIO.read(inFile);
            String name = inFile.getName().split(" ")[0];
            int height = image.getHeight();
            float scale = h / height;
            height=(int)h;
            int width=(int)(image.getWidth()*scale);
            ImgUtil.scale(image,new File(sc+name+".png"),scale);
        }
    }

    private String rename(String name,int width,int height){
        Matcher matcher = pattern.matcher(name);
        String newName="";
        if(matcher.find()){
            newName=matcher.replaceFirst("");
            newName=newName+"["+width+"X"+height+"].png";
            return newName;
        }
        name=name.substring(0,name.lastIndexOf("."));
        name=name+"["+width+"X"+height+"].png";
        return name;
    }

    @Test
    public void toPingYin(){
        File f=new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\yunshi\\genshin\\reflect.txt");
        List<String> list = FileUtil.readLines(f, StandardCharsets.UTF_8);

        StringBuilder sb=new StringBuilder();
        for (String s : list) {
            String[] split = s.split("=");
            PinyinFormat format=new PinyinFormat();
            format.setToneType(ToneType.WITHOUT_TONE);
            format.setSeparator("");
            String pinyin=ChineseUtils.toPinyin(split[1],format);
            s=s.replace(split[1],pinyin);
            sb.append(s+"\n");
        }
        FileUtil.writeString(sb.toString(),new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\yunshi\\genshin\\reflect.txt"), StandardCharsets.UTF_8);
//        for (File file : f.listFiles()) {
//            String name = file.getName().replace(".png", "");
//            boolean has2=false;
//            if(name.contains("2")){
//                name=name.replace("2","");
//                has2=true;
//            }
//            PinyinFormat format=new PinyinFormat();
//            format.setToneType(ToneType.WITHOUT_TONE);
//            format.setSeparator("");
//            String pinyin=ChineseUtils.toPinyin(name,format);
//            sb.append(pinyin+"="+name+"\n");
//            if(has2){
//                pinyin+="2.png";
//            }else {
//                pinyin+=".png";
//            }
//            FileUtil.rename(file,pinyin,true);
//        }
//        FileUtil.writeString(sb.toString(),new File("C:\\Users\\谢昕捷\\Desktop\\workspace\\source\\yunshi\\genshin\\nameReflect.txt"), StandardCharsets.UTF_8);
    }
}
