package com.xxj.qqbot.util.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxj.qqbot.util.botconfig.config.Apis;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CommonEventToolUtil {

    /**
     * 对LoliconApi发起请求
     */
    public static List<MessageChain> getSexPic(BotMessageInfo info, boolean isR18,int max){
        HttpRequest post = HttpUtil.createPost(Apis.sexPicApi);
        post.header("Content-Type","application/json");
        ParamMap<String, String> params = info.getParams();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("r18",isR18?"1":"0");
        jsonObject.put("proxy",Apis.pixivProxy);
        jsonObject.put("size","regular");

        int picNum=1;
        String num=params.getOrDefault(null,"数量","num","param1");
        if(num!=null){
            picNum = Integer.parseInt(num);
            picNum=Math.min(picNum, max);
            picNum=Math.max(1,picNum);
            jsonObject.put("num",picNum);
        }

        String keyword=params.getOrDefault(null,"key","keyword","关键词","param2");
        if(keyword!=null) jsonObject.put("keyword",keyword);

        String tag=params.getOrDefault(null,"tag","标签","param3");
        if(tag!=null) jsonObject.put("tag",tag);


        List<MessageChain> chains=new ArrayList<>();
        post.body(jsonObject.toJSONString());
        String body = post.execute().body();
        JSONObject resultJsonObject = JSONObject.parseObject(body);
        JSONArray dataArray = resultJsonObject.getJSONArray("data");
        if(dataArray.size()==0) return null;
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject result = dataArray.getJSONObject(i);
            String url = result.getJSONObject("urls").getString("regular");
            InputStream inputStream = HttpUtil.createGet(url).execute().bodyStream();
            Image image = ImageUploadUtil.uploadAntiKill(inputStream);

            MessageChainBuilder inBuilder=new MessageChainBuilder();
            inBuilder.append("title:"+result.getString("title")+"\n");
            inBuilder.append("pid:"+result.getString("pid")+"\n");
            inBuilder.append("uid:"+result.getString("uid")+"\n");
            inBuilder.append("author:"+result.getString("author")+"\n");
            inBuilder.append("是否r18:"+result.getString("r18")+"\n");
            inBuilder.append("width:"+result.getString("width")+"\n");
            inBuilder.append("height:"+result.getString("height")+"\n");
            inBuilder.append("格式:"+result.getString("ext")+"\n");
            inBuilder.append("tags:"+result.getString("tags")+"\n");
            inBuilder.append(image);
            chains.add(inBuilder.build());
        }
        return chains;
    }

    /**
     * 对LoliconApi发起请求
     */
    public static List<MessageChain> getSexPicWithoutInfo(BotMessageInfo info, boolean isR18,int max){
        HttpRequest post = HttpUtil.createPost(Apis.sexPicApi);
        post.header("Content-Type","application/json");
        ParamMap<String, String> params = info.getParams();
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("r18",isR18?"1":"0");
        jsonObject.put("proxy",Apis.pixivProxy);
        jsonObject.put("size","regular");

        int picNum=1;
        String num=params.getOrDefault(null,"数量","num","param1");
        if(num!=null){
            picNum = Integer.parseInt(num);
            picNum=Math.min(picNum, max);
            picNum=Math.max(1,picNum);
            jsonObject.put("num",picNum);
        }

        String keyword=params.getOrDefault(null,"key","keyword","关键词","param2");
        if(keyword!=null) jsonObject.put("keyword",keyword);

        String tag=params.getOrDefault(null,"tag","标签","param3");
        if(tag!=null) jsonObject.put("tag",tag);


        List<MessageChain> chains=new ArrayList<>();
        post.body(jsonObject.toJSONString());
        String body = post.execute().body();
        JSONObject resultJsonObject = JSONObject.parseObject(body);
        JSONArray dataArray = resultJsonObject.getJSONArray("data");
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject result = dataArray.getJSONObject(i);
            String url = result.getJSONObject("urls").getString("regular");
            InputStream inputStream = HttpUtil.createGet(url).execute().bodyStream();
            Image image = ImageUploadUtil.uploadAntiKill(inputStream);

            MessageChainBuilder inBuilder=new MessageChainBuilder();
//            inBuilder.append("title:"+result.getString("title")+"\n");
            inBuilder.append("pid:"+result.getString("pid")+"\n");
            inBuilder.append("uid:"+result.getString("uid")+"\n");
            inBuilder.append("author:"+result.getString("author")+"\n");
            inBuilder.append("是否r18:"+result.getString("r18")+"\n");
            inBuilder.append("width:"+result.getString("width")+"\n");
            inBuilder.append("height:"+result.getString("height")+"\n");
            inBuilder.append("格式:"+result.getString("ext")+"\n");
//            inBuilder.append("tags:"+result.getString("tags")+"\n");
            inBuilder.append(image);
            chains.add(inBuilder.build());
        }
        return chains;
    }

}
