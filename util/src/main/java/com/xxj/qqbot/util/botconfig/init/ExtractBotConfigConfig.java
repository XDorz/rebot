package com.xxj.qqbot.util.botconfig.init;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxj.qqbot.util.botconfig.config.SourcePath;
import com.xxj.qqbot.util.botconfig.config.Yunshi;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.ExtractBotConfig;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ExtractBotConfigConfig implements ExtractBotConfig {
    @Override
    public void initex() {
        String fortuneString = FileUtil.readString(Yunshi.fortune, StandardCharsets.UTF_8);
        JSONArray jsonArray = JSONObject.parseArray(fortuneString);
        Yunshi.fortunes=new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            Yunshi.Fortune fortune = jsonArray.getJSONObject(i).toJavaObject(Yunshi.Fortune.class);
            Yunshi.fortunes.add(fortune);
        }
    }
}
