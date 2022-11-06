package com.xxj.qqbot.util.botconfig.init;

import com.xxj.qqbot.util.botconfig.config.BotInfo;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.BotRegister;
import com.xxj.qqbot.util.botconfig.functioncompent.frameneededconfigload.EnvPrefix;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.utils.BotConfiguration;

import java.io.File;

/**
 * 注册机器人，该类不为框架所属
 */
@Slf4j
@EnvPrefix(prefix = "mirai.config")
public class BotInit extends BotRegister {

    /**
     * 运行路径
     */
    private String workdir;

    /**
     * 缓存路径
     */
    private String cachedir;

    /**
     * 断线自动重连
     */
    private boolean autoconn;

    /**
     * 开启缓存，生产环境建议false
     */
    private boolean enablecache;

    /**
     * 保存设备信息
     */
    private boolean deviceinfo;

    /**
     * 设备信息文件名,默认放在workdir路径下
     */
    private String devicename;

    @Override
    public Bot registerBot() {
        Long account=BotInfo.account;
        String password=BotInfo.password;
        BotConfiguration config=new BotConfiguration();
        config.setWorkingDir(new File(workdir));
        config.setCacheDir(new File(cachedir));
        config.setAutoReconnectOnForceOffline(autoconn);
        String heart=BotInfo.heartBeatingType==null?"0":BotInfo.heartBeatingType;
        if(heart.equals("0")){
            config.setHeartbeatStrategy(BotConfiguration.HeartbeatStrategy.STAT_HB);
            log.info("当前心跳策略：持续");
        }else if(heart.equals("1")){
            config.setHeartbeatStrategy(BotConfiguration.HeartbeatStrategy.REGISTER);
            log.info("当前心跳策略：注册");
        }else if(heart.equals("2")){
            config.setHeartbeatStrategy(BotConfiguration.HeartbeatStrategy.NONE);
            log.info("当前心跳策略：不使用");
        }
        if(enablecache){
            config.enableContactCache();
        }
        if(deviceinfo){
            config.fileBasedDeviceInfo(devicename);
        }
        Bot bot= BotFactory.INSTANCE.newBot(account,password,config);
        bot.login();
        return bot;
    }

    @Override
    public Long getRootId() {
        return BotInfo.rootId;
    }
}
