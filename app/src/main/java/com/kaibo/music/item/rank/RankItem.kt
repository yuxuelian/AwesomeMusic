package com.kaibo.music.item.rank

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
import com.kaibo.music.bean.RankBean
import kotlinx.android.synthetic.main.item_rank_layout.view.*

/**
 * @author 56896
 * @date 2018/10/14 23:06
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class RankItem(val rankBean: RankBean) : Item {

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val itemView: View = parent.inflate(R.layout.item_rank_layout)
            return ViewHolder(itemView, itemView.picImg, listOf(itemView.songOne, itemView.songTwo, itemView.songThree))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as RankItem
            GlideApp.with(holder.picImg)
                    .load(item.rankBean.picUrl)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo).into(holder.picImg)
            val songList = item.rankBean.songList
            if (songList.size == 3) {
                songList.forEachIndexed { index, song ->
                    holder.songList[index].text = "${index + 1}.${song.songname}-${song.singername}"
                }
            }
        }

        private class ViewHolder(itemView: View,
                                 val picImg: ImageView,
                                 val songList: List<TextView>) : RecyclerView.ViewHolder(itemView)
    }

    override val controller: ItemController = Controller
}