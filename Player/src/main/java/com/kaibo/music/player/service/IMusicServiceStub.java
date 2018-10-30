package com.kaibo.music.player.service;

import android.os.RemoteException;

import com.kaibo.music.ISongService;
import com.kaibo.music.bean.SongBean;
import com.kaibo.music.player.manager.PlayModeManager;

import java.lang.ref.WeakReference;
import java.util.List;

public class IMusicServiceStub extends ISongService.Stub {

    private final WeakReference<MusicPlayerService> mService;

    public IMusicServiceStub(final MusicPlayerService service) {
        mService = new WeakReference<>(service);
    }

    @Override
    public void nextPlay(SongBean music) throws RemoteException {
        mService.get().nextPlay(music);
    }

    @Override
    public void playSong(SongBean music) throws RemoteException {
        mService.get().play(music);
    }

    @Override
    public void playPlaylist(List<SongBean> songs, int id, String playListId) throws RemoteException {
        mService.get().play(songs, id, playListId);
    }

    @Override
    public void play(int id) throws RemoteException {
        mService.get().playMusic(id);
    }

    @Override
    public void playPause() throws RemoteException {
        mService.get().playPause();
    }

    @Override
    public void pause() throws RemoteException {
        mService.get().pause();
    }

    @Override
    public void stop() throws RemoteException {
        mService.get().stop(true);
    }

    @Override
    public void prev() throws RemoteException {
        mService.get().prev();
    }

    @Override
    public void next() throws RemoteException {
        mService.get().next(false);
    }

    @Override
    public void seekTo(int ms) throws RemoteException {
        mService.get().seekTo(ms);
    }

    @Override
    public String getSongName() throws RemoteException {
        return mService.get().getSongName();
    }

    @Override
    public String getSongArtist() throws RemoteException {
        return mService.get().getSingerName();
    }

    @Override
    public SongBean getPlayingSongBean() throws RemoteException {
        return mService.get().getPlayingMusic();
    }

    @Override
    public List<SongBean> getPlayList() throws RemoteException {
        return mService.get().getPlayQueue();
    }

    @Override
    public void removeFromQueue(int position) throws RemoteException {
        mService.get().removeFromQueue(position);
    }

    @Override
    public void clearQueue() throws RemoteException {
        mService.get().clearQueue();
    }

    @Override
    public void showDesktopLyric(boolean show) throws RemoteException {
        mService.get().showDesktopLyric(show);
    }

    @Override
    public int getAudioSessionId() throws RemoteException {
        return mService.get().getAudioSessionId();
    }

    @Override
    public int position() throws RemoteException {
        return mService.get().getPlayPosition();
    }

    @Override
    public int getDuration() throws RemoteException {
        return mService.get().getDuration();
    }

    @Override
    public int getCurrentPosition() throws RemoteException {
        return mService.get().getCurrentPosition();
    }

    @Override
    public boolean isPlaying() throws RemoteException {
        return mService.get().isPlaying();
    }

    @Override
    public boolean isPause() throws RemoteException {
        return !mService.get().isPlaying();
    }
}