package com.kaibo.music.fragment.singer

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gavin.com.library.StickyDecoration
import com.kaibo.core.adapter.withItems
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.util.checkResult
import com.kaibo.core.util.dip
import com.kaibo.core.util.sp
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.bean.SingerBean
import com.kaibo.music.bean.SingerContractBean
import com.kaibo.music.item.singer.SingerItem
import com.kaibo.music.net.Api
import com.kaibo.music.weight.overscroll.OverScrollDecoratorHelper
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fragment_singer_layout.*

/**
 * @author kaibo
 * @date 2018/10/9 10:31
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class SingerFragment : BaseFragment() {

    override fun getLayoutRes() = R.layout.fragment_singer_layout

    override fun initViewCreated(savedInstanceState: Bundle?) {
        // 获取歌曲数据
        Api.instance.getSingerList().checkResult().toMainThread().`as`(bindLifecycle())
                .subscribe({ singerContractBeanList: List<SingerContractBean> ->
                    // 初始化SingerList
                    initSingerList(singerContractBeanList.flatMap { it.items })
                    initSlideBar(singerContractBeanList)
                }) {
                    it.printStackTrace()
                }
    }

    private fun initSlideBar(singerContractBeanList: List<SingerContractBean>) {
        val titleList: MutableList<String> = ArrayList(singerContractBeanList.size)
        val itemsCount: MutableList<Int> = ArrayList(singerContractBeanList.size + 1)
        itemsCount.add(0)
        singerContractBeanList.forEachIndexed { index, singerContractBean ->
            titleList.add(singerContractBean.title)
            itemsCount.add(itemsCount[index] + singerContractBean.items.size)
        }
        // 设置右侧的字母索引表
        slideBar.letters = titleList
        val layoutManager = (singerList.layoutManager as LinearLayoutManager)
        slideBar.letterChangeListener = { index, letter ->
            // 移动到指定的位置
            layoutManager.scrollToPositionWithOffset(itemsCount[index], 0)
        }
        singerList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visiblePosition = layoutManager.findFirstVisibleItemPosition()
                itemsCount.forEachIndexed { index, start ->
                    if (index < itemsCount.size - 1) {
                        if (visiblePosition in start until itemsCount[index + 1]) {
                            // 移动slideBar的Index
                            slideBar.index = index
                        }
                    }
                }
            }
        })
    }

    /**
     * 初始化歌手列表
     */
    private fun initSingerList(singerBeanList: List<SingerBean>) {
        singerList.layoutManager = LinearLayoutManager(context)
        // 设置纵向回弹
        OverScrollDecoratorHelper.setUpOverScroll(singerList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
        singerList.withItems(singerBeanList.map {
            SingerItem(it)
        })
        val decoration = StickyDecoration.Builder
                .init { position ->
                    singerBeanList[position].title
                }
//                .setDivideHeight(1)
//                .setDivideColor(ContextCompat.getColor(context!!, R.color.color_333))
                .setGroupBackground(ContextCompat.getColor(context!!, R.color.color_333))
                .setGroupHeight(dip(30))
                .setGroupTextSize(sp(12))
                .setGroupTextColor(ContextCompat.getColor(context!!, R.color.colorThemeText))
                .setTextSideMargin(dip(20))
                .build()
        singerList.addItemDecoration(decoration)
    }
}