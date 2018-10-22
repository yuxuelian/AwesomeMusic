package com.kaibo.music.player.receiver;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;

import com.kaibo.music.player.Constants;
import com.kaibo.music.player.service.MusicPlayerService;

import java.util.List;

/**
 * 需要在manifest文件中注册
 * Used to control headset playback.
 * Single press: pause/resume
 * Double press: next track
 * Triple press: previous track
 * Long press: voice search
 */

public class MediaButtonIntentReceiver extends BroadcastReceiver {

    private static final String TAG = "ButtonIntentReceiver";

    private static final int MSG_LONGPRESS_TIMEOUT = 1;
    private static final int MSG_HEADSET_DOUBLE_CLICK_TIMEOUT = 2;

    private static final int LONG_PRESS_DELAY = 1000;
    private static final int DOUBLE_CLICK = 800;

    private static WakeLock mWakeLock = null;
    private static int mClickCounter = 0;
    private static long mLastClickTime = 0;
    private static boolean mDown = false;
    private static boolean mLaunched = false;

    @SuppressLint("HandlerLeak")
    private static Handler mHandler = new Handler() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_LONGPRESS_TIMEOUT:
                    if (!mLaunched) {
                        final Context context = (Context) msg.obj;
                        final Intent i = new Intent();
                        i.putExtra("autoshuffle", "true");
                        i.setClassName(context, "com.kaibo.music.activity.MainActivity");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(i);
                        mLaunched = true;
                    }
                    break;
                case MSG_HEADSET_DOUBLE_CLICK_TIMEOUT:
                    //双击时间阈值内
                    final int clickCount = msg.arg1;
                    final String command;
                    switch (clickCount) {
                        case 1:
                            command = Constants.CMD_TOGGLE_PAUSE;
                            break;
                        case 2:
                            command = Constants.CMD_NEXT;
                            break;
                        case 3:
                            command = Constants.CMD_PREVIOUS;
                            break;
                        default:
                            command = null;
                            break;
                    }

                    if (command != null) {
                        final Context context1 = (Context) msg.obj;
                        startService(context1, command);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 启动musicservice,并拥有wake_lock权限
     *
     * @param context
     * @param command
     */
    private static void startService(Context context, String command) {
        final Intent i = new Intent(context, MusicPlayerService.class);
        i.setAction(Constants.SERVICE_CMD);
        i.putExtra(Constants.CMD_NAME, command);
        i.putExtra(Constants.FROM_MEDIA_BUTTON, true);
        context.startService(i);
    }

    @SuppressLint("InvalidWakeLockTag")
    private static void acquireWakeLockAndSendMessage(Context context, Message msg, long delay) {
        if (mWakeLock == null) {
            Context appContext = context.getApplicationContext();
            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Listener headset button");
            //设置无论请求多少次vakelock,都只需一次释放
            mWakeLock.setReferenceCounted(false);
        }
        // Make sure we don't indefinitely hold the wake lock under any circumstances
        //防止无期限hold住wakelock
        mWakeLock.acquire(10000);
        mHandler.sendMessageDelayed(msg, delay);
    }

    /**
     * 如果handler的消息队列中没有待处理消息,就释放receiver hold住的wakelog
     */
    private static void releaseWakeLockIfHandlerIdle() {
        if (mHandler.hasMessages(MSG_LONGPRESS_TIMEOUT) || mHandler.hasMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)) {
            return;
        }
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String intentAction = intent.getAction();
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
            //当耳机拔出时暂停播放
            if (isMusicServiceRunning(context)) {
                Intent i = new Intent(context, MusicPlayerService.class);
                i.setAction(Constants.SERVICE_CMD);
                i.putExtra(Constants.CMD_NAME, Constants.CMD_PAUSE);
                context.startService(i);
            }
        } else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            //耳机按钮事件
            final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            final int keycode = event.getKeyCode();
            final int action = event.getAction();
            final long eventtime = event.getEventTime();
            String command = null;
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    command = Constants.CMD_STOP;
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    command = Constants.CMD_TOGGLE_PAUSE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    command = Constants.CMD_NEXT;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    command = Constants.CMD_PREVIOUS;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    command = Constants.CMD_PAUSE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    command = Constants.CMD_PLAY;
                    break;
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    command = Constants.CMD_FORWARD;
                    break;
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    command = Constants.CMD_REWIND;
                    break;
                default:
                    break;
            }
            if (command != null) {
                if (action == KeyEvent.ACTION_DOWN) {
                    if (mDown) {
                        if (Constants.CMD_TOGGLE_PAUSE.equals(command) || Constants.CMD_PLAY.equals(command)) {
                            if (mLastClickTime != 0
                                    && eventtime - mLastClickTime > LONG_PRESS_DELAY) {
                                acquireWakeLockAndSendMessage(context,
                                        mHandler.obtainMessage(MSG_LONGPRESS_TIMEOUT, context), 0);
                            }
                        }
                    } else if (event.getRepeatCount() == 0) {
                        if (keycode == KeyEvent.KEYCODE_HEADSETHOOK) {
                            if (eventtime - mLastClickTime >= DOUBLE_CLICK) {
                                mClickCounter = 0;
                            }
                            mClickCounter++;
                            mHandler.removeMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT);
                            Message msg = mHandler.obtainMessage(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT, mClickCounter, 0, context);
                            long delay = mClickCounter < 3 ? DOUBLE_CLICK : 0;
                            if (mClickCounter >= 3) {
                                mClickCounter = 0;
                            }
                            mLastClickTime = eventtime;
                            acquireWakeLockAndSendMessage(context, msg, delay);
                        } else {
                            startService(context, command);
                        }
                        mLaunched = false;
                        mDown = true;
                    }
                } else {
                    mHandler.removeMessages(MSG_LONGPRESS_TIMEOUT);
                    mDown = false;
                }
                if (isOrderedBroadcast()) {
                    abortBroadcast();
                }
                releaseWakeLockIfHandlerIdle();
            }
        }
    }

    private boolean isMusicServiceRunning(Context context) {
        boolean isServiceRuning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final int maxServciesNum = 100;
        List<ActivityManager.RunningServiceInfo> list = am.getRunningServices(maxServciesNum);
        for (ActivityManager.RunningServiceInfo info : list) {
            if (MusicPlayerService.class.getName().equals(info.service.getClassName())) {
                isServiceRuning = true;
                break;
            }
        }
        return isServiceRuning;
    }
}
