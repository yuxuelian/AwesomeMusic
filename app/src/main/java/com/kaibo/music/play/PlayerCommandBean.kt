package com.kaibo.music.play

/**
 * @author kaibo
 * @date 2018/10/16 17:22
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

data class SeekCommand(
        val seek: Int
)

data class PlayCommand(
        // 播放源
        var dataSource: String,
        // 是否进行播放
        var isPlay: Boolean
)

