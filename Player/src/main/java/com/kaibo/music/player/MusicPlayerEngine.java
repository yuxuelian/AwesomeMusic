package com.kaibo.music.player;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;

import static com.kaibo.music.player.MusicPlayerService.PLAYER_PREPARED;
import static com.kaibo.music.player.MusicPlayerService.PREPARE_ASYNC_UPDATE;
import static com.kaibo.music.player.MusicPlayerService.RELEASE_WAKELOCK;
import static com.kaibo.music.player.MusicPlayerService.TRACK_PLAY_ENDED;
import static com.kaibo.music.player.MusicPlayerService.TRACK_PLAY_ERROR;
import static com.kaibo.music.player.MusicPlayerService.TRACK_WENT_TO_NEXT;

/**
 * Mediaplayer的回调
 */

class MusicPlayerEngine implements
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener {

    private static final String TAG = "MusicPlayerEngine";

    private final WeakReference<MusicPlayerService> mService;

    /**
     * 真正的播放器
     */
    private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

    private Handler mHandler;

    /**
     * 是否已经初始化
     */
    private boolean mIsInitialized = false;

    /**
     * 是否已经初始化
     */
    private boolean mIsPrepared = false;

    MusicPlayerEngine(final MusicPlayerService service) {
        mService = new WeakReference<>(service);
        mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    void setDataSource(final String path) {
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
    }

    void setHandler(final Handler handler) {
        mHandler = handler;
    }

    boolean isInitialized() {
        return mIsInitialized;
    }

    boolean isPrepared() {
        return mIsPrepared;
    }

    void start() {
        mCurrentMediaPlayer.start();
    }

    void stop() {
        try {
            mCurrentMediaPlayer.reset();
            mIsInitialized = false;
            mIsPrepared = false;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    void release() {
        mCurrentMediaPlayer.release();
    }

    void pause() {
        mCurrentMediaPlayer.pause();
    }

    boolean isPlaying() {
        return mCurrentMediaPlayer.isPlaying();
    }

    /**
     * getDuration 只能在prepared之后才能调用，不然会报-38错误
     *
     * @return
     */
    int duration() {
        if (mIsPrepared) {
            return mCurrentMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    int position() {
        try {
            return mCurrentMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            return -1;
        }
    }

    void seek(final int whereto) {
        mCurrentMediaPlayer.seekTo(whereto);
    }

    void setVolume(final float vol) {
        mCurrentMediaPlayer.setVolume(vol, vol);
    }

    int getAudioSessionId() {
        return mCurrentMediaPlayer.getAudioSessionId();
    }

    private boolean setDataSourceImpl(final MediaPlayer player, final String path) {
        if (path == null) {
            return false;
        }
        try {
            if (player.isPlaying()) {
                player.stop();
            }
            mIsPrepared = false;
            player.reset();
            if (path.startsWith("content://")) {
                player.setDataSource(mService.get(), Uri.parse(path));
            } else {
                player.setDataSource(path);
            }
            player.prepareAsync();
            player.setOnPreparedListener(this);
            player.setOnBufferingUpdateListener(this);
            player.setOnErrorListener(this);
            player.setOnCompletionListener(this);
        } catch (Exception todo) {
            todo.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                final MusicPlayerService service = mService.get();
                final TrackErrorInfo errorInfo = new TrackErrorInfo(service.getAudioId(), service.getSongName());
                mIsInitialized = false;
                mCurrentMediaPlayer.release();
                mCurrentMediaPlayer = new MediaPlayer();
                mCurrentMediaPlayer.setWakeMode(service, PowerManager.PARTIAL_WAKE_LOCK);
                Message msg = mHandler.obtainMessage(TRACK_PLAY_ERROR, errorInfo);
                mHandler.sendMessageDelayed(msg, 2000);
                return true;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
        Logger.e(TAG, "onCompletion");
        if (mp == mCurrentMediaPlayer) {
            mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
        } else {
            mService.get().mWakeLock.acquire(30000);
            mHandler.sendEmptyMessage(TRACK_PLAY_ENDED);
            mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Message message = mHandler.obtainMessage(PREPARE_ASYNC_UPDATE, percent);
        mHandler.sendMessage(message);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        mIsPrepared = true;
        Message message = mHandler.obtainMessage(PLAYER_PREPARED);
        mHandler.sendMessage(message);
    }

    private class TrackErrorInfo {
        private String audioId;
        private String trackName;

        public TrackErrorInfo(String audioId, String trackName) {
            this.audioId = audioId;
            this.trackName = trackName;
        }

        public String getAudioId() {
            return audioId;
        }

        public void setAudioId(String audioId) {
            this.audioId = audioId;
        }

        public String getTrackName() {
            return trackName;
        }

        public void setTrackName(String trackName) {
            this.trackName = trackName;
        }
    }
}
