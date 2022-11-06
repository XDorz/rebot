package com.xxj.qqbot.util.music;

import java.util.List;

public interface MusicSource {

    MusicInfo getMusicInfo(String songName);

    MusicInfo getMusicInfo(String songName,int location,boolean postpone);

    List<String> getMusicList(String songName);
}
