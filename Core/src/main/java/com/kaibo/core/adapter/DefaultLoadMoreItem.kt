package com.kaibo.core.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.R
import kotlinx.android.synthetic.main.item_default_load_more.view.*
import org.jetbrains.anko.layoutInflater

/**
 * @author kaibo
 * @date 2018/7/4 16:35
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class DefaultLoadMoreItem : LoadMoreItem {

    override lateinit var loadMoreStatus: LoadMoreStatus

    /**
     * 加载更多回调
     */
    override var loadMoreClick: (() -> Unit)? = null

    /**
     * implements these functions to delegate the core method of RecyclerView's Item
     */
    companion object Controller : ItemController {
        override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            val inflater = parent.context.layoutInflater
            val view: View = inflater.inflate(R.layout.item_default_load_more, parent, false)
            return ViewHolder(view, view.progress, view.hint_text)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item) {
            /**
             * with the help of Kotlin Smart Cast, we can cast the ViewHolder and item first.
             * the RecyclerView DSL framework could guarantee the holder and item are correct, just cast it !
             *
             * 因为Kotlin的智能Cast 所以后面我们就不需要自己强转了
             * DSL 框架可以保证holder和item的对应性
             */
            holder as ViewHolder
            item as DefaultLoadMoreItem

            /**
             * what you do in OnBindViewHolder in RecyclerView, just do it here
             */
            when (item.loadMoreStatus) {
                LoadMoreStatus.Loading -> {
                    loading(holder)
                }
                LoadMoreStatus.Ready -> {
                    holder.hintText.text = "点我加载更多"
                    holder.progress.visibility = View.GONE
                    //点击加载更多
                    holder.hintText.clicks().subscribe {
                        //更改状态
                        item.loadMoreStatus = LoadMoreStatus.Loading
                        loading(holder)
                        //回调加载更多方法
                        item.loadMoreClick?.invoke()
                    }
                }
                LoadMoreStatus.Fail -> {
                    holder.hintText.text = "加载失败,点我重试"
                    holder.progress.visibility = View.GONE
                    //设置一个点击事件
                    holder.hintText.clicks().subscribe {
                        //更改状态
                        item.loadMoreStatus = LoadMoreStatus.Loading
                        loading(holder)
                        //回调加载更多方法
                        item.loadMoreClick?.invoke()
                    }
                }
                LoadMoreStatus.End -> {
                    //加载结束
                    holder.hintText.text = "没有更多数据"
                    holder.progress.visibility = View.GONE
                    //清除监听事件
                    holder.hintText.setOnClickListener(null)
                }
            }
            // custom settings for TextView passed by DSL
//            holder.loadMoreLayout.apply(item.init)
        }

        private fun loading(holder: ViewHolder) {
            //正在加载
            holder.hintText.text = "正在加载..."
            holder.progress.visibility = View.VISIBLE
            //清除监听事件
            holder.hintText.setOnClickListener(null)
        }

        /**
         * define your ViewHolder here to pass com.kaibo.mvp.view from OnCreateViewViewHolder to OnBindViewHolder
         * this ViewHolder class should be private and only use in this scope
         *
         * 在这里声明此Item所对应的ViewHolder，用来从OnCreateViewHolder传View到OnBindViewHolder中。
         * 这个ViewHolder类应该是私有的，只在这里用
         */
        private class ViewHolder(itemView: View?, val progress: ProgressBar, val hintText: TextView) : RecyclerView.ViewHolder(itemView)
    }

    /**
     * ItemController is necessary , it is often placed in the Item's companion Object
     * DON'T new an ItemController , because item viewType is corresponding to ItemController::class.java
     * or you will get many different viewType (for one type really) , which could break the RecyclerView's Cache
     *
     * 一般来讲，我们把ItemController放在Item的伴生对象里面，不要在这里new ItemController，因为在自动生成ViewType的时候，
     * 我们是根据ItemController::class.java 来建立一一对应关系，如果是new的话，会导致无法相等以至于生成许多ItemType，这样子会严重破坏Recyclerview的缓存机制
     */
    override val controller: ItemController
        get() = Controller

    override fun areContentsTheSame(newItem: Item): Boolean {
        //本次刷新是否需要进行视图更新
        return false
    }

    override fun areItemsTheSame(newItem: Item): Boolean = this.areContentsTheSame(newItem)
}
