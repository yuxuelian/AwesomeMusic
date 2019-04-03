package com.kaibo.music.item.search

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaibo.core.adapter.Item
import com.kaibo.core.adapter.ItemController
import com.kaibo.core.util.inflate
import com.kaibo.music.R
import com.kaibo.music.player.bean.HotSearchBean
import kotlinx.android.synthetic.main.item_hot_search.view.*

/**
 * @author kaibo
 * @createDate 2018/10/15 10:47
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class HotSearchItem(val hotSearchBean: HotSearchBean,
                    val init: View.() -> Unit = {}) : Item {

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val itemView: View = parent.inflate(R.layout.item_hot_search)
            return ViewHolder(itemView, itemView.hotSearchText)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as HotSearchItem
            // 设置热搜关键字
            holder.hotSearchText.text = item.hotSearchBean.searchKey
            // 回调初始化的lambda表达式
            holder.itemView.apply(item.init)
        }

        private class ViewHolder(itemView: View, val hotSearchText: TextView) : RecyclerView.ViewHolder(itemView)
    }

    override val controller: ItemController = Controller
}