package com.kaibo.music.fragment.recommend

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.kaibo.core.adapter.withItems
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.util.checkResult
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.bean.BannerDataBean
import com.kaibo.music.bean.RecommendBean
import com.kaibo.music.item.recommend.BannerItem
import com.kaibo.music.item.recommend.SongListItem
import com.kaibo.music.item.recommend.SongTitleItem
import com.kaibo.music.net.Api
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.fragment_recommend_layout.*

/**
 * @author kaibo
 * @date 2018/10/9 10:31
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class RecommendFragment : BaseFragment() {

    private val bannerItem by lazy {
        BannerItem()
    }

    override fun getLayoutRes() = R.layout.fragment_recommend_layout

    override fun initViewCreated(savedInstanceState: Bundle?) {
        val songTitleItem = SongTitleItem()
        Observable
                .zip(
                        Api.instance.getBannerList().checkResult(),
                        Api.instance.getRecommendList().checkResult(),
                        BiFunction<List<BannerDataBean>, List<RecommendBean>, Pair<List<BannerDataBean>, List<RecommendBean>>> { t1, t2 ->
                            Pair(t1, t2)
                        })
                .toMainThread()
                .`as`(bindLifecycle())
                .subscribe({ netRes ->
                    recommendRecyclerView.layoutManager = LinearLayoutManager(context)
                    recommendRecyclerView.withItems {
                        // 设置轮播图数据
                        bannerItem.setData(netRes.first)
                        // 列表轮播图
                        add(bannerItem)
                        // 列表标题
                        add(songTitleItem)
                        // 列表数据
                        addAll(netRes.second.map {
                            SongListItem(it)
                        })
                    }
                }) {
                    it.printStackTrace()
                }
    }

    override fun onResume() {
        super.onResume()
        bannerItem.startAutoPlay()
    }

    override fun onPause() {
        bannerItem.stopAutoPlay()
        super.onPause()
    }

}