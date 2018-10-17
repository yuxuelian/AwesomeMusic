package com.kaibo.music.fragment.recommend

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaibo.core.adapter.withItems
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.util.animInStartActivity
import com.kaibo.core.util.checkResult
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.activity.SongListActivity
import com.kaibo.music.bean.BannerDataBean
import com.kaibo.music.bean.RecommendBean
import com.kaibo.music.item.recommend.BannerItem
import com.kaibo.music.item.recommend.RecommendItem
import com.kaibo.music.item.recommend.SongTitleItem
import com.kaibo.music.net.Api
import com.kaibo.music.weight.AcFunOverView

import com.liaoinstan.springview.widget.SpringView
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
        springview.setGive(SpringView.Give.NONE)
        springview.setListener(object : SpringView.OnFreshListener {
            override fun onRefresh() {}

            override fun onLoadmore() {}
        })
        springview.header = AcFunOverView(context)
        springview.footer = AcFunOverView(context)
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
                    initRecommendList(netRes)
                }) {
                    it.printStackTrace()
                }
    }

    private fun initRecommendList(netRes: Pair<List<BannerDataBean>, List<RecommendBean>>) {
        recommendRecyclerView.layoutManager = LinearLayoutManager(context)
        // 设置纵向回弹
        recommendRecyclerView.withItems {
            // 设置轮播图数据
            bannerItem.setData(netRes.first)
            // 列表轮播图
            add(bannerItem)
            // 列表标题
            add(SongTitleItem())
            // 列表数据
            addAll(netRes.second.map { recommendBean: RecommendBean ->
                RecommendItem(recommendBean) {
                    setOnClickListener {
                        //                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                            val intent = Intent(activity, SongListActivity::class.java)
//                            intent.putExtra("disstid", recommendBean.disstid)
//                            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
//                                    activity,
//                                    it.recommendImg,
//                                    getString(R.string.transition_recommend_logo)).toBundle())
//                        } else {
//                            activity?.animInStartActivity<SongListActivity>("disstid" to recommendBean.disstid)
//                        }
                        activity?.animInStartActivity<SongListActivity>("disstid" to recommendBean.disstid)
                    }
                }
            })
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