package com.kaibo.music.player.manager

import android.util.SparseArray
import com.kaibo.music.utils.SPUtils

/**
 * Created by master on 2018/5/14.
 */

object PlayModeManager {

    /**
     * 播放模式 0：顺序播放，1：单曲循环，2：随机播放
     */
    const val PLAY_MODE_LOOP = 0
    const val PLAY_MODE_REPEAT = 1
    const val PLAY_MODE_RANDOM = 2

    val playingMode = SparseArray<String>(3).apply {
        put(PLAY_MODE_LOOP, "顺序播放")
        put(PLAY_MODE_REPEAT, "单曲循环")
        put(PLAY_MODE_RANDOM, "随机播放")
    }

    /**
     * 更新播放模式
     */
    fun updatePlayMode(): String {
        val currentMode = (getPlayModeId() + 1) % 3
        SPUtils.savePlayMode(currentMode)
        return playingMode[currentMode]
    }

    /**
     * 获取播放模式id
     */
    fun getPlayModeId(): Int {
        return SPUtils.getPlayMode()
    }
}
