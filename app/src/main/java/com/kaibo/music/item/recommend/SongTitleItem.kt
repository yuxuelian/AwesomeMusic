package com.kaibo.music.item.recommend

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaibo.core.adapter.Item
import com.kaibo.core.adapter.ItemController
import com.kaibo.core.util.inflate
import com.kaibo.music.R

/**
 * @author kaibo
 * @date 2018/10/10 12:58
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class SongTitleItem : Item {

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val itemView: View = parent.inflate(R.layout.item_song_title)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as SongTitleItem
        }
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override val controller: ItemController = Controller
}
