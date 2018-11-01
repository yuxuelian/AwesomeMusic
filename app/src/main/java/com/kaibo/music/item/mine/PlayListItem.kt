package com.kaibo.music.item.mine

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.kaibo.core.adapter.Item
import com.kaibo.core.adapter.ItemController
import com.kaibo.core.util.inflate
import com.kaibo.music.R
import kotlinx.android.synthetic.main.item_play_list.view.*

/**
 * @author kaibo
 * @date 2018/11/1 16:47
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

data class PlayListItemBean(
        @DrawableRes
        val listImageRes: Int,
        val listText: String,
        val count: Int,
        @DrawableRes
        val endImageRes: Int = R.drawable.ic_play_circle
)

class PlayListItem(val playListBean: PlayListItemBean, val init: View.() -> Unit = {}) : Item {

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val itemView: View = parent.inflate(R.layout.item_play_list)
            return ViewHolder(itemView,
                    itemView.item_list_image,
                    itemView.item_list_text,
                    itemView.item_count_text,
                    itemView.item_end_image)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as PlayListItem
            holder.itemListImage.setImageResource(item.playListBean.listImageRes)
            holder.itemListText.text = item.playListBean.listText
            holder.itemCountText.text = "${item.playListBean.count}首"
            holder.itemEndImage.setImageResource(item.playListBean.endImageRes)
            // 初始化
            holder.itemView.apply(item.init)
        }
    }

    private class ViewHolder(itemView: View,
                             val itemListImage: ImageView,
                             val itemListText: TextView,
                             val itemCountText: TextView,
                             val itemEndImage: ImageView) : RecyclerView.ViewHolder(itemView)

    override val controller: ItemController = Controller
}