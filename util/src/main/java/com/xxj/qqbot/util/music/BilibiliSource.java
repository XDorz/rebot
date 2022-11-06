package com.xxj.qqbot.util.music;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxj.qqbot.util.common.MoriBotException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BilibiliSource implements MusicSource {

    @Override
    public MusicInfo getMusicInfo(String songName) {
        return getMusicInfo(songName,0,true);
    }

    @Override
    public MusicInfo getMusicInfo(String songName, int location,boolean postpone) {
        JSONArray infos = getInfos(songName);
        if(location>=infos.size()){
            throw new MoriBotException("您选择的音乐顺位超过最大值 :(");
        }
        JSONObject info = infos.getJSONObject(location);
        String murl=null;
        try{
            while (location<info.size() && postpone) {
                if(info.getJSONArray("play_url_list").size()>0){
                    break;
                }
                location++;
                info=infos.getJSONObject(location);
            }
            murl=info.getJSONArray("play_url_list").getJSONObject(0).getString("url");
        }catch (Exception e){
            throw new MoriBotException("该曲目暂时没有音源",e);
        }
        MusicInfo mi = new MusicInfo(info.getString("title"),
                "UP:" + info.getString("up_name"),
                info.getString("cover"),
                murl,
                "https://www.bilibili.com/audio/au" + info.getString("id"),
                "哔哩哔哩",
                "https://open.gtimg.cn/open/app_icon/00/95/17/76/100951776_100_m.png?t=1624441854",
                100951776);
        mi.properties = new HashMap<>();
        mi.properties.put("user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36");
        mi.properties.put("referer", "https://www.bilibili.com/");
        return mi;
    }

    @Override
    public List<String> getMusicList(String songName) {
        JSONArray infos = getInfos(songName);
        List<String> list=new ArrayList<>();
        for (int i = 0; i < Math.min(infos.size(),MusicUrls.listMaxNum); i++) {
            JSONObject info = infos.getJSONObject(i);
            list.add(info.getString("title")+"---up:"+info.getString("up_name")+"---创作:"+info.getString("author"));
        }
        return list;
    }

    private JSONArray getInfos(String songName){
        HttpResponse response = HttpUtil.createGet(MusicUrls.bili+songName).execute();
        if(response.isOk()){
            String resBody = response.body();
            JSONObject jsonObject = JSONObject.parseObject(resBody);
            JSONArray results = jsonObject.getJSONObject("data").getJSONArray("result");
            return results;
        }
        throw new MoriBotException("无法获取B站音乐信息！请检查API是否可用");
    }
}
