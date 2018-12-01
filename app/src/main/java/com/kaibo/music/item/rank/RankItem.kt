package com.kaibo.music.item.rank

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kaibo.core.adapter.Item
import com.kaibo.core.adapter.ItemController
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.toast.ToastUtils
import com.kaibo.core.util.inflate
import com.kaibo.music.R
import com.kaibo.music.bean.RankBean
import kotlinx.android.synthetic.main.item_rank.view.*
import q.rorbin.badgeview.Badge
import q.rorbin.badgeview.QBadgeView

/**
 * @author 56896
 * @createDate 2018/10/14 23:06
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class RankItem(val rankBean: RankBean, val init: View.() -> Unit = {}) : Item {

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val itemView: View = parent.inflate(R.layout.item_rank)
            return ViewHolder(itemView, itemView.rankItem, itemView.picImg, listOf(itemView.songOne, itemView.songTwo, itemView.songThree))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as RankItem
            GlideApp.with(holder.picImg)
                    .load(item.rankBean.picUrl)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo).into(holder.picImg)
            val songList = item.rankBean.songBeanList
            if (songList.size == 3) {
                songList.forEachIndexed { index, song ->
                    holder.songList[index].text = "${index + 1}.${song.songname}-${song.singername}"
                }
            }
            holder.badge.badgeNumber = holder.adapterPosition + 1
            holder.itemView.apply(item.init)
        }

        private class ViewHolder(itemView: View,
                                 rankItem: ConstraintLayout,
                                 val picImg: ImageView,
                                 val songList: List<TextView>) : RecyclerView.ViewHolder(itemView) {

            val badge: Badge = QBadgeView(rankItem.context).bindTarget(rankItem)

            init {
                badge.badgeGravity = Gravity.END or Gravity.TOP
                badge.setBadgePadding(4f, true)
                badge.setBadgeTextSize(12f, true)
                badge.setOnDragStateChangedListener { dragState, _, _ ->
                    if (dragState == Badge.OnDragStateChangedListener.STATE_SUCCEED) {
                        ToastUtils.showSuccess("消失 ${this.adapterPosition}")
                    }
                }
            }

        }
    }

    override val controller: ItemController = Controller
}