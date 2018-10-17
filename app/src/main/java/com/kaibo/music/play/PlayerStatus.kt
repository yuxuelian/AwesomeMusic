package com.kaibo.music.play

import com.kaibo.music.bean.SongBean

/**
 * @author kaibo
 * @date 2018/10/16 17:21
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 * PlayerService向外发送的事件
 */

sealed class PlayerStatus

// 播放源发生改变
data class DataSourceStatus(
        var songBean: SongBean? = null
) : PlayerStatus()

// 播放状态发生改变
data class PlayingStatus(
        var status: Status = Status.STOP
) : PlayerStatus() {
    enum class Status {
        PLAY, STOP, PAUSE, ERROR
    }
}

// 播放时长发生改变
data class DurationStatus(
        var duration: Int = 0
) : PlayerStatus()

// 播放进度发生改变
data class SeekStatus(
        var seek: Int = 0
) : PlayerStatus()

