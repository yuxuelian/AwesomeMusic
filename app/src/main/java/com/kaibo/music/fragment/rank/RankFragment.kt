package com.kaibo.music.fragment.rank

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaibo.core.adapter.withItems
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.util.checkResult
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.bean.RankBean
import com.kaibo.music.item.rank.RankItem
import com.kaibo.music.net.Api
import com.kaibo.music.weight.overscroll.OverScrollDecoratorHelper
import kotlinx.android.synthetic.main.fragment_rank_layout.*

/**
 * @author kaibo
 * @date 2018/10/9 10:32
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class RankFragment : BaseFragment() {

    override fun getLayoutRes() = R.layout.fragment_rank_layout

    override fun initViewCreated(savedInstanceState: Bundle?) {
        Api.instance.getRankList().checkResult()
                .toMainThread().`as`(bindLifecycle())
                .subscribe({
                    initRankList(it)
                }) {
                    it.printStackTrace()
                }
    }

    private fun initRankList(rankBeanList: List<RankBean>) {
        rankList.layoutManager = LinearLayoutManager(context)
        OverScrollDecoratorHelper.setUpOverScroll(rankList, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
        rankList.withItems(rankBeanList.map {
            RankItem(it)
        })
    }

}