package com.kaibo.music.fragment.home.tab

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaibo.core.adapter.withItems
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.util.checkResult
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.bean.RankBean
import com.kaibo.music.fragment.home.HomeFragment
import com.kaibo.music.fragment.songlist.SongListFragment
import com.kaibo.music.item.rank.RankItem
import com.kaibo.music.net.Api

import kotlinx.android.synthetic.main.fragment_rank.*

/**
 * @author kaibo
 * @createDate 2018/10/9 10:32
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class RankFragment : BaseFragment() {

    companion object {
        fun newInstance(arguments: Bundle = Bundle()): RankFragment {
            return RankFragment().apply {
                this.arguments = arguments
            }
        }
    }

    override fun getLayoutRes() = R.layout.fragment_rank

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
        rankList.withItems(rankBeanList.map { rankBean: RankBean ->
            RankItem(rankBean) {
                setOnClickListener {
                    (parentFragment as HomeFragment).start(SongListFragment.newInstance(Bundle().apply {
                        putInt("topid", rankBean.id)
                    }))
                }
            }
        })
    }

}