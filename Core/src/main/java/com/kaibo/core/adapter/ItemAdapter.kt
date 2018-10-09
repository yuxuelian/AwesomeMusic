package com.kaibo.core.adapter

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

interface Item {
    val controller: ItemController

    fun areItemsTheSame(newItem: Item): Boolean = false

    fun areContentsTheSame(newItem: Item): Boolean = false
}

interface ItemController {
    fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
    fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item)
}

class ItemAdapter(private val itemManager: ItemManagerAbstract) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), MutableList<Item> by itemManager {

    init {
        itemManager.observer = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemManager.getController(viewType).onCreateViewHolder(parent)

    override fun getItemCount() = itemManager.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = itemManager[position].controller.onBindViewHolder(holder, itemManager[position])

    override fun getItemViewType(position: Int) = ItemManager.getViewType(itemManager[position].controller)
}

fun RecyclerView.withItems(items: List<Item>): ItemManager {
    val itemManager = ItemManager(items.toMutableList())
    this.adapter = ItemAdapter(itemManager)
    return itemManager
}

fun RecyclerView.withItems(init: MutableList<Item>.() -> Unit) = withItems(mutableListOf<Item>().apply(init))

interface ItemManagerAbstract : MutableList<Item> {
    var observer: RecyclerView.Adapter<RecyclerView.ViewHolder>?
}

class ItemManager(private val delegated: MutableList<Item> = mutableListOf()) : ItemManagerAbstract {
    override var observer: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
    private val itemListSnapshot: List<Item> get() = delegated

    /**
     * 加载更多Item
     */
    internal var loadMoreItem: LoadMoreItem? = null

    init {
        ensureControllers(delegated)
    }

    companion object ItemControllerManager {
        // companion object保证了单例 因此ViewType肯定是从0开始
        private var viewType = 0

        // controller to com.kaibo.mvp.view type
        private val c2vt = mutableMapOf<ItemController, Int>()

        // com.kaibo.mvp.view type to controller
        private val vt2c = mutableMapOf<Int, ItemController>()

        /**
         * 检查Item(对应的controller)是否已经被注册，如果没有，那就注册一个ViewType
         */
        private fun ensureController(item: Item) {
            val controller = item.controller
            if (!c2vt.contains(controller)) {
                c2vt[controller] = viewType
                vt2c[viewType] = controller
                viewType++
            }
        }

        /**
         * 对于一个Collection的ViewType注册，先进行一次去重
         */
        private fun ensureControllers(items: Collection<Item>): Unit = items.distinctBy(Item::controller).forEach(ItemControllerManager::ensureController)

        /**
         * 根据ItemController获取对应的Item -> 代理Adapter.getItemViewType
         */
        fun getViewType(controller: ItemController): Int = c2vt[controller]
                ?: throw IllegalStateException("ItemController $controller is not ensured")

        /**
         * 根据ViewType获取ItemController -> 代理OnCreateViewHolder相关逻辑
         */
        fun getController(viewType: Int): ItemController = vt2c[viewType]
                ?: throw IllegalStateException("ItemController $viewType is unused")
    }

    override val size: Int get() = delegated.size

    override fun contains(element: Item) = delegated.contains(element)

    override fun containsAll(elements: Collection<Item>) = delegated.containsAll(elements)

    override fun get(index: Int): Item = delegated[index]

    override fun indexOf(element: Item) = delegated.indexOf(element)

    override fun isEmpty() = delegated.isEmpty()

    override fun iterator() = delegated.iterator()

    override fun lastIndexOf(element: Item) = delegated.lastIndexOf(element)

    override fun listIterator() = delegated.listIterator()

    override fun listIterator(index: Int) = delegated.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = delegated.subList(fromIndex, toIndex)

    override fun add(element: Item) = delegated.add(element).also {
        ensureController(element)
        if (it) observer?.notifyItemInserted(size)
    }

    override fun add(index: Int, element: Item) = delegated.add(index, element).also {
        ensureController(element)
        observer?.notifyItemInserted(index)
    }

    override fun addAll(index: Int, elements: Collection<Item>) = delegated.addAll(elements).also {
        ensureControllers(elements)
        if (it) observer?.notifyItemRangeInserted(index, elements.size)
    }

    override fun addAll(elements: Collection<Item>) = delegated.addAll(elements).also {
        ensureControllers(elements)
        if (it) observer?.notifyItemRangeInserted(size, elements.size)
    }

    override fun clear() = delegated.clear().also {
        observer?.notifyItemRangeRemoved(0, size)
    }

    override fun remove(element: Item): Boolean = delegated.remove(element).also {
        if (it) observer?.notifyDataSetChanged()
    }

    override fun removeAll(elements: Collection<Item>): Boolean = delegated.removeAll(elements).also {
        if (it) observer?.notifyDataSetChanged()
    }

    override fun removeAt(index: Int) = delegated.removeAt(index).also {
        observer?.notifyItemRemoved(index)
    }

    override fun retainAll(elements: Collection<Item>) = delegated.retainAll(elements).also {
        if (it) observer?.notifyDataSetChanged()
    }

    override fun set(index: Int, element: Item) = delegated.set(index, element).also {
        ensureController(element)
        observer?.notifyItemChanged(index)
    }

    private fun refreshAll(elements: List<Item>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = delegated[oldItemPosition]
                val newItem = elements[newItemPosition]
                return oldItem.areItemsTheSame(newItem)
            }

            override fun getOldListSize(): Int = delegated.size
            override fun getNewListSize(): Int = elements.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = delegated[oldItemPosition]
                val newItem = elements[newItemPosition]
                return oldItem.areContentsTheSame(newItem)
            }
        }
        val result = DiffUtil.calculateDiff(diffCallback, true)
        delegated.clear()
        delegated.addAll(elements)
        ensureControllers(elements)
        result.dispatchUpdatesTo(observer)
    }

    /**
     * 创建了一个全新的 mutableList 替换 上次设置的 mutableList
     */
    fun refreshAll(init: MutableList<Item>.() -> Unit) {
        val itemList = mutableListOf<Item>().apply(init)
        loadMoreItem?.let {
            //如果 loadMoreItem 不为null  直接添加
            itemList.add(it)
        }
        refreshAll(itemList)
    }

    /**
     * 在上次的 mutableList 基础上再增加新的 mutableList
     */
    fun autoRefresh(init: MutableList<Item>.() -> Unit) {
        val snapshot = this.itemListSnapshot.toMutableList()
        snapshot.apply(init)
        loadMoreItem?.let {
            //如果 loadMoreItem 不为null  移动到最后去
            if (!snapshot.contains(it)) {
                //不存在就添加
                snapshot.add(it)
            } else {
                //存在就交换
                snapshot.swap(snapshot.indexOf(it), snapshot.size - 1)
            }
        }
        refreshAll(snapshot)
    }
}

/**
 * 交换 MutableList 中指定的两个位置的元素
 */
fun <E> MutableList<E>.swap(position1: Int, position2: Int) {
    val e: E = this.removeAt(position1)
    if (position1 == size) {
        this.add(e)
    } else {
        this.add(position2, e)
    }
}

//--------------------------------------------------加载更多--------------------------------------------------------------------

/**
 * 扩展一个加载更多的方法
 */
fun RecyclerView.setLoadMoreListener(
        itemManager: ItemManager,
        loadMore: LoadMoreItem = DefaultLoadMoreItem(),
        loadMoreListener: () -> Unit): LoadMoreManager {
    //设置加载更多Item
    itemManager.loadMoreItem = loadMore
    return LoadMoreManager(this, itemManager, loadMore.apply { this.loadMoreClick = loadMoreListener }, loadMoreListener)
}

/**
 * 自定义加载更多View需要用到
 */
interface LoadMoreItem : Item {

    /**
     * 加载更多状态
     */
    var loadMoreStatus: LoadMoreStatus

    /**
     * 点击回调加载更多
     */
    var loadMoreClick: (() -> Unit)?
}

/**
 * 加载更多状态
 */
enum class LoadMoreStatus {
    /**
     * 正在加载更多状态
     */
    Loading,
    /**
     * 可加载更多状态
     */
    Ready,
    /**
     * 加载失败
     */
    Fail,
    /**
     * 加载结束
     */
    End;
}

/**
 * 加载更多管理
 */
class LoadMoreManager(rv: RecyclerView,
                      private val itemManager: ItemManager,
                      private val loadMoreItem: LoadMoreItem,
                      private val loadMoreListener: () -> Unit) {

    private var loadMoreStatus: LoadMoreStatus = LoadMoreStatus.Ready

    init {
        //同步状态
        loadMoreItem.loadMoreStatus = LoadMoreStatus.Ready

        //自动加载更多
//        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
//                        !recyclerView.canScrollVertically(1) &&
//                        loadMoreStatus == LoadMoreStatus.Ready) {
//                    loadMoreStatus = LoadMoreStatus.Loading
//                    loadMoreItem.loadMoreStatus = LoadMoreStatus.Loading
//                    //刷新一下  这里的刷新主要是刷新  loadMoreItem  显示的状态
//                    itemManager.autoRefresh { }
//                    //回调加载更多
//                    loadMoreListener.invoke()
//                }
//            }
//        })
    }

    /**
     * 清除数据   并主动启动加载更多
     */
    fun clearAndStartLoading() {
        //加载更多结束
        loadMoreStatus = LoadMoreStatus.Loading
        loadMoreItem.loadMoreStatus = LoadMoreStatus.Loading
        itemManager.refreshAll { }
        //回调加载更多
        loadMoreListener.invoke()
    }

    /**
     * 恢复状态为可加载更多状态
     */
    fun reset() {
        setLoadMoreComplete {}
    }

    /**
     * 本次加载更多成功
     */
    fun setLoadMoreComplete(init: MutableList<Item>.() -> Unit) {
        //加载更多结束
        loadMoreStatus = LoadMoreStatus.Ready
        loadMoreItem.loadMoreStatus = LoadMoreStatus.Ready
        //刷新一下  这里的刷新主要是刷新  loadMoreItem  显示的状态
        itemManager.autoRefresh(init)
    }

    /**
     * 加载更多失败
     */
    fun setLoadMoreFail() {
        //加载更多失败
        loadMoreItem.loadMoreStatus = LoadMoreStatus.Fail
        //刷新一下  这里的刷新主要是刷新  loadMoreItem  显示的状态
        itemManager.autoRefresh {}
    }

    /**
     * 加载更多结束
     */
    fun setLoadMoreEnd(init: MutableList<Item>.() -> Unit) {
        //加载更多结束
        loadMoreItem.loadMoreStatus = LoadMoreStatus.End
        //刷新一下  这里的刷新主要是刷新  loadMoreItem  显示的状态
        itemManager.autoRefresh(init)
    }
}




