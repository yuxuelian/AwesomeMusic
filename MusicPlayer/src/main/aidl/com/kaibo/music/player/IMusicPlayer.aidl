package com.kaibo.music.player;

import com.kaibo.music.player.bean.SongBean;
import com.kaibo.music.player.bean.LyricRowBean;
import com.kaibo.music.player.IPlayerStateCallback;

interface IMusicPlayer {
    // 切換播放暂停
    void togglePlayer();
    // 暂停
    void pause();
    // 播放
    void play();
    // 停止
    void stop();
    // 上一曲
    void prev();
    // 下一曲
    void next();
    // 获取播放总时长
    int getDuration();
    // 设置播放进度
    void seekTo(int pos);
    // 获取当前播放进度
    int getCurrentPosition();
    // 设置播放队列位置
    void setPlayPosition(int pos);
    // 获取播放队列位置
    int getPlayPosition();
    // 设置单曲播放
    void setPlaySong(in SongBean songBean);
    // 获取播放单曲
    SongBean getPlaySong();
    // 是否正在播放中
    boolean isPlaying();
    // 是否准备完成
    boolean isPrepared();
    // 获取播放队列
    List<SongBean> getPlayQueue();
    // 设置播放队列
    void setPlayQueue(in List<SongBean> playQueue);
    // 从播放队列中移出指定位置的单曲
    void removeAt(int position);
    // 移出指定单曲
    void remove(in SongBean songBean);
    // 切换显示桌面歌词
    void showDesktopLyric();
    // 清空播放队列
    void clearPlayQueue();
    // 更新播放模式
    int updatePlayMode();
    // 获取播放歌曲的歌词
    List<LyricRowBean> getLyricRowBeans();
    // 退出
    void exit();
    // 注册远程回调
    void registerCallback(IPlayerStateCallback callback);
    // 注销远程回调
    void unregisterCallback(IPlayerStateCallback callback);
}
