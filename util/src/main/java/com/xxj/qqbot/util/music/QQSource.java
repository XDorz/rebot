package com.xxj.qqbot.util.music;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxj.qqbot.util.common.MoriBotException;

import java.util.ArrayList;
import java.util.List;


/**
 * 废案，无法获取歌曲真实URL
 * 只可听无版权歌
 */
@Deprecated
public class QQSource implements MusicSource {

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
                if(info.getString("mid")!=null){
                    break;
                }
                location++;
                info=infos.getJSONObject(location);
            }
            String mid = info.getString("mid");
            murl=queryRealUrl(info.getString("mid"));
        }catch (Exception e){
            throw new MoriBotException("该曲目暂时没有音源",e);
        }
        JSONArray singers = info.getJSONArray("singer");
        StringBuilder sb=new StringBuilder();
        for (int j = 0; j < singers.size(); j++) {
            JSONObject singer = singers.getJSONObject(j);
            String name = singer.getString("name");
            if(j>0){
                sb.append(" · ");
            }
            sb.append(name);
        }
        MusicInfo mi = new MusicInfo(info.getString("title"),
                "singer:" + sb.toString(),
                "http://y.gtimg.cn/music/photo_new/T002R300x300M000" + info.getJSONObject("album").getString("mid") + ".jpg",
                murl,
                "https://i.y.qq.com/v8/playsong.html?_wv=1&songid=" + info.getString("id") + "&source=qqshare&ADTAG=qqshare",
                MusicSourceInfo.QQ.getAppName(),
                MusicSourceInfo.QQ.icon,
                MusicSourceInfo.QQ.getAppId());
        return mi;
    }

    @Override
    public List<String> getMusicList(String songName) {
        JSONArray infos = getInfos(songName);
        List<String> list=new ArrayList<>();
        for (int i = 0; i < infos.size(); i++) {
            JSONObject jsonObject = infos.getJSONObject(i);
            JSONArray singers = jsonObject.getJSONArray("singer");
            StringBuilder sb=new StringBuilder();
            for (int j = 0; j < singers.size(); j++) {
                JSONObject singer = singers.getJSONObject(j);
                String name = singer.getString("name");
                if(j>0){
                    sb.append(" · ");
                }
                sb.append(name);
            }
            list.add(jsonObject.getString("title")+"---singer:"+sb.toString()+"---"+jsonObject.getJSONObject("album").getString("title"));
        }
        return list;
    }

    public JSONArray getInfos(String songName){
        HttpRequest request = HttpUtil.createPost(MusicUrls.qq);
        request.header("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
        JSONObject searchParam=new JSONObject();
        JSONObject search=new JSONObject();
        JSONObject body=new JSONObject();
        searchParam.put("query", songName);
        searchParam.put("num_per_page",MusicUrls.listMaxNum);
        searchParam.put("search_type",0);
        searchParam.put("page_num",1);

        search.put("param",searchParam);
        search.put("module","music.search.SearchCgiService");
        search.put("method", "DoSearchForQQMusicDesktop");

        body.put("music.search.SearchCgiService", search);

        request.body(body.toJSONString());

        request.header("referer","https://y.qq.com");
        HttpResponse response = request.execute();
        String result = response.body();

        JSONObject resultJson = JSONObject.parseObject(result);
        return resultJson.getJSONObject("music.search.SearchCgiService").getJSONObject("data")
                .getJSONObject("body").getJSONObject("song").getJSONArray("list");
    }

    public String queryRealUrl(String songmid) {
        try {
            StringBuilder urlsb = new StringBuilder(
                    "https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&data=%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22");
            urlsb.append(songmid);
            urlsb.append(
                    "%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%221139869558%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%2218585073516%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D");
            HttpRequest request = HttpUtil.createGet(urlsb.toString());
            request.header("Host", "u.y.qq.com");
            request.header("User-Agent",
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1");
            HttpResponse response = request.execute();
            JSONObject jsonObject = JSONObject.parseObject(response.body());

            if (jsonObject.getIntValue("code") != 0) {
                throw new MoriBotException("无法从QQ音乐服务器拉取正确的URL");
            }
            StringBuilder sb = new StringBuilder(jsonObject.getJSONObject("req_0").getJSONObject("data")
                    .getJSONArray("sip").getString(0));

            sb.append(jsonObject.getJSONObject("req_0").getJSONObject("data").getJSONArray("midurlinfo")
                    .getJSONObject(0).getString("purl"));
            return sb.toString();
        } catch (Throwable e) {
            throw  new MoriBotException("获取QQ音乐真实URL出错!",e);
        }
    }

//    String[] availenc = new String[] { "D00A", // .flac
//            "A000", // .ape
//            "F000", // .flac
//            "M800", // .mp3
//            //"O600", // .ogg
//            "C400", // .m4a
//            "M500",// .mp3
//    };
//    String[] availext = new String[] { ".flac", ".ape", ".flac", ".mp3",/* ".ogg",*/ ".m4a", ".mp3", };
//    public String queryRealUrl2(String songmid) {
//        JSONObject main=new JSONObject();
//        JSONObject r0=new JSONObject();
//        JSONObject comm=new JSONObject();
//        JSONObject param=new JSONObject();
//        JSONArray fns=new JSONArray();
//        JSONArray ids=new JSONArray();
//        JSONArray types=new JSONArray();
//        String guid=String.valueOf((int)(Math.random()*9000000+1000000));
//        main.put("req_0",r0);
//        r0.put("module","vkey.GetVkeyServer");
//        r0.put("method", "CgiGetVkey");
//        r0.put("param", param);
//        param.put("uin",1139869558); //
//        param.put("loginflag",1);
//        param.put("platform", "20");
//        param.put("guid","544A7A29C2517CFDDD944AE527474EC4");
//        types.add(0);
//        param.put("songtype",types);
//        param.put("songmid",ids);
//        ids.add(songmid);
//        param.put("filename",fns);
//        main.put("comm",comm);
//        comm.put("uin",1139869558); //
//        comm.put("format","json");
//        comm.put("ct",19);
//        comm.put("cv", 0);
//        comm.put("authst","Q_H_L_5qhxOXI7A8VkQJag8LDz9dY0j8F695j9REvSpGX0UiL3TOzq_vaRlrg"); //
////        comm.put("psrf_qqaccess_token","A2AF488378167A6C56AEC5FB644CE149");
////        comm.put("psrf_qqopenid","CE3D2CC98DAE091C43D780219552965E");
////        comm.put("psrf_qqunionid","BA9843FB08BFF308CD0F9CA0692EB3D2");
//        StringBuilder urlsb = new StringBuilder("https://u.y.qq.com/cgi-bin/musicu.fcg?-=getplaysongvkey&g_tk=5381");
//        urlsb.append("&loginUin="+1139869558); //
//        urlsb.append("&format=json&platform=yqq.json&data=");
//        String out = null;
//        String iqual = "2";
//        int i = 0;
//        if (iqual != null)
//            i = Integer.parseInt(iqual);
//        try {
//            if (i < availenc.length)
//                for (; i < availenc.length; i++) {
//                    if(fns.size()>0)
//                        fns.remove(0);
//                    fns.add(availenc[i] + songmid +songmid + availext[i]);
//                    HttpRequest request = HttpUtil.createGet(urlsb.toString() + URLEncoder.encode(main.toString(), "UTF-8"));
//                    //System.out.println("incoming " + u.toString());
//                    request.header("Host", "u.y.qq.com");
//                    request.header("Referer", "http://y.qq.com");
//                    request.header("User-Agent",
//                            "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1");
//                    request.header("Cookie","psrf_qqaccess_token=A2AF488378167A6C56AEC5FB644CE149; psrf_qqopenid=CE3D2CC98DAE091C43D780219552965E; qqmusic_gkey=1933EEAB29F8E73D76BCC21649C6E9D84E1A3DF452E7158B; qqmusic_gtime=205319432; qqmusic_guid=544A7A29C2517CFDDD944AE527474EC4; qqmusic_key=Q_H_L_5qhxOXI7A8VkQJag8LDz9dY0j8F695j9REvSpGX0UiL3TOzq_vaRlrg; qqmusic_uin=1139869558; qqmusic_version=18; tmeLoginType=2; wxopenid=; wxrefresh_token=; qm_hideuin=1139869558; qm_method=1; qqmusic_magictag=;");
//                    HttpResponse response = request.execute();
//                    //conn.setRequestProperty("cookie",cookie);
//                    String body = response.body();
//                    out=JSONObject.parseObject(body).getJSONObject("req_0").getJSONObject("data").getJSONArray("midurlinfo").getJSONObject(0).getString("purl");
//                    //System.out.println(new String(bs, "UTF-8"));
//                    if (out.length()==0) {
//                        continue;
//                    }
//                    break;
//                }
//            StringBuilder sb = new StringBuilder("http://ws.stream.qqmusic.qq.com/")
//                    .append(out);
//            return sb.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
