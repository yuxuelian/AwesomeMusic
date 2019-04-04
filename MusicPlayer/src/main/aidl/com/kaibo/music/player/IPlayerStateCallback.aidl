package com.kaibo.music.player;

import android.graphics.Bitmap;

import com.kaibo.music.player.bean.SongBean;
import com.kaibo.music.player.bean.LyricRowBean;
import com.kaibo.music.player.bean.PlayerStateBean;

interface IPlayerStateCallback {
    // 播放歌曲发生改变
    void songChange(in SongBean songBean);
    // 歌词加载完成
    void lyricLoadDone(in List<LyricRowBean> lyricList);
    // 播放状态发生改变
    void isPlayingChange(boolean isPlaying);
    // 歌曲图片加载完成
    void songImageLoadDone(in Bitmap songImage);
    // 开始准备
    void startPrepare();
    // 准备完成
    void prepareDone();
}
