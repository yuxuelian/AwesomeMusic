package com.kaibo.music.activity

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.kaibo.core.activity.SuperActivity
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.isDoubleClick
import com.kaibo.music.R
import com.kaibo.music.fragment.home.HomeFragment
import com.kaibo.music.fragment.home.MiniPlayerFragment
import com.kaibo.music.player.manager.PlayManager


/**
 * @author kaibo
 * @createDate 2018/10/9 11:02
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class MainActivity : SuperActivity() {

    private var serviceToken: PlayManager.ServiceToken? = null

    private fun setNeedsMenuKey() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams::class.java.getField("FLAG_NEEDS_MENU_KEY").getInt(null))
        } else {
            val setNeedsMenuKey = Window::class.java.getDeclaredMethod("setNeedsMenuKey", Int::class.javaPrimitiveType)
            val value = WindowManager.LayoutParams::class.java.getField("NEEDS_MENU_SET_TRUE").getInt(null)
            setNeedsMenuKey.isAccessible = true
            setNeedsMenuKey.invoke(window, value)
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        super.initOnCreate(savedInstanceState)
        serviceToken = PlayManager.bindToService(this)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.homeContainer, HomeFragment())
                .replace(R.id.miniPlayerContainer, MiniPlayerFragment())
                .commitAllowingStateLoss()

        setNeedsMenuKey()
    }

    override fun onBackPressed() {
        if (isDoubleClick()) {
            super.onBackPressed()
        } else {
            ToastUtils.showInfo("再点击一次返回键退出")
        }
    }

    override fun onDestroy() {
        PlayManager.unbindFromService(serviceToken)
        super.onDestroy()
    }
}
