package com.kaibo.music.player.engine;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

import com.kaibo.music.player.Constants;
import com.kaibo.music.player.handler.MusicPlayerHandler;
import com.kaibo.music.player.service.MusicPlayerService;

import java.lang.ref.WeakReference;

/**
 * 封装后的MediaPlayer播放器
 * 以每一首歌为播放单元
 */

public class MusicPlayerEngine implements
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener {

    private final WeakReference<MusicPlayerService> mService;

    /**
     * 真正的播放器
     */
    private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

    /**
     * Handler
     */
    private Handler mHandler;

    /**
     * 是否已经初始化
     */
    private boolean mIsInitialized = false;

    /**
     * 是否准备完成
     */
    private boolean mIsPrepared = false;

    public MusicPlayerEngine(final MusicPlayerService service, MusicPlayerHandler mHandler) {
        mService = new WeakReference<>(service);
        // 锁定CPU,屏幕锁定后  CPU依然继续运行
        mCurrentMediaPlayer.setWakeMode(service, PowerManager.PARTIAL_WAKE_LOCK);
        this.mHandler = mHandler;
    }

    /**
     * 设置播放源
     *
     * @param path
     */
    public void setDataSource(final String path) {
        // 返回是否初始化成功
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public boolean isPrepared() {
        return mIsPrepared;
    }

    /**
     * 启动播放
     */
    public void start() {
        mCurrentMediaPlayer.start();
    }

    /**
     * 停止播放
     */
    public void stop() {
        try {
            mCurrentMediaPlayer.reset();
            mIsInitialized = false;
            mIsPrepared = false;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放播放器
     */
    public void release() {
        mCurrentMediaPlayer.release();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        mCurrentMediaPlayer.pause();
    }

    public boolean isPlaying() {
        return mCurrentMediaPlayer.isPlaying();
    }

    /**
     * getDuration 只能在prepared之后才能调用，不然会报-38错误
     *
     * @return
     */
    public int duration() {
        if (mIsPrepared) {
            return mCurrentMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    public int position() {
        try {
            return mCurrentMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            return -1;
        }
    }

    public void seek(final int whereto) {
        mCurrentMediaPlayer.seekTo(whereto);
    }

    /**
     * 音量控制
     *
     * @param vol
     */
    public void setVolume(final float vol) {
        mCurrentMediaPlayer.setVolume(vol, vol);
    }

    public int getAudioSessionId() {
        return mCurrentMediaPlayer.getAudioSessionId();
    }

    private boolean setDataSourceImpl(MediaPlayer player, String path) {
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
            return true;
        } catch (Exception todo) {
            todo.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                mIsInitialized = false;
                // 释放上一个播放器的资源
                mCurrentMediaPlayer.release();
                // 重新创建一个新的播放器
                mCurrentMediaPlayer = new MediaPlayer();
                MusicPlayerService service = mService.get();
                // 屏幕长亮
                mCurrentMediaPlayer.setWakeMode(service, PowerManager.PARTIAL_WAKE_LOCK);
                // 将错误信息发送出去
                TrackErrorInfo errorInfo = new TrackErrorInfo(service.getAudioId(), service.getSongName());
                Message msg = mHandler.obtainMessage(Constants.TRACK_PLAY_ERROR, errorInfo);
                mHandler.sendMessageDelayed(msg, 2000);
                return true;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
        if (mp == mCurrentMediaPlayer) {
            // 播放完成  切换下一首歌
            mHandler.sendEmptyMessage(Constants.TRACK_WENT_TO_NEXT);
        } else {
            mService.get().mWakeLock.acquire(30000);
            mHandler.sendEmptyMessage(Constants.TRACK_PLAY_ENDED);
            mHandler.sendEmptyMessage(Constants.RELEASE_WAKELOCK);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // 正在加载 percent 加载进度
        mHandler.obtainMessage(Constants.PREPARE_ASYNC_UPDATE, percent).sendToTarget();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // 准备完成
        mp.start();
        // 准备结束标记置为true
        mIsPrepared = true;
        mHandler.obtainMessage(Constants.PLAYER_PREPARED).sendToTarget();
    }

    public static class TrackErrorInfo {
        private int audioId;
        private String trackName;

        public TrackErrorInfo(int audioId, String trackName) {
            this.audioId = audioId;
            this.trackName = trackName;
        }

        public int getAudioId() {
            return audioId;
        }

        public String getTrackName() {
            return trackName;
        }
    }
}
