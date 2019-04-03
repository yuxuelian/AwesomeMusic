package com.kaibo.music.player.player.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * @author kaibo
 * @date 2019/4/3 12:09
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class HeadsetPlugInReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra("state")) {
            //通过判断 "state" 来知道状态

        }
    }
}