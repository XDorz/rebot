package com.xxj.qqbot.util.botconfig.init;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.config.BotInfo;
import com.xxj.qqbot.util.botconfig.config.SourcePath;
import com.xxj.qqbot.util.botconfig.config.Yunshi;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvPrefix;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.ExtractBotConfig;
import io.github.mzdluo123.silk4j.AudioUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Slf4j
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

        File file=new File("E:\\mirai_temp");
        try {
            if(!file.exists()){
                file.mkdirs();
            }
            BotInfo.tempFile=file;
            AudioUtils.init(file);
        } catch (IOException e) {
            log.error("无法创建临时文件缓存目录或初始化AudioFile");
        }
    }
}
