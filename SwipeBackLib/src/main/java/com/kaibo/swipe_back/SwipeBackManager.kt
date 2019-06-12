package com.kaibo.swipe_back

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.*

/**
 * @author: kaibo
 * @date: 2019/6/5 10:28
 * @GitHub: https://github.com/yuxuelian
 * @qq: 568966289
 * @description:
 */


object SwipeBackManager {

    private val mActivityStack: Stack<Activity> = Stack()

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(MyActivityLifecycleCallbacks())
    }

    class MyActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            mActivityStack.add(activity)
            if (activity is SwipeBackEnable && getPenultimateActivity(activity) != null) {
                SwipeBackLayout(activity)
            }
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            mActivityStack.remove(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
        }
    }

    /**
     * 获取倒数第二个 Activity
     *
     * @return
     */
    internal fun getPenultimateActivity(currentActivity: Activity): Activity? {
        var activity: Activity? = null
        try {
            if (mActivityStack.size > 1) {
                activity = mActivityStack[mActivityStack.size - 2]
                if (currentActivity == activity) {
                    val index = mActivityStack.indexOf(currentActivity)
                    if (index > 0) {
                        // 处理内存泄漏或最后一个 Activity 正在 finishing 的情况
                        activity = mActivityStack[index - 1]
                    } else if (mActivityStack.size == 2) {
                        // 处理屏幕旋转后 mActivityStack 中顺序错乱
                        activity = mActivityStack.lastElement()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return activity
    }

}