package com.kaibo.music.item.recommend

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.kaibo.core.adapter.Item
import com.kaibo.core.adapter.ItemController
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.util.inflate
import com.kaibo.music.R
import com.kaibo.music.bean.RecommendBean
import kotlinx.android.synthetic.main.item_recommend_banner_layout.view.*

/**
 * @author kaibo
 * @date 2018/10/9 10:58
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class SongListItem(val recommendBean: RecommendBean) : Item {

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val itemView: View = parent.inflate(R.layout.item_recommend_banner_layout)
            return ViewHolder(itemView, itemView.recommendImg, itemView.nameText, itemView.dissnameText)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as SongListItem

            //加载图片
            GlideApp.with(holder.recommendImg).load(item.recommendBean.imgurl)
                    .placeholder(R.drawable.logo).error(R.drawable.logo).into(holder.recommendImg)
            holder.nameText.text = item.recommendBean.name
            holder.dissnameText.text = item.recommendBean.dissname
        }

        private class ViewHolder(itemView: View,
                                 val recommendImg: ImageView,
                                 val nameText: TextView,
                                 val dissnameText: TextView) : RecyclerView.ViewHolder(itemView)
    }

    override val controller: ItemController
        get() = Controller

    override fun areContentsTheSame(newItem: Item): Boolean {
        //本次刷新是否需要进行视图更新
        return false
    }

    override fun areItemsTheSame(newItem: Item): Boolean = this.areContentsTheSame(newItem)

}