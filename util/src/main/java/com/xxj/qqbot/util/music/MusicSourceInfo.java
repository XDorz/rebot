package com.xxj.qqbot.util.music;

public enum MusicSourceInfo {
    BILIBILI("哔哩哔哩","https://open.gtimg.cn/open/app_icon/00/95/17/76/100951776_100_m.png?t=1624441854",100951776),
    QQ("QQ音乐","https://url.cn/PwqZ4Jpi",100497308),
    KUGOU("酷狗","",205141),
    NETEASY("网易云音乐","",100495085),
    ;
    String appName;
    String icon;
    long appId;

    MusicSourceInfo(String appName,String icon, long appId){
        this.appName=appName;
        this.icon=icon;
        this.appId=appId;
    }

    public String getAppName() {
        return appName;
    }

    public String getIcon() {
        return icon;
    }

    public long getAppId() {
        return appId;
    }
}
