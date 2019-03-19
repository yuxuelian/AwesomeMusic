package com.kaibo.music.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;

import com.kaibo.core.AppContext;
import com.kaibo.music.utils.rom.FloatUtil;
import com.kaibo.core.BaseApplication;

import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * android系统工具类
 * 主要功能判断android系统的版本、判断后台Service是否运行
 * 作者：yonglong on 2016/8/12 16:45
 * 邮箱：643872807@qq.com
 * 版本：2.5
 */
public class SystemUtils {

    //判断是否是android 8.0
    public static boolean isO() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    //判断是否是android 6.0
    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    //判断是否是android 5.0
    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    //判断是否是android 4.0
    public static boolean isKITKAT() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * 判断是否打开“悬浮窗权限”
     *
     * @return
     */
    public static boolean isOpenFloatWindow() {
        return FloatUtil.INSTANCE.checkPermission(AppContext.INSTANCE);
    }

    /**
     * 检查申请打开“悬浮窗权限”
     *
     * @return
     */
    public static void applySystemWindow() {
        FloatUtil.INSTANCE.applyOrShowFloatWindow(AppContext.INSTANCE);
    }

    /**
     * 判断是否打开“有权查看使用权限的应用”这个选项
     *
     * @return
     */
    public static boolean isOpenUsageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && isNoOptions()) {
            return isNoSwitch();
        } else {
            return true;
        }
    }

    /**
     * 判断当前设备中有没有“有权查看使用权限的应用”这个选项
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static boolean isNoOptions() {
        PackageManager packageManager = AppContext.INSTANCE.getPackageManager();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * 判断调用该设备中“有权查看使用权限的应用”这个选项的APP有没有打开
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private static boolean isNoSwitch() {
        long dujinyang = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) AppContext.INSTANCE.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = null;
        if (usageStatsManager != null) {
            queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, dujinyang);
        }
        return queryUsageStats != null && !queryUsageStats.isEmpty();
    }

}
