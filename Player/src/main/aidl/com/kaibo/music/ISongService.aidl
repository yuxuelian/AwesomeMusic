// ISongService.aidl
package com.kaibo.music;

// Declare any non-default types here with import statements
import com.kaibo.music.bean.SongBean;

interface ISongService {
    void nextPlay(in SongBean songBean);
    void playSong(in SongBean songBean);
    void playPlaylist(in List<SongBean> playlist,int id,String pid);
    void play(int id);
    void playPause();
    void pause();
    void stop();
    void prev();
    void next();
    void setPlayMode(int mode);
    void seekTo(int ms);
    int position();
    int getDuration();
    int getCurrentPosition();
    boolean isPlaying();
    boolean isPause();
    String getSongName();
    String getSongArtist();
    SongBean getPlayingSongBean();
    List<SongBean> getPlayList();
    void removeFromQueue(int position);
    void clearQueue();
    void showDesktopLyric(boolean show);
    int getAudioSessionId();
}
