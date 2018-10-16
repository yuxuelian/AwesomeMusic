package com.kaibo.music.play

import android.os.Binder

/**
 * @author kaibo
 * @date 2018/10/16 17:55
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class PlayerStatusBinder(val service: PlayerService) : Binder() {
    val duration
        get() = service.mediaPlayer.duration

    val seek
        get() = service.mediaPlayer.currentPosition
}