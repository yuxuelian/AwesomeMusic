package com.kaibo.music.activity

import android.os.Bundle
import com.kaibo.core.activity.SuperActivity
import com.kaibo.music.player.PlayerController

/**
 * @author kaibo
 * @date 2019/4/4 10:56
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

abstract class BaseMusicActivity : SuperActivity() {
    private var bindToken: PlayerController.BindToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindToken = PlayerController.bindService(this)
    }


    override fun onDestroy() {
        PlayerController.unbindService(bindToken)
        super.onDestroy()
    }

}