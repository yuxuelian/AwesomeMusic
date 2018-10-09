package com.kaibo.music.item

import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.kaibo.core.adapter.Item
import com.kaibo.core.adapter.ItemController
import com.kaibo.core.util.inflate
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.bean.BannerDataBean
import com.kaibo.music.weight.BannerIndicatorView
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import kotlinx.android.synthetic.main.item_recommend_banner_layout.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * @author kaibo
 * @date 2018/10/9 10:58
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class RecommendBannerItem(val bannerDataBeans: List<BannerDataBean>) : Item {

    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            Logger.d("onBindViewHolder")
            val itemView: View = parent.inflate(R.layout.item_recommend_banner_layout)
            return ViewHolder(itemView, itemView.banner, itemView.bannerIndicatorView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            holder as ViewHolder
            item as RecommendBannerItem
            // 复制一个集合出来(为了不影响原集合)
            val tempPageData: MutableList<BannerDataBean> = ArrayList(item.bannerDataBeans)
            // 向头部添加最后一个
            tempPageData.add(0, item.bannerDataBeans.last())
            // 向尾部添加第一个
            tempPageData.add(item.bannerDataBeans.first())
            // 设置适配器
            holder.banner.adapter = object : PagerAdapter() {
                // 重用View
                private val collectionView: MutableList<ImageView> = LinkedList()

                override fun instantiateItem(container: ViewGroup, position: Int): Any {
                    val imageView = if (collectionView.isEmpty()) {
                        ImageView(container.context).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    } else {
                        // 从集合中移出
                        collectionView.removeAt(0)
                    }.apply {
                        setImageResource(tempPageData[position].testDrawable)
                    }
                    container.addView(imageView)
                    return imageView
                }

                override fun isViewFromObject(view: View, any: Any): Boolean {
                    return view == any
                }

                override fun getCount() = tempPageData.size

                override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
                    container.removeView(any as ImageView)
                    // 将View添加到集合
                    collectionView.add(any)
                }
            }
            // 初始化的时候移动到位置1上去
            holder.banner.currentItem = 1
            // 监听状态改变
            holder.banner.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                // 记录一下当前位置
                private var currentIndex = holder.banner.currentItem

                override fun onPageScrollStateChanged(state: Int) {
                    when (state) {
                        ViewPager.SCROLL_STATE_IDLE -> {
                            // 空闲
                            if (currentIndex == tempPageData.size - 1) {
                                //最后位置的时候  需要移动到顺数第二个位置
                                holder.banner.setCurrentItem(1, false)
                            } else if (currentIndex == 0) {
                                //第一个位置的时候 需要移动到倒数第二个位置
                                holder.banner.setCurrentItem(tempPageData.size - 2, false)
                            }
                        }
                        ViewPager.SCROLL_STATE_DRAGGING -> {
                            //正在被拖拽
                        }
                        ViewPager.SCROLL_STATE_SETTLING -> {
                            //正在执行动画
                        }
                    }
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    // 记录下当前的选中位置
                    currentIndex = position
                }
            })
            //关联indicator
            holder.bannerIndicatorView.circleCount = item.bannerDataBeans.size
            holder.bannerIndicatorView.setupWithViewPager(holder.banner)

            //启动轮播
            Observable.interval(3000, TimeUnit.MILLISECONDS).toMainThread().subscribe {
                holder.banner.currentItem = holder.banner.currentItem + 1
            }
        }

        private class ViewHolder(itemView: View, val banner: ViewPager, val bannerIndicatorView: BannerIndicatorView) : RecyclerView.ViewHolder(itemView)
    }

    override val controller: ItemController
        get() = Controller

    override fun areContentsTheSame(newItem: Item): Boolean {
        //本次刷新是否需要进行视图更新
        return false
    }

    override fun areItemsTheSame(newItem: Item): Boolean = this.areContentsTheSame(newItem)

}