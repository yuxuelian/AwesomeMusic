package com.kaibo.music.play

/**
 * @author kaibo
 * @date 2018/10/16 17:21
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 * PlayerService向外发送的事件
 */

data class PlaySeekBean(
        val seek: Int
)

data class PlayDurationBean(
        val duration: Int
)