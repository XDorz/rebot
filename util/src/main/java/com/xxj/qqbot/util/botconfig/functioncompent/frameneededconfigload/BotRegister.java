package com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload;

import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.utils.BotConfiguration;

import java.io.File;
public abstract class BotRegister {

    public abstract Bot registerBot();

    public abstract Long getRootId();

    public static Bot registerDefaultBot(Long account,String password){
        String workdir=System.getProperty("user.dir")+"/workspace";
        File workFile=new File(workdir);
        if(!workFile.exists()) workFile.mkdirs();
        File cacheFile=new File(workdir+"/cache");
        if(!cacheFile.exists()) cacheFile.mkdirs();
        BotConfiguration config=new BotConfiguration();
        config.setWorkingDir(workFile);
        config.setCacheDir(cacheFile);
        config.setAutoReconnectOnForceOffline(true);
        config.setHeartbeatStrategy(BotConfiguration.HeartbeatStrategy.REGISTER);
        config.enableContactCache();
        config.fileBasedDeviceInfo("device.json");
        Bot bot = BotFactory.INSTANCE.newBot(account, password, config);
        bot.login();
        BotFrameworkConfig.bot=bot;
        return bot;
    }
}
