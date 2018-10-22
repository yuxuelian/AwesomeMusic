package com.kaibo.music.fragment.singer

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gavin.com.library.StickyDecoration
import com.kaibo.core.adapter.withItems
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.util.*
import com.kaibo.music.R
import com.kaibo.music.activity.SongListActivity
import com.kaibo.music.bean.SingerBean
import com.kaibo.music.bean.SingerListBean
import com.kaibo.music.item.singer.SingerItem
import com.kaibo.music.net.Api

import kotlinx.android.synthetic.main.fragment_singer.*

/**
 * @author kaibo
 * @createDate 2018/10/9 10:31
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class SingerFragment : BaseFragment() {

    override fun getLayoutRes() = R.layout.fragment_singer

    override fun initViewCreated(savedInstanceState: Bundle?) {
        // 获取歌曲数据
        Api.instance.getSingerList().checkResult().toMainThread().`as`(bindLifecycle())
                .subscribe({ singerListBeanList: List<SingerListBean> ->
                    // 初始化SingerList
                    initSingerList(singerListBeanList.flatMap { it.items })
                    initSlideBar(singerListBeanList)
                }) {
                    it.printStackTrace()
                }
    }

    private fun initSlideBar(singerListBeanList: List<SingerListBean>) {
        val titleList: MutableList<String> = ArrayList(singerListBeanList.size)
        val itemsCount: MutableList<Int> = ArrayList(singerListBeanList.size + 1)
        itemsCount.add(0)
        singerListBeanList.forEachIndexed { index, singerContractBean ->
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
        // RecyclerView滑动的时候联动SlideBar
        singerList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // 找到第一个可见的Item所在的position
                val visiblePosition = layoutManager.findFirstVisibleItemPosition()
                // 根据position值寻找position是处于哪个区间
                itemsCount.forEachIndexed { index, start ->
                    // index 最大取值只能取值到  0  size-2  因为 itemsCount 大小是 letters.size+1
                    if (index < itemsCount.size - 1) {
                        // 比对区间
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
        singerList.withItems(singerBeanList.map { singerBean: SingerBean ->
            SingerItem(singerBean) {
                setOnClickListener {
                    activity?.animInStartActivity<SongListActivity>("singermid" to singerBean.id)
                }
            }
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