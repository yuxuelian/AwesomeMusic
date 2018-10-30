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

    private static final float MAX_VOLUME = 1.0f;
    private static final float MIN_VOLUME = 0.2f;


    /**
     * 中间变量用于音量的增减
     */
    private float mCurrentVolume = 1.0f;

    private final WeakReference<MusicPlayerService> mService;

    public MusicPlayerHandler(MusicPlayerService service, Looper looper) {
        super(looper);
        // 创建一个弱引用持有service  防止内存泄漏
        mService = new WeakReference<>(service);
    }

    @Override
    public void handleMessage(Message msg) {
        MusicPlayerService service = mService.get();
        if (service != null && service.mPlayer != null) {
            switch (msg.what) {
                case Constants.VOLUME_FADE_DOWN:
                    // 音量逐渐降低
                    mCurrentVolume -= 0.01f;
                    if (mCurrentVolume > MIN_VOLUME) {
                        // 每10ms变化一次音量
                        sendEmptyMessageDelayed(Constants.VOLUME_FADE_DOWN, 10);
                    } else {
                        // 小于0.2f的时候  设置为0.2f
                        mCurrentVolume = MIN_VOLUME;
                    }
                    // 改变音量
                    service.mPlayer.setVolume(mCurrentVolume);
                    break;
                case Constants.VOLUME_FADE_UP:
                    // 音量逐渐升高
                    mCurrentVolume += 0.01f;
                    if (mCurrentVolume < MAX_VOLUME) {
                        sendEmptyMessageDelayed(Constants.VOLUME_FADE_UP, 10);
                    } else {
                        // 大于1.0f的时候  设置为1.0f
                        mCurrentVolume = MAX_VOLUME;
                    }
                    service.mPlayer.setVolume(mCurrentVolume);
                    break;
                case Constants.TRACK_WENT_TO_PREV:
                    // 播放上一首歌曲
                    service.mMainHandler.post(service::prev);
                    break;
                case Constants.TRACK_WENT_TO_NEXT:
                    // 播放完毕切换到下一首 service.next(true) 在主线程被执行
                    service.mMainHandler.post(() -> service.next(true));
                    break;
                case Constants.TRACK_PLAY_ENDED:
                    // mPlayer播放完毕且暂时没有下一首
                    if (PlayModeManager.INSTANCE.getPlayModeId() == PlayModeManager.PLAY_MODE_REPEAT) {
                        // 单曲
                        service.seekTo(0);
                        service.mMainHandler.post(service::play);
                    } else {
                        service.mMainHandler.post(() -> service.next(true));
                    }
                    break;
                case Constants.TRACK_PLAY_ERROR:
                    // 播放出错的时候 去获取下一首歌开始播放
                    service.mMainHandler.post(() -> service.next(true));
                    break;
                case Constants.RELEASE_WAKELOCK:
                    //释放电源锁
                    service.mWakeLock.release();
                    break;
                case Constants.PREPARE_ASYNC_UPDATE:
                    // 正在装载
                    service.notifyChange(Constants.PLAY_STATE_LOADING_CHANGED);
                    break;
                case Constants.PLAYER_PREPARED:
                    // 装载完成  这时候可以获取播放的总时长
                    service.notifyChange(Constants.PLAY_STATE_CHANGED);
                    break;
                case Constants.AUDIO_FOCUS_CHANGE:
                    handleFocus(msg, service);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleFocus(Message msg, MusicPlayerService service) {
        switch (msg.arg1) {
            //失去音频焦点
            case AudioManager.AUDIOFOCUS_LOSS:
                //暂时失去焦点
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                service.mMainHandler.post(service::pause);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                removeMessages(Constants.VOLUME_FADE_UP);
                sendEmptyMessage(Constants.VOLUME_FADE_DOWN);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                //重新获取焦点
                //重新获得焦点，且符合播放条件，开始播放
                if (!service.isPlaying()) {
                    mCurrentVolume = 0f;
                    service.mPlayer.setVolume(mCurrentVolume);
                    // 启动播放
                    service.mMainHandler.post(service::play);
                } else {
                    // 将音量从低调到高
                    removeMessages(Constants.VOLUME_FADE_DOWN);
                    sendEmptyMessage(Constants.VOLUME_FADE_UP);
                }
                break;
            default:
                break;
        }
    }
}
