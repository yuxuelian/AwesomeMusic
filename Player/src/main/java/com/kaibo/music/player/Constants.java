package com.kaibo.music.player;

/**
 * @author kaibo
 * @date 2018/10/22 18:06
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

public interface Constants {

    /**
     * 展示播放的Activity界面
     */
    String PLAY_ACTIVITY_CLASS = "com.kaibo.music.activity.MainActivity";

    /**
     * 通知栏id
     */
    int NOTIFICATION_ID = 0x123;

    /**
     * 广播标志
     */
    String ACTION_SERVICE = "com.kaibo.music.service";

    /**
     * 请求桌面悬浮框权限的请求码
     */
    int REQUEST_CODE_FLOAT_WINDOW = 0x123;

    /**
     * 通过广播发送播放器状态时用到  是否正在播放  用于更新桌面小部件
     */
    String PLAY_STATUS = "play_status";
    String SONG = "song";

    /**
     * 通知栏 按钮的点击事件  发出的广播
     */
    String ACTION_NEXT = "com.kaibo.music.notify.next";// 下一首广播标志
    String ACTION_PREV = "com.kaibo.music.notify.prev";// 上一首广播标志
    // 通知栏的播放暂停按钮
    String ACTION_PLAY_PAUSE = "com.kaibo.music.notify.play_state";// 播放暂停广播
    String ACTION_CLOSE = "com.kaibo.music.notify.close";// 关闭播放器
    String ACTION_LYRIC = "com.kaibo.music.notify.lyric";// 歌词
    String ACTION_REPEAT = "com.kaibo.music.notify.repeat";// 循环方式

    // 歌词准备完成
    String ACTION_LYRIC_COMPLETE="com.kaibo.music.notify.lyric_complete";

    /**
     * 播放器向外共享出的播放状态广播标志,可全局更新UI 包括桌面小部件等
     */
    String PLAY_STATE_CHANGED = "com.kaibo.music.play_state";// 播放暂停广播
    String PLAY_STATE_LOADING_CHANGED = "com.kaibo.music.play_state_loading";// 播放loading
    String DURATION_CHANGED = "com.kaibo.music.duration";// 播放时长
    // 播放出错广播
    String TRACK_ERROR = "com.kaibo.music.error";
    // 关闭播放器
    String SHUTDOWN = "com.kaibo.music.shutdown";
    String PLAY_QUEUE_CLEAR = "com.kaibo.music.play_queue_clear"; //清空播放队列
    String PLAY_QUEUE_CHANGE = "com.kaibo.music.play_queue_change"; //播放队列改变
    String META_CHANGED = "com.kaibo.music.metachanged";//状态改变(歌曲替换)
    // 定时广播  用于定时发送播放进度(可以在桌面小部件上更新播放进度)
    String SCHEDULE_CHANGED = "com.kaibo.music.schedule";//定时广播

    /**
     * 通过startCommand的方式控制播放器
     */
    String CMD_TOGGLE_PAUSE = "toggle_pause";//按键播放暂停
    String CMD_PREVIOUS = "previous";//按键上一首
    String CMD_NEXT = "next";//按键下一首
    String CMD_PAUSE = "pause";//按键暂停
    String CMD_PLAY = "playSongList";//按键播放
    String CMD_STOP = "stop";//按键停止
    String CMD_FORWARD = "forward";//快进
    String CMD_REWIND = "reward";//按键停止
    String FROM_MEDIA_BUTTON = "media";//状态改变

    String SERVICE_CMD = "cmd_service";//命令Key
    String CMD_NAME = "name";//命令Key

    /**
     * MusicHandle发送时的  what 值
     */
    int TRACK_WENT_TO_PREV = 1; //上一首
    int TRACK_WENT_TO_NEXT = 2; //下一首
    int RELEASE_WAKELOCK = 3; //释放电源锁
    int TRACK_PLAY_ENDED = 4; //播放完成
    int TRACK_PLAY_ERROR = 5; //播放出错
    int PREPARE_ASYNC_UPDATE = 7; //PrepareAsync装载进程
    int PLAYER_PREPARED = 8; //mediaplayer准备完成
    int AUDIO_FOCUS_CHANGE = 12; //音频焦点改变
    int VOLUME_FADE_DOWN = 13; //音量调低
    int VOLUME_FADE_UP = 14; //音量调高
}
