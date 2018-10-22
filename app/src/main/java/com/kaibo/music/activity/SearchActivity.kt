package com.kaibo.music.activity

import android.os.Bundle
import android.view.View
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.kaibo.core.adapter.withItems
import com.kaibo.core.util.checkResult
import com.kaibo.core.util.statusBarHeight
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.activity.base.BaseActivity
import com.kaibo.music.bean.HotSearchBean
import com.kaibo.music.item.search.HotSearchItem
import com.kaibo.music.net.Api
import kotlinx.android.synthetic.main.activity_search.*
import java.util.concurrent.TimeUnit

/**
 * @author kaibo
 * @createDate 2018/10/15 14:08
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class SearchActivity : BaseActivity() {

    override fun getLayoutRes() = R.layout.activity_search

    override fun initOnCreate(savedInstanceState: Bundle?) {
        super.initOnCreate(savedInstanceState)
        appBarLayout.setPadding(0, statusBarHeight, 0, 0)
        setSupportActionBar(searchToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        Api.instance.getHotSearch().checkResult()
                .toMainThread().`as`(bindLifecycle()).subscribe({
                    initHotSearchList(it)
                }) {
                    it.printStackTrace()
                }

        // 获取搜索框中的内容
        searchContentInput.textChanges()
                .debounce(200, TimeUnit.MILLISECONDS).toMainThread()
                .`as`(bindLifecycle()).subscribe {
                    if (it.isNotEmpty()) {
                        clearInput.visibility = View.VISIBLE
                        clearInput.isEnabled = true
                    } else {
                        clearInput.visibility = View.INVISIBLE
                        clearInput.isEnabled = false
                    }
                }

        // 点击清除按钮
        clearInput.clicks().`as`(bindLifecycle()).subscribe {
            searchContentInput.setText("")
        }

        // 点击返回键
        backBtn.clicks().`as`(bindLifecycle()).subscribe {
            animOutFinish()
        }
    }

    private fun initHotSearchList(hotSearchBeanList: List<HotSearchBean>) {
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.justifyContent = JustifyContent.SPACE_BETWEEN
        hot_search_list.layoutManager = layoutManager
        hot_search_list.isNestedScrollingEnabled = false
        hot_search_list.withItems(hotSearchBeanList.asSequence()
                .filterIndexed { index, _ -> index < 10 }
                .map { hotSearchBean: HotSearchBean ->
                    HotSearchItem(hotSearchBean) {
                        initItem(hotSearchBean)
                    }
                }
                .toList())
    }

    private fun View.initItem(hotSearchBean: HotSearchBean) {
        setOnClickListener {
            searchContentInput.setText(hotSearchBean.searchKey)
            // TODO 执行搜索操作

        }
    }
}