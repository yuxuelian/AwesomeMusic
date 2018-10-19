package com.kaibo.music.item.song

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
import com.kaibo.music.bean.SongBean
import kotlinx.android.synthetic.main.item_song.view.*

/**
 * @author kaibo
 * @date 2018/10/17 14:57
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class SongItem(val songBean: SongBean, val init: View.() -> Unit) : Item {

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val itemView: View = parent.inflate(R.layout.item_song)
            return ViewHolder(itemView, itemView.songImg, itemView.songName, itemView.songSinger)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as SongItem
            // 加载头像
            GlideApp.with(holder.songImg)
                    .load(item.songBean.image).placeholder(R.drawable.logo)
                    .error(R.drawable.logo).into(holder.songImg)
            holder.songName.text = item.songBean.songname
            holder.songSinger.text = item.songBean.singername
            // 初始化Item
            holder.itemView.apply(item.init)
        }

        private class ViewHolder(itemView: View,
                                 val songImg: ImageView,
                                 val songName: TextView,
                                 val songSinger: TextView) : RecyclerView.ViewHolder(itemView)
    }

    override val controller: ItemController = Controller
}
