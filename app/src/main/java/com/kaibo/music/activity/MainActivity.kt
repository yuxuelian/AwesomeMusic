package com.kaibo.music.activity

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.kaibo.core.activity.SuperActivity
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.isDoubleClick
import com.kaibo.music.R
import com.kaibo.music.fragment.RootFragment
import com.kaibo.music.player.manager.PlayManager
import me.yokeyword.fragmentation.anim.FragmentAnimator


/**
 * @author kaibo
 * @createDate 2018/10/9 11:02
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class MainActivity : SuperActivity() {

    private var serviceToken: PlayManager.ServiceToken? = null

    val rootFragment by lazy {
        RootFragment.newInstance()
    }

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
        // 加载根Fragment
        if (findFragment(RootFragment::class.java) == null) {
            loadRootFragment(R.id.rootFragmentContainer, rootFragment)
        }
        setNeedsMenuKey()
    }

    override fun onCreateFragmentAnimator(): FragmentAnimator {
        return FragmentAnimator(R.anim.translation_right_in, R.anim.translation_left_out,
                R.anim.translation_left_in, R.anim.translation_right_out)
    }

    override fun onBackPressedSupport() {
        if (isDoubleClick()) {
            super.onBackPressedSupport()
        } else {
            ToastUtils.showInfo("再点击一次返回键退出")
        }
    }

    override fun onDestroy() {
        PlayManager.unbindFromService(serviceToken)
        super.onDestroy()
    }
}
