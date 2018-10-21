package com.kaibo.music.player;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;

import com.kaibo.core.http.DownLoadManager;
import com.kaibo.music.bean.SongBean;
import com.kaibo.music.bean.data.PlayHistoryLoader;
import com.kaibo.music.bean.data.PlayQueueLoader;
import com.kaibo.music.player.playqueue.PlayQueueManager;
import com.kaibo.music.utils.FileUtils;
import com.kaibo.music.utils.SPUtils;
import com.kaibo.music.utils.SystemUtils;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * 作者：yonglong on 2016/8/11 19:16
 * 邮箱：643872807@qq.com
 * 版本：2.5 播放service
 */
public class MusicPlayerService extends Service {

    private static final String TAG = "MusicPlayerService";
    public static final String ACTION_SERVICE = "com.kaibo.music.service";// 广播标志

    //歌曲类型
    public static final String LOCAL = "local";
    public static final String QQ = "qq";
    public static final String PLAYLIST_QUEUE_ID = "queue";
    public static final String IS_URL_HEADER = "http";
    public static final int REQUEST_CODE_FLOAT_WINDOW = 0x123;

    public static final String PLAY_STATUS = "play_status";
    public static final String SONG = "song";

    //    通知栏
    public static final String ACTION_NEXT = "com.kaibo.music.notify.next";// 下一首广播标志
    public static final String ACTION_PREV = "com.kaibo.music.notify.prev";// 上一首广播标志
    public static final String ACTION_PLAY_PAUSE = "com.kaibo.music.notify.play_state";// 播放暂停广播
    public static final String ACTION_CLOSE = "com.kaibo.music.notify.close";// 关闭播放器
    public static final String ACTION_LYRIC = "com.kaibo.music.notify.lyric";// 歌词
    public static final String ACTION_REPEAT = "com.kaibo.music.notify.repeat";// 循环方式

    public static final String PLAY_STATE_CHANGED = "com.kaibo.music.play_state";// 播放暂停广播
    public static final String PLAY_STATE_LOADING_CHANGED = "com.kaibo.music.play_state_loading";// 播放loading
    public static final String DURATION_CHANGED = "com.kaibo.music.duration";// 播放时长
    public static final String TRACK_ERROR = "com.kaibo.music.error";
    public static final String SHUTDOWN = "com.kaibo.music.shutdown";
    public static final String REFRESH = "com.kaibo.music.refresh";
    public static final String PLAY_QUEUE_CLEAR = "com.kaibo.music.play_queue_clear"; //清空播放队列
    public static final String PLAY_QUEUE_CHANGE = "com.kaibo.music.play_queue_change"; //播放队列改变
    public static final String META_CHANGED = "com.kaibo.music.metachanged";//状态改变(歌曲替换)
    public static final String SCHEDULE_CHANGED = "com.kaibo.music.schedule";//定时广播
    public static final String CMD_TOGGLE_PAUSE = "toggle_pause";//按键播放暂停
    public static final String CMD_PREVIOUS = "previous";//按键上一首
    public static final String CMD_NEXT = "next";//按键下一首
    public static final String CMD_PAUSE = "pause";//按键暂停
    public static final String CMD_PLAY = "play";//按键播放
    public static final String CMD_STOP = "stop";//按键停止
    public static final String CMD_FORWARD = "forward";//按键停止
    public static final String CMD_REWIND = "reward";//按键停止
    public static final String SERVICE_CMD = "cmd_service";//状态改变
    public static final String FROM_MEDIA_BUTTON = "media";//状态改变
    public static final String CMD_NAME = "name";//命令Key

    public static final int TRACK_WENT_TO_PREV = 1; //上一首
    public static final int TRACK_WENT_TO_NEXT = 2; //下一首
    public static final int RELEASE_WAKELOCK = 3; //释放电源锁
    public static final int TRACK_PLAY_ENDED = 4; //播放完成
    public static final int TRACK_PLAY_ERROR = 5; //播放出错
    public static final int PREPARE_ASYNC_UPDATE = 7; //PrepareAsync装载进程
    public static final int PLAYER_PREPARED = 8; //mediaplayer准备完成
    public static final int AUDIO_FOCUS_CHANGE = 12; //音频焦点改变
    public static final int VOLUME_FADE_DOWN = 13; //音量调低
    public static final int VOLUME_FADE_UP = 14; //音量调高

    /**
     * 展示播放的Activity界面
     */
    private static final String PLAY_ACTIVITY_CLASS = "com.kaibo.music.activity.PlayerActivity";

    /**
     * 通知栏id
     */
    private static final int NOTIFICATION_ID = 0x123;

    /**
     * Service的启动Id  由onStartCommand方法传入,使用stopself时用到
     */
    private int mServiceStartId = -1;

    /**
     * 错误次数，超过最大错误次数，自动停止播放
     */
    private int playErrorTimes = 0;

    /**
     * 错误最大重试次数
     */
    private static final int MAX_ERROR_TIMES = 5;

    /**
     * 封装后的播放器
     */
    private MusicPlayerEngine mPlayer = null;

    /**
     * 电源锁
     */
    public PowerManager.WakeLock mWakeLock;

    public SongBean mPlayingMusic = null;
    private List<SongBean> mPlayQueue = new ArrayList<>();
    private List<Integer> mHistoryPos = new ArrayList<>();

    /**
     * 记录播放位置
     */
    private int mPlayingPos = -1;

    private String mPlaylistId = PLAYLIST_QUEUE_ID;

    //广播接收者
    ServiceReceiver mServiceReceiver;

    HeadsetReceiver mHeadsetReceiver;

    HeadsetPlugInReceiver mHeadsetPlugInReceiver;

    /**
     * 桌面歌词管理
     */
    private FloatLyricViewManager mFloatLyricViewManager;
    /**
     * 线控和蓝牙远程控制播放
     */
    private MediaSessionManager mediaSessionManager;
    /**
     * 管理音频焦点
     */
    private AudioAndFocusManager audioAndFocusManager;

    /**
     * 通知
     */
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private Notification mNotification;

    /**
     * 进程间通信
     */
    private IMusicServiceStub mBindStub = new IMusicServiceStub(this);

    /**
     * 是否正在前台运行
     */
    private boolean isRunningForeground = false;

    /**
     * 是否音乐正在播放
     */
    private boolean isMusicPlaying = false;

    /**
     * 暂时失去焦点，会再次回去音频焦点
     */
    private boolean mPausedByTransientLossOfFocus = false;

    boolean mServiceInUse = false;

    /**
     * 工作线程和Handler
     */
    private MusicPlayerHandler mHandler;
    private HandlerThread mWorkThread;

    /**
     * 主线程Handler
     */
    private Handler mMainHandler;

    private boolean showLyric;

    /**
     * 通知栏的布局
     */
    private RemoteViews notRemoteView;
    private RemoteViews bigNotRemoteView;

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
            synchronized (mService) {
                switch (msg.what) {
                    case VOLUME_FADE_DOWN:
                        mCurrentVolume -= 0.05f;
                        if (mCurrentVolume > 0.2f) {
                            sendEmptyMessageDelayed(VOLUME_FADE_DOWN, 10);
                        } else {
                            mCurrentVolume = 0.2f;
                        }
                        service.mPlayer.setVolume(mCurrentVolume);
                        break;
                    case VOLUME_FADE_UP:
                        mCurrentVolume += 0.01f;
                        if (mCurrentVolume < 1.0f) {
                            sendEmptyMessageDelayed(VOLUME_FADE_UP, 10);
                        } else {
                            mCurrentVolume = 1.0f;
                        }
                        service.mPlayer.setVolume(mCurrentVolume);
                        break;
                    case TRACK_WENT_TO_NEXT:
                        //mplayer播放完毕切换到下一首
                        mMainHandler.post(() -> service.next(true));
                        break;
                    case TRACK_PLAY_ENDED:
                        //mPlayer播放完毕且暂时没有下一首
                        if (PlayQueueManager.INSTANCE.getPlayModeId() == PlayQueueManager.PLAY_MODE_REPEAT) {
                            service.seekTo(0);
                            mMainHandler.post(service::play);
                        } else {
                            mMainHandler.post(() -> service.next(true));
                        }
                        break;
                    case TRACK_PLAY_ERROR:
                        //mPlayer播放错误
                        Logger.e(TAG, msg.obj + "---");
                        mMainHandler.post(() -> service.next(true));
                        break;
                    case RELEASE_WAKELOCK:
                        //释放电源锁
                        service.mWakeLock.release();
                        break;
                    case PREPARE_ASYNC_UPDATE:
                        int percent = (int) msg.obj;
                        Logger.e(TAG, "Loading ... " + percent);
                        notifyChange(PLAY_STATE_LOADING_CHANGED);
                        break;
                    case PLAYER_PREPARED:
                        //执行prepared之后 准备完成，更新总时长
                        notifyChange(PLAY_STATE_CHANGED);
                        break;
                    case AUDIO_FOCUS_CHANGE:
                        switch (msg.arg1) {
                            //失去音频焦点
                            case AudioManager.AUDIOFOCUS_LOSS:
                                //暂时失去焦点
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                if (service.isPlaying()) {
                                    mPausedByTransientLossOfFocus = msg.arg1 == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                                }
                                mMainHandler.post(service::pause);
                                break;
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                removeMessages(VOLUME_FADE_UP);
                                sendEmptyMessage(VOLUME_FADE_DOWN);
                                break;
                            case AudioManager.AUDIOFOCUS_GAIN:
                                //重新获取焦点
                                //重新获得焦点，且符合播放条件，开始播放
                                if (!service.isPlaying() && mPausedByTransientLossOfFocus) {
                                    mPausedByTransientLossOfFocus = false;
                                    mCurrentVolume = 0f;
                                    service.mPlayer.setVolume(mCurrentVolume);
                                    mMainHandler.post(service::play);
                                } else {
                                    removeMessages(VOLUME_FADE_DOWN);
                                    sendEmptyMessage(VOLUME_FADE_UP);
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
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化广播
        initReceiver();
        //初始化参数
        initConfig();
        //初始化电话监听服务
        initTelephony();
        //初始化通知
        initNotify();
        //初始化音乐播放服务
        initMediaPlayer();
    }

    /**
     * 初始化广播
     */
    private void initReceiver() {
        //实例化过滤器，设置广播
        mServiceReceiver = new ServiceReceiver();
        mHeadsetReceiver = new HeadsetReceiver();
        mHeadsetPlugInReceiver = new HeadsetPlugInReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_SERVICE);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREV);
        intentFilter.addAction(SHUTDOWN);
        intentFilter.addAction(ACTION_PLAY_PAUSE);
        //注册广播
        registerReceiver(mServiceReceiver, intentFilter);
        registerReceiver(mHeadsetReceiver, intentFilter);
        registerReceiver(mHeadsetPlugInReceiver, intentFilter);
    }

    /**
     * 参数配置，AudioManager、锁屏
     */
    @SuppressLint("InvalidWakeLockTag")
    private void initConfig() {
        //初始化主线程Handler
        mMainHandler = new Handler(Looper.getMainLooper());
        PlayQueueManager.INSTANCE.getPlayModeId();
        //初始化工作线程
        mWorkThread = new HandlerThread("MusicPlayerThread");
        mWorkThread.start();
        mHandler = new MusicPlayerHandler(this, mWorkThread.getLooper());
        //电源键
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PlayerWakelockTag");
        mFloatLyricViewManager = new FloatLyricViewManager(this);
        //初始化和设置MediaSessionCompat
        mediaSessionManager = new MediaSessionManager(mBindStub, this, mMainHandler);
        audioAndFocusManager = new AudioAndFocusManager(this, mHandler);
    }

    /**
     * 初始化电话监听服务
     */
    private void initTelephony() {
        // 获取电话通讯服务
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        // 创建一个监听对象，监听电话状态改变事件
        telephonyManager.listen(new ServicePhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * 初始化音乐播放服务
     */
    private void initMediaPlayer() {
        mPlayer = new MusicPlayerEngine(this);
        mPlayer.setHandler(mHandler);
        //
        reloadPlayQueue();
    }

    /**
     * 释放通知栏;
     */
    private void releaseServiceUiAndStop() {
        if (isPlaying() || mHandler.hasMessages(TRACK_PLAY_ENDED)) {
            return;
        }
        cancelNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager.release();
        }
        if (!mServiceInUse) {
            savePlayQueue(false);
            stopSelf(mServiceStartId);
        }
    }

    /**
     * 重新加载当前进度
     */
    public void reloadPlayQueue() {
        mPlayQueue.clear();
        mHistoryPos.clear();
        mPlayQueue = PlayQueueLoader.INSTANCE.getPlayQueue();
        mPlayingPos = SPUtils.getPlayPosition();
        if (mPlayingPos >= 0 && mPlayingPos < mPlayQueue.size()) {
            mPlayingMusic = mPlayQueue.get(mPlayingPos);
            updateNotification(false);
            notifyChange(META_CHANGED);
        }
        notifyChange(PLAY_QUEUE_CHANGE);
    }

    /**
     * 启动Service服务，执行onStartCommand
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;
        mServiceInUse = true;
        if (intent != null) {
            final String action = intent.getAction();
            if (SHUTDOWN.equals(action)) {
                releaseServiceUiAndStop();
            } else {
                handleCommandIntent(intent);
            }
        }
        return START_NOT_STICKY;
    }

    /**
     * 绑定Service
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBindStub;
    }

    /**
     * 下一首
     */
    public void next(Boolean isAuto) {
        mPlayingPos = getNextPosition(isAuto);
        stop(false);
        playCurrentAndNext();
    }

    /**
     * 上一首
     */
    public void prev() {
        mPlayingPos = getPreviousPosition();
        stop(false);
        playCurrentAndNext();
    }

    /**
     * 播放当前歌曲
     */
    private void playCurrentAndNext() {
        if (mPlayingPos >= mPlayQueue.size() || mPlayingPos < 0) {
            return;
        }
        mPlayingMusic = mPlayQueue.get(mPlayingPos);
        notifyChange(META_CHANGED);
        if (!LOCAL.equals(mPlayingMusic.getType()) || "".equals(mPlayingMusic.getUrl()) || "null".equals(mPlayingMusic.getUrl())) {
            saveHistory();
            isMusicPlaying = true;
            playErrorTimes = 0;
            mPlayer.setDataSource(mPlayingMusic.getUrl());
        }
        saveHistory();
        mHistoryPos.add(mPlayingPos);
        isMusicPlaying = true;
        if (!mPlayingMusic.getUrl().startsWith(IS_URL_HEADER) && !FileUtils.exists(mPlayingMusic.getUrl())) {
            isAbnormalPlay();
        } else {
            playErrorTimes = 0;
            mPlayer.setDataSource(mPlayingMusic.getUrl());
        }
        mediaSessionManager.updateMetaData(mPlayingMusic);
        audioAndFocusManager.requestAudioFocus();
        updateNotification(false);
        final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(intent);
        if (mPlayer.isInitialized()) {
            mHandler.removeMessages(VOLUME_FADE_DOWN);
            //组件调到正常音量
            mHandler.sendEmptyMessage(VOLUME_FADE_UP);
            isMusicPlaying = true;
        }
    }

    /**
     * 异常播放，自动切换下一首
     */
    private void isAbnormalPlay() {
        if (playErrorTimes > MAX_ERROR_TIMES) {
            pause();
        } else {
            playErrorTimes++;
            // TODO 播放异常
//            ToastUtils.showInfo("播放地址异常，自动切换下一首");
            next(false);
        }
    }

    /**
     * 停止播放
     *
     * @param removeStatusIcon
     */
    public void stop(boolean removeStatusIcon) {
        if (mPlayer != null && mPlayer.isInitialized()) {
            mPlayer.stop();
        }

        if (removeStatusIcon) {
            cancelNotification();
        }

        if (removeStatusIcon) {
            isMusicPlaying = false;
        }
    }

    /**
     * 获取下一首位置
     *
     * @return
     */
    private int getNextPosition(Boolean isAuto) {
        int playModeId = PlayQueueManager.INSTANCE.getPlayModeId();
        if (mPlayQueue == null || mPlayQueue.isEmpty()) {
            return -1;
        }
        if (mPlayQueue.size() == 1) {
            return 0;
        }
        if (playModeId == PlayQueueManager.PLAY_MODE_REPEAT && isAuto) {
            if (mPlayingPos < 0) {
                return 0;
            } else {
                return mPlayingPos;
            }
        } else if (playModeId == PlayQueueManager.PLAY_MODE_RANDOM) {
            return new Random().nextInt(mPlayQueue.size());
        } else {
            if (mPlayingPos == mPlayQueue.size() - 1) {
                return 0;
            } else if (mPlayingPos < mPlayQueue.size() - 1) {
                return mPlayingPos + 1;
            }
        }
        return mPlayingPos;
    }

    /**
     * 获取上一首位置
     *
     * @return
     */
    private int getPreviousPosition() {
        int playModeId = PlayQueueManager.INSTANCE.getPlayModeId();
        if (mPlayQueue == null || mPlayQueue.isEmpty()) {
            return -1;
        }
        if (mPlayQueue.size() == 1) {
            return 0;
        }
        if (playModeId == PlayQueueManager.PLAY_MODE_REPEAT) {
            if (mPlayingPos < 0) {
                return 0;
            }
        } else if (playModeId == PlayQueueManager.PLAY_MODE_RANDOM) {
            mPlayingPos = new Random().nextInt(mPlayQueue.size());
            return new Random().nextInt(mPlayQueue.size());
        } else {
            if (mPlayingPos == 0) {
                return mPlayQueue.size() - 1;
            } else if (mPlayingPos > 0) {
                return mPlayingPos - 1;
            }
        }
        return mPlayingPos;
    }

    /**
     * 根据位置播放音乐
     *
     * @param position
     */
    public void playMusic(int position) {
        if (position >= mPlayQueue.size() || position == -1) {
            mPlayingPos = getNextPosition(true);
        } else {
            mPlayingPos = position;
        }
        if (mPlayingPos == -1) {
            return;
        }
        playCurrentAndNext();
    }

    /**
     * 音乐播放
     */
    public void play() {
        if (mPlayer.isInitialized()) {
            mPlayer.start();
            isMusicPlaying = true;
            notifyChange(PLAY_STATE_CHANGED);
            audioAndFocusManager.requestAudioFocus();
            mHandler.removeMessages(VOLUME_FADE_DOWN);
            //组件调到正常音量
            mHandler.sendEmptyMessage(VOLUME_FADE_UP);
            updateNotification(true);
        } else {
            playCurrentAndNext();
        }
    }

    public int getAudioSessionId() {
        return mPlayer.getAudioSessionId();
    }

    /**
     * 【在线音乐】加入播放队列并播放音乐
     *
     * @param music
     */
    public void play(SongBean music) {
        if (mPlayingPos == -1 || mPlayQueue.size() == 0) {
            mPlayQueue.add(music);
            mPlayingPos = 0;
        } else if (mPlayingPos < mPlayQueue.size()) {
            mPlayQueue.add(mPlayingPos, music);
        } else {
            mPlayQueue.add(mPlayQueue.size(), music);
        }
        Logger.e(TAG, music.toString());
        mPlayingMusic = music;
        playCurrentAndNext();
    }

    /**
     * 下一首播放
     *
     * @param music 设置的歌曲
     */
    public void nextPlay(SongBean music) {
        if (mPlayQueue.size() == 0) {
            play(music);
        } else if (mPlayingPos < mPlayQueue.size()) {
            mPlayQueue.add(mPlayingPos + 1, music);
        }
    }

    /**
     * 切换歌单播放
     * 1、歌单不一样切换
     */
    public void play(List<SongBean> musicList, int id, String pid) {
        if (musicList.size() <= id) {
            return;
        }
        if (!mPlaylistId.equals(pid) || mPlayQueue.size() == 0 || mPlayQueue.size() != musicList.size()) {
            setPlayQueue(musicList);
            mPlaylistId = pid;
        }
        mPlayingPos = id;
        playCurrentAndNext();
    }

    /**
     * 播放暂停
     */
    public void playPause() {
        if (isPlaying()) {
            pause();
        } else {
            if (mPlayer.isInitialized()) {
                play();
            } else {
                isMusicPlaying = true;
                playCurrentAndNext();
            }
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        mHandler.removeMessages(VOLUME_FADE_UP);
        mHandler.sendEmptyMessage(VOLUME_FADE_DOWN);

        if (isPlaying()) {
            isMusicPlaying = false;
            notifyChange(PLAY_STATE_CHANGED);
            updateNotification(true);
            // 实时向外发送播放器状态
            Disposable subscribe = Observable.interval(200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        final Intent intent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
                        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
                        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                        //由系统接收,通知系统audio_session将关闭,不再使用音效
                        sendBroadcast(intent);
                        mPlayer.pause();
                    });
        }
    }

    /**
     * 是否正在播放音乐
     *
     * @return 是否正在播放音乐
     */
    public boolean isPlaying() {
        return isMusicPlaying;
    }

    /**
     * 跳到输入的进度
     */
    public void seekTo(int pos) {
        if (mPlayer != null && mPlayer.isInitialized() && mPlayingMusic != null) {
            mPlayer.seek(pos);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.e(TAG, "onUnbind");
        mServiceInUse = false;
        savePlayQueue(false);

        releaseServiceUiAndStop();
        stopSelf(mServiceStartId);
        return true;
    }

    /**
     * 保存播放队列
     *
     * @param full 是否存储
     */
    private void savePlayQueue(boolean full) {
        if (full) {
            PlayQueueLoader.INSTANCE.updateQueue(mPlayQueue);
        }
        if (mPlayingMusic != null) {
            //保存歌曲id
            SPUtils.saveCurrentSongId(mPlayingMusic.getMid());
        }
        //保存歌曲id
        SPUtils.setPlayPosition(mPlayingPos);
        //保存歌曲进度
        SPUtils.savePosition(getCurrentPosition());
        notifyChange(PLAY_QUEUE_CHANGE);
    }

    private void saveHistory() {
        PlayHistoryLoader.INSTANCE.addSongToHistory(mPlayingMusic);
        savePlayQueue(false);
    }

    /**
     * 获取正在播放的歌曲[本地|网络]
     */
    public void removeFromQueue(int position) {
        try {
            if (position == mPlayingPos) {
                mPlayQueue.remove(position);
                if (mPlayQueue.size() == 0) {
                    clearQueue();
                } else {
                    playMusic(position);
                }
            } else if (position > mPlayingPos) {
                mPlayQueue.remove(position);
            } else if (position < mPlayingPos) {
                mPlayQueue.remove(position);
                mPlayingPos = mPlayingPos - 1;
            }
            notifyChange(PLAY_QUEUE_CLEAR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取正在播放的歌曲[本地|网络]
     */
    public void clearQueue() {
        mPlayingMusic = null;
        isMusicPlaying = false;
        mPlayingPos = -1;
        mPlayQueue.clear();
        mHistoryPos.clear();
        savePlayQueue(true);
        stop(true);
        notifyChange(META_CHANGED);
        notifyChange(PLAY_STATE_CHANGED);
        notifyChange(PLAY_QUEUE_CLEAR);
    }

    /**
     * 获取正在播放进度
     */
    public int getCurrentPosition() {
        if (mPlayer != null && mPlayer.isInitialized()) {
            return mPlayer.position();
        } else {
            return 0;
        }
    }

    /**
     * 获取总时长
     */
    public int getDuration() {
        if (mPlayer != null && mPlayer.isInitialized() && mPlayer.isPrepared()) {
            return mPlayer.duration();
        }
        return 0;
    }

    /**
     * 是否准备播放
     *
     * @return
     */
    public boolean isPrepared() {
        if (mPlayer != null) {
            return mPlayer.isPrepared();
        }
        return false;
    }

    /**
     * 发送更新广播
     *
     * @param what 发送更新广播
     */
    private void notifyChange(final String what) {
        switch (what) {
            case META_CHANGED:
                mFloatLyricViewManager.loadLyric(mPlayingMusic);
                updateWidget(META_CHANGED);
                notifyChange(PLAY_STATE_CHANGED);
                // TODO 发送通知
//                EventBus.getDefault().post(new MetaChangedEvent(mPlayingMusic));
                break;
            case PLAY_STATE_CHANGED:
                updateWidget(PLAY_STATE_CHANGED);
                mediaSessionManager.updatePlaybackState();
//                EventBus.getDefault().post(new StatusChangedEvent(isPrepared(), isPlaying()));
                break;
            case PLAY_QUEUE_CLEAR:
            case PLAY_QUEUE_CHANGE:
//                EventBus.getDefault().post(new PlaylistEvent(PLAYLIST_QUEUE_ID, null));
                break;
            default:
                break;
        }
    }

    /**
     * 更新桌面小控件
     */
    private void updateWidget(String action) {
        Intent intent = new Intent(action);
        intent.putExtra(PLAY_STATUS, isPlaying());
        if (action.equals(META_CHANGED)) {
            intent.putExtra(SONG, mPlayingMusic);
        }
        sendBroadcast(intent);
    }

    /**
     * 获取标题
     *
     * @return
     */
    public String getSongName() {
        if (mPlayingMusic != null) {
            return mPlayingMusic.getSongname();
        }
        return null;
    }

    /**
     * 获取歌手专辑
     *
     * @return
     */
    public String getSingerName() {
        if (mPlayingMusic != null) {
            return mPlayingMusic.getSingername();
        }
        return null;
    }

    /**
     * 获取当前音乐
     *
     * @return
     */
    public SongBean getPlayingMusic() {
        if (mPlayingMusic != null) {
            return mPlayingMusic;
        }
        return null;
    }


    /**
     * 设置播放队列
     *
     * @param playQueue 播放队列
     */
    public void setPlayQueue(List<SongBean> playQueue) {
        mPlayQueue.clear();
        mHistoryPos.clear();
        mPlayQueue.addAll(playQueue);
        savePlayQueue(true);
    }

    /**
     * 获取播放队列
     *
     * @return 获取播放队列
     */
    public List<SongBean> getPlayQueue() {
        if (mPlayQueue.size() > 0) {
            return mPlayQueue;
        }
        return mPlayQueue;
    }

    /**
     * 获取当前音乐在播放队列中的位置
     *
     * @return 当前音乐在播放队列中的位置
     */
    public int getPlayPosition() {
        if (mPlayingPos >= 0) {
            return mPlayingPos;
        } else {
            return 0;
        }
    }

    /**
     * 初始化通知栏
     */
    private void initNotify() {
        // 获取发送通知的服务
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // 打开播放界面
        Intent nowPlayingIntent = new Intent();
        nowPlayingIntent.setClassName(this, PLAY_ACTIVITY_CLASS);
        // 通知被点击后发送intent  getActivity  表示Intent被Activity注册的广播接收器接收
        PendingIntent clickIntent = PendingIntent.getActivity(this, 0, nowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 创建NotificationCompat.Builder
        mNotificationBuilder = new NotificationCompat.Builder(this, initNotifyChannel())
                .setSmallIcon(R.drawable.ic_icon)
                .setContentIntent(clickIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP));

        // 小布局
        notRemoteView = new RemoteViews(getPackageName(), R.layout.player_notification);
        // 设置RemoteViews
        setupNotification(notRemoteView);
        mNotificationBuilder.setCustomContentView(notRemoteView);
        // 大布局
//        bigNotRemoteView = new RemoteViews(getPackageName(), R.layout.player_notification_expanded);
//        setupNotification(bigNotRemoteView);
//        mNotificationBuilder.setCustomBigContentView(bigNotRemoteView);

        // 创建通知
        mNotification = mNotificationBuilder.build();
    }

    /**
     * 创建Notification ChannelID
     *
     * @return 频道id
     */
    private String initNotifyChannel() {
        // 通知渠道的id
        String id = "delicate_music_01";
        // 用户可以看到的通知渠道的名字.
        CharSequence name = "DelicateMusic";
        // 用户可以看到的通知渠道的描述
        String description = "DelicateMusicController";
        // Android 8.0需要创建这个通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            mChannel.setDescription(description);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel);
        }
        return id;
    }

    /**
     * 这个通知栏布局中按钮的点击发送的Intent类型
     *
     * @param remoteViews
     */
    private void setupNotification(RemoteViews remoteViews) {
        // 播放暂停
        remoteViews.setOnClickPendingIntent(R.id.notificationPlayPause, retrievePlaybackAction(ACTION_PLAY_PAUSE));
        // 关闭
        remoteViews.setOnClickPendingIntent(R.id.notificationStop, retrievePlaybackAction(ACTION_CLOSE));
        // 上一曲
        remoteViews.setOnClickPendingIntent(R.id.notificationFRewind, retrievePlaybackAction(ACTION_PREV));
        // 下一曲
        remoteViews.setOnClickPendingIntent(R.id.notificationFForward, retrievePlaybackAction(ACTION_NEXT));
        // 词
        remoteViews.setOnClickPendingIntent(R.id.notificationLyric, retrievePlaybackAction(ACTION_LYRIC));
        // 循环模式
        remoteViews.setOnClickPendingIntent(R.id.notificationRepeat, retrievePlaybackAction(ACTION_REPEAT));
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        Intent intent = new Intent(action);
        intent.setComponent(new ComponentName(this, MusicPlayerService.class));
        return PendingIntent.getService(this, 0, intent, 0);
    }

    public String getAudioId() {
        if (mPlayingMusic != null) {
            return mPlayingMusic.getMid();
        } else {
            return null;
        }
    }

    private Disposable lyricDisposable;

    public void showDesktopLyric(boolean show) {
        if (show) {
            // 开启定时器，每隔0.5秒刷新一次
            lyricDisposable = Observable.interval(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> mFloatLyricViewManager.updateLyric(getCurrentPosition(), getDuration()));
        } else {
            if (lyricDisposable != null && !lyricDisposable.isDisposed()) {
                lyricDisposable.dispose();
            }
            // 移除桌面歌词
            mFloatLyricViewManager.removeFloatLyricView(this);
        }
    }

    /**
     * 电话监听
     */
    private class ServicePhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                //通话状态
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //通话状态
                case TelephonyManager.CALL_STATE_RINGING:
                    pause();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 更新状态栏通知
     */
    private void updateNotification(boolean changePlayStatus) {
        Disposable disposable = DownLoadManager.INSTANCE
                .downImage(mPlayingMusic.getImage())
                .subscribe(bitmap -> {
                    if (!changePlayStatus) {
                        // 更新UI
                        final String songName = getSongName();
                        final String singerName = getSingerName();
                        int playButtonResId = isMusicPlaying ? R.drawable.ic_pause : R.drawable.ic_play;
                        notRemoteView.setTextViewText(R.id.notificationSongName, songName);
                        notRemoteView.setTextViewText(R.id.notificationArtist, singerName);
                        notRemoteView.setImageViewResource(R.id.notificationPlayPause, playButtonResId);
                        notRemoteView.setImageViewBitmap(R.id.notificationCover, bitmap);
//                        bigNotRemoteView.setTextViewText(R.id.notificationSongName, songName);
//                        bigNotRemoteView.setTextViewText(R.id.notificationArtist, singerName);
//                        bigNotRemoteView.setImageViewResource(R.id.notificationPlayPause, playButtonResId);
//                        bigNotRemoteView.setImageViewBitmap(R.id.notificationCover, bitmap);
                    }
                    // 重新设置自定义View
                    mNotificationBuilder.setCustomContentView(notRemoteView);
//                    mNotificationBuilder.setCustomBigContentView(bigNotRemoteView);
                    mNotification = mNotificationBuilder.build();
                    // 更新歌词
                    mFloatLyricViewManager.updatePlayStatus(isMusicPlaying);
                    // 显示到通知栏
                    startForeground(NOTIFICATION_ID, mNotification);
                }, Throwable::printStackTrace);
    }

    /**
     * 取消通知
     */
    private void cancelNotification() {
        stopForeground(true);
        mNotificationManager.cancel(NOTIFICATION_ID);
        isRunningForeground = false;
    }

    /**
     * Service broadcastReceiver 监听service中广播
     */
    private class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleCommandIntent(intent);
        }
    }

    /**
     * Intent处理
     *
     * @param intent
     */
    private void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();
        final String command = SERVICE_CMD.equals(action) ? intent.getStringExtra(CMD_NAME) : null;
        if (CMD_NEXT.equals(command) || ACTION_NEXT.equals(action)) {
            next(false);
        } else if (CMD_PREVIOUS.equals(command) || ACTION_PREV.equals(action)) {
            prev();
        } else if (CMD_TOGGLE_PAUSE.equals(command) || PLAY_STATE_CHANGED.equals(action)
                || ACTION_PLAY_PAUSE.equals(action)) {
            if (isPlaying()) {
                pause();
                mPausedByTransientLossOfFocus = false;
            } else {
                play();
            }
        } else if (CMD_PAUSE.equals(command)) {
            pause();
            mPausedByTransientLossOfFocus = false;
        } else if (CMD_PLAY.equals(command)) {
            play();
        } else if (CMD_STOP.equals(command)) {
            pause();
            mPausedByTransientLossOfFocus = false;
            seekTo(0);
            releaseServiceUiAndStop();
        } else if (ACTION_LYRIC.equals(action)) {
            startFloatLyric();
        } else if (ACTION_CLOSE.equals(action)) {
            stop(true);
            releaseServiceUiAndStop();
            stopSelf();
        }
    }

    /**
     * 开启歌词
     */
    private void startFloatLyric() {
        if (SystemUtils.isOpenFloatWindow()) {
            showLyric = !showLyric;
            showDesktopLyric(showLyric);
        } else {
            SystemUtils.applySystemWindow();
        }
    }

    /**
     * 耳机插入广播接收器
     */
    public class HeadsetPlugInReceiver extends BroadcastReceiver {
//        final IntentFilter filter;

        public HeadsetPlugInReceiver() {
//            filter = new IntentFilter();
//            if (Build.VERSION.SDK_INT >= 21) {
//                filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
//            } else {
//                filter.addAction(Intent.ACTION_HEADSET_PLUG);
//            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("state")) {
                //通过判断 "state" 来知道状态
                final boolean isPlugIn = intent.getIntExtra("state", 0) == 1;
                Logger.e(TAG, "耳机插入状态 ：" + isPlugIn);
            }
        }
    }

    /**
     * 耳机拔出广播接收器
     */
    private class HeadsetReceiver extends BroadcastReceiver {
        //        final IntentFilter filter;
        private final BluetoothAdapter bluetoothAdapter;

        public HeadsetReceiver() {
//            filter = new IntentFilter();
//            //有线耳机拔出变化
//            filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
//            //蓝牙耳机连接变化
//            filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isRunningForeground && intent != null) {
                //当前是正在运行的时候才能通过媒体按键来操作音频
                String action = intent.getAction();
                action = action != null ? action : "";
                switch (action) {
                    // 蓝牙耳机的连接状态发生改变
                    case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                        if (bluetoothAdapter != null &&
                                BluetoothProfile.STATE_DISCONNECTED == bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) &&
                                isPlaying()) {
                            //蓝牙耳机断开连接 同时当前音乐正在播放 则将其暂停
                            pause();
                        }
                        break;
                    case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                        if (isPlaying()) {
                            //有线耳机断开连接 同时当前音乐正在播放 则将其暂停
                            pause();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        // 关闭audio媒体中心
        final Intent audioEffectsIntent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);
        // 保存播放队列
        savePlayQueue(false);
        //释放mPlayer
        if (mPlayer != null) {
            isMusicPlaying = false;
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        // 释放Handler资源
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        // 释放工作线程资源
        if (mWorkThread != null && mWorkThread.isAlive()) {
            mWorkThread.quitSafely();
            mWorkThread.interrupt();
            mWorkThread = null;
        }
        // 释放音频焦点
        audioAndFocusManager.abandonAudioFocus();
        // 取消通知栏
        cancelNotification();
        // 注销广播
        unregisterReceiver(mServiceReceiver);
        unregisterReceiver(mHeadsetReceiver);
        unregisterReceiver(mHeadsetPlugInReceiver);
        // 释放电源锁
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        super.onDestroy();
    }
}
