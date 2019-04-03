package com.kaibo.music.item.singer

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaibo.core.adapter.Item
import com.kaibo.core.adapter.ItemController
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.util.inflate
import com.kaibo.music.R
import com.kaibo.music.player.bean.SingerBean
import kotlinx.android.synthetic.main.item_singer.view.*

/**
 * @author 56896
 * @createDate 2018/10/14 15:34
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class SingerItem(val singerBean: SingerBean, val init: View.() -> Unit = {}) : Item {

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val itemView: View = parent.inflate(R.layout.item_singer)
            return ViewHolder(itemView, itemView.singerAvatar, itemView.singerName)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as SingerItem
            // 加载头像
            GlideApp.with(holder.singerAvatar)
                    .load(item.singerBean.avatar).placeholder(R.drawable.logo)
                    .error(R.drawable.logo).into(holder.singerAvatar)
            // 设置姓名
            holder.singerName.text = item.singerBean.name
            // 初始化Item
            holder.itemView.apply(item.init)
        }

        private class ViewHolder(itemView: View,
                                 val singerAvatar: ImageView,
                                 val singerName: TextView) : RecyclerView.ViewHolder(itemView)
    }

    override val controller: ItemController = Controller
}
