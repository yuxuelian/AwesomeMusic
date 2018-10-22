package com.kaibo.music.player.manager

import androidx.annotation.IntDef
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

    private val playingMode = arrayOf("顺序播放", "单曲循环", "随机播放")

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @IntDef(value = [PLAY_MODE_LOOP, PLAY_MODE_REPEAT, PLAY_MODE_RANDOM])
    annotation class ModeTypeGuide

    /**
     * 更新播放模式
     */
    fun updatePlayMode(@ModeTypeGuide mode: Int): String {
        SPUtils.savePlayMode(mode)
        // TODO 总线发送播放模式
        return playingMode[mode]
    }

    /**
     * 获取播放模式id
     */
    fun getPlayModeId(): Int {
        return SPUtils.getPlayMode()
    }

    /**
     * 获取播放模式
     */
    fun getPlayMode(): String {
        return playingMode[getPlayModeId()]
    }
}
