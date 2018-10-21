package com.kaibo.music.activity.base

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.CallSuper
import com.kaibo.core.activity.SuperActivity
import com.kaibo.music.player.MusicPlayerService
import com.kaibo.music.player.PlayManager
import org.jetbrains.anko.startService

/**
 * @author kaibo
 * @date 2018/10/15 15:41
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

abstract class BaseActivity : SuperActivity(), ServiceConnection {

    private var serviceToken: PlayManager.ServiceToken? = null

    @CallSuper
    override fun initOnCreate(savedInstanceState: Bundle?) {
        serviceToken = PlayManager.bindToService(this, this)
    }

    override fun onServiceDisconnected(name: ComponentName) {
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
    }

    override fun onDestroy() {
        PlayManager.unbindFromService(serviceToken)
        super.onDestroy()
    }

}