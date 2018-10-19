package com.kaibo.music.player;

import android.content.Context;
import android.os.Handler;
import android.view.WindowManager;

import com.kaibo.core.BaseApplication;
import com.kaibo.music.bean.SongBean;

public class FloatLyricViewManager {

    private static final String TAG = "FloatLyricViewManager";
    private static WindowManager.LayoutParams mFloatLyricViewParams;
    private static WindowManager mWindowManager;
    private Handler handler = new Handler();
    private String mSongName;
    private static boolean isFirstSettingLyric; //第一次设置歌词

    /**
     * 歌词信息
     */
    public static String lyricInfo;

    /**
     * 定时器，定时进行检测当前应该创建还是移除悬浮窗。
     */
    private Context mContext;

    FloatLyricViewManager(Context context) {
        mContext = context;
    }

    /**
     * ---------------------------歌词View更新-----------------------------
     */

    public void updatePlayStatus(boolean isPlaying) {
//        if (mFloatLyricView != null) {
//            mFloatLyricView.setPlayStatus(isPlaying);
//        }
    }

    /**
     * 加载歌词
     */
    public void loadLyric(SongBean mPlayingMusic) {
        resetLyric(BaseApplication.Companion.getBaseApplication().getString(R.string.lyric_loading));
        if (mPlayingMusic != null) {
            mSongName = mPlayingMusic.getSongname();
            // TODO 获取歌词
//            @Override
//            public void success(String result) {
//                updateLyric(result);
//            }
//
//            @Override
//            public void error(String msg) {
//                updateLyric("");
//                Logger.e("LoadLyric", msg);
//            }
        } else {
            updateLyric("");
        }
    }

    /**
     * 保存歌词
     *
     * @param info 歌词
     */
    public static void saveLyricInfo(String name, String artist, String info) {
        lyricInfo = info;
//        MusicApiServiceImpl.INSTANCE.saveLyricInfo(name, artist, info);
//        setLyric(lyricInfo);
//        for (int i = 0; i < lyricViews.size(); i++) {
//            lyricViews.get(i).setLyricContent(info);
//        }
    }

    private void resetLyric(String info) {
        lyricInfo = info;
        setLyric(lyricInfo);
//        for (int i = 0; i < lyricViews.size(); i++) {
//            lyricViews.get(i).reset(info);
//        }
    }

    /**
     * 更新歌词
     *
     * @param info 歌词
     */
    private void updateLyric(String info) {
        lyricInfo = info;
        setLyric(lyricInfo);
//        for (int i = 0; i < lyricViews.size(); i++) {
//            lyricViews.get(i).setLyricContent(info);
//        }
    }


    /**
     * 设置歌词
     *
     * @param lyricInfo 歌词信息
     */
    public static void setLyric(String lyricInfo) {
//        mLyricInfo = LyricParseUtils.setLyricResource(lyricInfo);
//        isFirstSettingLyric = true;
    }


    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     *
     * @param context 必须为应用程序的Context.
     */
    private void createFloatLyricView(Context context) {
//        try {
//            WindowManager windowManager = getWindowManager();
//            Point size = new Point();
//            //获取屏幕宽高
//            windowManager.getDefaultDisplay().getSize(size);
//            int screenWidth = size.x;
//            int screenHeight = size.y;
//            if (mFloatLyricView == null) {
//                mFloatLyricView = new FloatLyricView(context);
//                if (mFloatLyricViewParams == null) {
//                    mFloatLyricViewParams = new WindowManager.LayoutParams();
//                    mFloatLyricViewParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        mFloatLyricViewParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//                    } else {
//                        mFloatLyricViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//                    }
//
//                    mFloatLyricViewParams.format = PixelFormat.RGBA_8888;
//                    mFloatLyricViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//                    mFloatLyricViewParams.gravity = Gravity.START | Gravity.TOP;
//                    mFloatLyricViewParams.width = mFloatLyricView.viewWidth;
//                    mFloatLyricViewParams.height = mFloatLyricView.viewHeight;
//                    mFloatLyricViewParams.x = screenWidth;
//                    mFloatLyricViewParams.y = screenHeight / 2;
//                }
//                mFloatLyricView.setParams(mFloatLyricViewParams);
//                windowManager.addView(mFloatLyricView, mFloatLyricViewParams);
//                setLyric(lyricInfo);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     *
     * @param context 必须为应用程序的Context.
     */
    public void removeFloatLyricView(Context context) {
//        try {
//            if (mFloatLyricView != null) {
//                WindowManager windowManager = getWindowManager();
//                windowManager.removeView(mFloatLyricView);
//                mFloatLyricView = null;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 更新小悬浮窗的TextView上的数据，显示内存使用的百分比。
     */
    public void updateLyric(long positon, long duration) {
        // 当前界面不是本应用界面，且没有悬浮窗显示，则创建悬浮窗。
//        if (!isHome() && !isWindowShowing()) {
//            handler.post(() -> createFloatLyricView(mContext));
//        } else if (isHome() && isWindowShowing()) {
//            handler.post(() -> removeFloatLyricView(mContext));
//        } else if (isWindowShowing()) {
//            handler.post(() -> {
//                if (mFloatLyricView != null) {
//                    if (isFirstSettingLyric) {
//                        mFloatLyricView.mTitle.setText(mSongName);
//                        mFloatLyricView.mLyricText.setLyricInfo(mLyricInfo);
//                        isFirstSettingLyric = false;
//                    }
//                    mFloatLyricView.mLyricText.setCurrentTimeMillis(positon);
//                    mFloatLyricView.mLyricText.setDurationMillis(duration);
//                }
//            });
//        }
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager() {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) BaseApplication.Companion.getBaseApplication().getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

//    private String getProcess() throws Exception {
//        if (Build.VERSION.SDK_INT >= 21) {
//            return getProcessNew();
//        } else {
//            return getProcessOld();
//        }
//    }
//
//    private String topPackageName = null;
//
//    //API 21 and above
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private String getProcessNew() throws Exception {
//        UsageStatsManager mUsageStatsManager = (UsageStatsManager) BaseApplication.Companion.getBaseApplication()().getSystemService(Context.USAGE_STATS_SERVICE);
//        long time = System.currentTimeMillis();
//        // We get usage stats for the last 10 seconds
//        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 5, time);
//        // Sort the stats by the last time used
//
//        if (stats != null) {
//            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
//            for (UsageStats usageStats : stats) {
//                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
//            }
//            if (!mySortedMap.isEmpty()) {
//                topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
//            }
//        }
//        return topPackageName;
//    }
//
//    //API below 21
//    @SuppressWarnings("deprecation")
//    private String getProcessOld() throws Exception {
//        String topPackageName = null;
//        ActivityManager activity = (ActivityManager) BaseApplication.Companion.getBaseApplication()().getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> runningTask = activity.getRunningTasks(1);
//        if (runningTask != null) {
//            ActivityManager.RunningTaskInfo taskTop = runningTask.get(0);
//            ComponentName componentTop = taskTop.topActivity;
//            topPackageName = componentTop.getPackageName();
//        }
//        return topPackageName;
//    }
}
