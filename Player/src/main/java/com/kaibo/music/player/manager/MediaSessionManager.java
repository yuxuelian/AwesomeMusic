package com.kaibo.music.player.manager;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.kaibo.music.bean.SongBean;
import com.kaibo.music.player.service.IMusicServiceStub;
import com.kaibo.music.utils.DownLoadManager;

import io.reactivex.disposables.Disposable;

/**
 * MediaSession管理类
 * 主要管理Android 5.0以后线控和蓝牙远程控制播放
 */

public class MediaSessionManager {

    private static final String TAG = "MediaSessionManager";

    /**
     * 指定可以接收的来自锁屏页面的按键信息
     */
    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;

    /**
     * 播放器控制器
     */
    private final IMusicServiceStub control;
    private final Context context;
    private Handler mHandler;

    private MediaSessionCompat mMediaSession;

    /**
     * API 21 以上 耳机多媒体按钮监听 MediaSessionCompat.Callback
     */
    private MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            try {
                control.togglePlayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPause() {
            try {
                control.togglePlayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSkipToNext() {
            try {
                control.next();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSkipToPrevious() {
            try {
                control.prev();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStop() {
            try {
                control.togglePlayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSeekTo(long pos) {
            try {
                control.seekTo((int) pos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public MediaSessionManager(IMusicServiceStub control, Context context, Handler mHandler) {
        this.control = control;
        this.context = context;
        this.mHandler = mHandler;
        setupMediaSession();
    }

    /**
     * 初始化并激活 MediaSession
     */
    private void setupMediaSession() {
//        第二个参数 tag: 这个是用于调试用的,随便填写即可
        mMediaSession = new MediaSessionCompat(context, TAG);
        //指明支持的按键信息类型
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setCallback(callback, mHandler);
        mMediaSession.setActive(true);
    }

    /**
     * 更新播放状态， 播放／暂停／拖动进度条时调用
     */
    public void updatePlaybackState() {
        int state = isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setActions(MEDIA_SESSION_ACTIONS).setState(state, getCurrentPosition(), 1).build());
    }

    private long getCurrentPosition() {
        try {
            return control.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 是否在播放
     *
     * @return
     */
    protected boolean isPlaying() {
        try {
            return control.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新正在播放的音乐信息，切换歌曲时调用
     */
    public void updateMetaData(SongBean songInfo) {
        if (songInfo == null) {
            mMediaSession.setMetadata(null);
            return;
        }

        final MediaMetadataCompat.Builder metaDta = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songInfo.getSongname())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songInfo.getSingername())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, songInfo.getSingername())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            metaDta.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, getCount());
        }

        Disposable disposable = DownLoadManager.INSTANCE.downImage(songInfo.getImage()).subscribe(bitmap -> {
            // 图片加载完成
            metaDta.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
            mMediaSession.setMetadata(metaDta.build());
        }, Throwable::printStackTrace);
    }

    private int getDuration() {
        try {
            return control.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getCount() {
        try {
            return control.getPlaySongQueue().size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 释放MediaSession，退出播放器时调用
     */
    public void release() {
        mMediaSession.setCallback(null);
        mMediaSession.setActive(false);
        mMediaSession.release();
    }
}
