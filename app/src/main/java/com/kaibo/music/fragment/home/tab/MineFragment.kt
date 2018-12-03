package com.kaibo.music.fragment.home.tab

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaibo.core.adapter.withItems
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.toast.ToastUtils
import com.kaibo.music.R
import com.kaibo.music.item.mine.PlayListItem
import com.kaibo.music.item.mine.PlayListItemBean
import kotlinx.android.synthetic.main.fragment_mine.*
import q.rorbin.badgeview.Badge
import q.rorbin.badgeview.QBadgeView

/**
 * @author kaibo
 * @createDate 2018/10/9 10:32
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
class MineFragment : BaseFragment() {

    companion object {
        fun newInstance(arguments: Bundle = Bundle()): MineFragment {
            return MineFragment().apply {
                this.arguments = arguments
            }
        }
    }

    override fun getLayoutRes() = R.layout.fragment_mine

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        playListItem.layoutManager = LinearLayoutManager(mActivity)
        playListItem.withItems(listOf(PlayListItemBean(R.drawable.item_music, "本地歌曲", 20),
                PlayListItemBean(R.drawable.item_history, "播放历史", 10),
                PlayListItemBean(R.drawable.item_favorite, "我的收藏", 30),
                PlayListItemBean(R.drawable.item_download, "下载历史", 40)
        ).mapIndexed { index, playListItemBean ->
            PlayListItem(playListItemBean) {
                setOnClickListener {
                    when (index) {
                        0 -> {

                        }
                        1 -> {
                            // 跳转到播放历史界面

                        }
                        2 -> {

                        }
                        3 -> {

                        }
                    }
                }
            }
        })

        QBadgeView(requireContext()).bindTarget(badgeTest).apply {
            setBadgeTextSize(12f, true)
            setBadgePadding(4f, true)
            setOnDragStateChangedListener { dragState, _, _ ->
                if (dragState == Badge.OnDragStateChangedListener.STATE_SUCCEED) {
                    ToastUtils.showSuccess("成功")
                }
            }
            badgeNumber = 999
        }

    }

}