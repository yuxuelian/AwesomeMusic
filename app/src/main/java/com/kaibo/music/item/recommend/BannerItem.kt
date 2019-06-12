package com.kaibo.music.item.recommend

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.kaibo.core.adapter.Item
import com.kaibo.core.adapter.ItemController
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.util.inflate
import com.kaibo.music.R
import com.kaibo.music.player.bean.BannerDataBean
import com.stx.xhb.androidx.XBanner
import kotlinx.android.synthetic.main.item_banner.view.*

/**
 * @author kaibo
 * @createDate 2018/10/10 12:55
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 * 单例bannerItem
 */

class BannerItem : Item {

    private class ViewHolder(itemView: View, val xbanner: XBanner) : RecyclerView.ViewHolder(itemView)

    private var holder: ViewHolder? = null

    private var bannerDataBeanList: List<BannerDataBean> = emptyList()

    fun setData(bannerDataBeanList: List<BannerDataBean>) {
        this.bannerDataBeanList = bannerDataBeanList
    }

    fun startAutoPlay() {
        holder?.xbanner?.startAutoPlay()
    }

    fun stopAutoPlay() {
        holder?.xbanner?.stopAutoPlay()
    }

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val itemView: View = parent.inflate(R.layout.item_banner)
            return ViewHolder(itemView, itemView.xbanner)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as BannerItem
            if (item.holder == null) {
                //设置图片加载器
                holder.xbanner.loadImage { banner, model, view, position ->
                    GlideApp
                            .with(view)
                            .load((model as BannerDataBean).picUrl)
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.logo)
                            .into(view as ImageView)
                }
                //加载数据
                holder.xbanner.setData(item.bannerDataBeanList, null)
                //把holder设置出去以便进行 startAutoPlay stopAutoPlay操作
                item.holder = holder
            }
        }
    }

    override val controller: ItemController
        get() = Controller
}
