package com.kaibo.music.activity.base

import android.content.ServiceConnection
import android.os.Bundle
import com.kaibo.core.activity.CoreActivity
import com.kaibo.music.player.PlayManager

/**
 * @author kaibo
 * @date 2018/10/15 15:41
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

abstract class BaseActivity : CoreActivity(), ServiceConnection {


    private var serviceToken: PlayManager.ServiceToken? = null

    override fun initOnCreate(savedInstanceState: Bundle?) {
        serviceToken = PlayManager.bindToService(this, this)
    }

    override fun onDestroy() {
        PlayManager.unbindFromService(serviceToken)
        super.onDestroy()
    }

    /**
     * 带动画返回
     */
    override fun onBackPressed() {
        animOutFinish()
    }

}