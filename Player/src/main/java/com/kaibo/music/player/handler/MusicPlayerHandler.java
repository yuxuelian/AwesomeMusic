package com.kaibo.music.player.handler;

import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.kaibo.music.player.Constants;
import com.kaibo.music.player.manager.PlayModeManager;
import com.kaibo.music.player.service.MusicPlayerService;

import java.lang.ref.WeakReference;

/**
 * @author kaibo
 * @date 2018/10/22 18:04
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

public class MusicPlayerHandler extends Handler {
    private final WeakReference<MusicPlayerService> mService;
    private float mCurrentVolume = 1.0f;

    public MusicPlayerHandler(final MusicPlayerService service, final Looper looper) {
        super(looper);
        mService = new WeakReference<>(service);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        MusicPlayerService service = mService.get();
        switch (msg.what) {
            case Constants.VOLUME_FADE_DOWN:
                // 音量逐渐降低
                mCurrentVolume -= 0.01f;
                if (mCurrentVolume > 0.2f) {
                    sendEmptyMessageDelayed(Constants.VOLUME_FADE_DOWN, 10);
                } else {
                    // 小于0.2f的时候  设置为0.2f
                    mCurrentVolume = 0.2f;
                }
                // 改变音量
                service.mPlayer.setVolume(mCurrentVolume);
                break;
            case Constants.VOLUME_FADE_UP:
                // 音量逐渐升高
                mCurrentVolume += 0.01f;
                if (mCurrentVolume < 1.0f) {
                    sendEmptyMessageDelayed(Constants.VOLUME_FADE_UP, 10);
                } else {
                    // 大于1.0f的时候  设置为1.0f
                    mCurrentVolume = 1.0f;
                }
                service.mPlayer.setVolume(mCurrentVolume);
                break;
            case Constants.TRACK_WENT_TO_NEXT:
                //mplayer播放完毕切换到下一首
                service.mMainHandler.post(() -> service.next(true));
                break;
            case Constants.TRACK_PLAY_ENDED:
                //mPlayer播放完毕且暂时没有下一首
                if (PlayModeManager.INSTANCE.getPlayModeId() == PlayModeManager.PLAY_MODE_REPEAT) {
                    service.seekTo(0);
                    service.mMainHandler.post(service::play);
                } else {
                    service.mMainHandler.post(() -> service.next(true));
                }
                break;
            case Constants.TRACK_PLAY_ERROR:
                //mPlayer播放错误
                service.mMainHandler.post(() -> service.next(true));
                break;
            case Constants.RELEASE_WAKELOCK:
                //释放电源锁
                service.mWakeLock.release();
                break;
            case Constants.PREPARE_ASYNC_UPDATE:
                service.notifyChange(Constants.PLAY_STATE_LOADING_CHANGED);
                break;
            case Constants.PLAYER_PREPARED:
                //执行prepared之后 准备完成，更新总时长
                service.notifyChange(Constants.PLAY_STATE_CHANGED);
                break;
            case Constants.AUDIO_FOCUS_CHANGE:
                switch (msg.arg1) {
                    //失去音频焦点
                    case AudioManager.AUDIOFOCUS_LOSS:
                        //暂时失去焦点
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        if (service.isPlaying()) {
                            service.mPausedByTransientLossOfFocus = msg.arg1 == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                        }
                        service.mMainHandler.post(service::pause);
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        removeMessages(Constants.VOLUME_FADE_UP);
                        sendEmptyMessage(Constants.VOLUME_FADE_DOWN);
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        //重新获取焦点
                        //重新获得焦点，且符合播放条件，开始播放
                        if (!service.isPlaying() && service.mPausedByTransientLossOfFocus) {
                            service.mPausedByTransientLossOfFocus = false;
                            mCurrentVolume = 0f;
                            service.mPlayer.setVolume(mCurrentVolume);
                            service.mMainHandler.post(service::play);
                        } else {
                            removeMessages(Constants.VOLUME_FADE_DOWN);
                            sendEmptyMessage(Constants.VOLUME_FADE_UP);
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }
}
