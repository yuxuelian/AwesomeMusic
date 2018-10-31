package com.kaibo.music.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.animInStartActivity
import com.kaibo.core.util.isDoubleClick
import com.kaibo.core.util.statusBarHeight
import com.kaibo.music.R
import com.kaibo.music.activity.base.BaseMiniPlayerActivity
import com.kaibo.music.fragment.MineFragment
import com.kaibo.music.fragment.RankFragment
import com.kaibo.music.fragment.RecommendFragment
import com.kaibo.music.fragment.SingerFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_home_title.*

/**
 * @author kaibo
 * @createDate 2018/10/9 11:02
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class MainActivity : BaseMiniPlayerActivity() {

    override val mineContainer = R.id.bottomControllerContainer

    override fun getLayoutRes(): Int {
        return R.layout.activity_main
    }

    override fun initOnCreate(savedInstanceState: Bundle?) {
        super.initOnCreate(savedInstanceState)
        appBarLayout.setPadding(0, statusBarHeight, 0, 0)
        initViewPager()

        // 点击右上角的搜索按钮
        search.clicks().`as`(bindLifecycle()).subscribe {
            animInStartActivity<SearchActivity>()
        }
    }

    private fun initViewPager() {
        val pageTitles: Array<String> = resources.getStringArray(R.array.home_tab_array)
        val fragments = listOf<Fragment>(RecommendFragment(), SingerFragment(), RankFragment(), MineFragment())
        mainPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int) = fragments[position]

            override fun getCount() = fragments.size

            override fun getPageTitle(position: Int) = pageTitles[position]
        }
        mainPager.offscreenPageLimit = 4
        tabLayout.setupWithViewPager(mainPager)
    }

    override fun onBackPressed() {
        // 返回桌面
//        val intent = Intent(Intent.ACTION_MAIN)
//        intent.addCategory(Intent.CATEGORY_HOME)
//        startActivity(intent)
        if (isDoubleClick()) {
            super.onBackPressed()
        } else {
            ToastUtils.showInfo("再按一次返回键退出")
        }
    }

}
