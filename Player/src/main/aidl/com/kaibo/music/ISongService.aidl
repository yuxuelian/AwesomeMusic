// ISongService.aidl
package com.kaibo.music;

// Declare any non-default types here with import statements
import com.kaibo.music.bean.SongBean;

interface ISongService {
    void togglePlayer();
    void pause();
    void stop();
    void prev();
    void next();
    void seekTo(int pos);
    int getCurrentPosition();
    void setPlayPosition(int id);
    void setNextSong(in SongBean songBean);
    void setPlaySong(in SongBean songBean);
    int getPlayPosition();
    int getDuration();
    boolean isPlaying();
    boolean isPrepared();
    SongBean getPlaySong();
    List<SongBean> getPlaySongQueue();
    void setPlaySongList(in List<SongBean> playlist,int position,String playListId);
    void removeFromQueue(int position);
    void showDesktopLyric(boolean show);
    void clearQueue();
    String updatePlayMode();
}
