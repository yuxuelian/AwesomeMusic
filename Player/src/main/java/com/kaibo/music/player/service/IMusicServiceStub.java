package com.kaibo.music.player.service;

import android.os.RemoteException;

import com.kaibo.music.ISongService;
import com.kaibo.music.bean.SongBean;

import java.lang.ref.WeakReference;
import java.util.List;

public class IMusicServiceStub extends ISongService.Stub {

    private final WeakReference<MusicPlayerService> mService;

    public IMusicServiceStub(final MusicPlayerService service) {
        mService = new WeakReference<>(service);
    }

    @Override
    public int getCurrentPosition() throws RemoteException {
        return mService.get().getCurrentPosition();
    }

    @Override
    public void setPlayPosition(int id) throws RemoteException {
        mService.get().setPlayPosition(id);
    }

    @Override
    public void setNextSong(SongBean songBean) throws RemoteException {
        mService.get().setNextSong(songBean);
    }

    @Override
    public void setPlaySong(SongBean songBean) throws RemoteException {
        mService.get().setPlaySong(songBean);
    }

    @Override
    public int getPlayPosition() throws RemoteException {
        return mService.get().getPlayPosition();
    }

    @Override
    public SongBean getPlaySong() throws RemoteException {
        return mService.get().getPlayingMusic();
    }

    @Override
    public List<SongBean> getPlaySongQueue() throws RemoteException {
        return mService.get().getPlayQueue();
    }

    @Override
    public void setPlaySongList(List<SongBean> playlist, int position, String playListId) throws RemoteException {
        mService.get().setPlaySongList(playlist, position, playListId);
    }

    @Override
    public void togglePlayer() throws RemoteException {
        mService.get().togglePlayer();
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
        mService.get().next();
    }

    @Override
    public void seekTo(int pos) throws RemoteException {
        mService.get().seekTo(pos);
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
    public int getDuration() throws RemoteException {
        return mService.get().getDuration();
    }

    @Override
    public boolean isPlaying() throws RemoteException {
        return mService.get().isPlaying();
    }

    @Override
    public boolean isPrepared() throws RemoteException {
        return mService.get().isPrepared();
    }
}