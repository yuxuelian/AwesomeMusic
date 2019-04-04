package com.kaibo.music.callback

import android.graphics.Bitmap
import com.kaibo.music.player.IPlayerStateCallback
import com.kaibo.music.player.bean.LyricRowBean
import com.kaibo.music.player.bean.SongBean

/**
 * @author kaibo
 * @date 2019/4/4 12:11
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

abstract class BasePlayerCallbackStub : IPlayerStateCallback.Stub() {

    override fun songChange(songBean: SongBean) {
    }

    override fun lyricLoadDone(lyricList: MutableList<LyricRowBean>) {
    }

    override fun isPlayingChange(isPlaying: Boolean) {
    }

    override fun songImageLoadDone(songImage: Bitmap) {
    }

    override fun startPrepare() {
    }

    override fun prepareDone() {
    }
}