package com.kaibo.music.fragment.songlist

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaibo.core.adapter.withItems
import com.kaibo.core.fragment.BaseFragment
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.util.*
import com.kaibo.music.R
import com.kaibo.music.activity.MainActivity
import com.kaibo.music.bean.RankSongListBean
import com.kaibo.music.bean.RecommendSongListBean
import com.kaibo.music.bean.SingerSongListBean
import com.kaibo.music.bean.SongBean
import com.kaibo.music.fragment.player.PlayerFragment
import com.kaibo.music.item.song.SongItem
import com.kaibo.music.net.Api
import com.kaibo.music.player.manager.PlayManager
import com.yan.pullrefreshlayout.PullRefreshLayout
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_song_list.*

/**
 * @author kaibo
 * @date 2018/11/1 9:52
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class SongListFragment : BaseFragment() {

    companion object {
        fun newInstance(arguments: Bundle = Bundle()): SongListFragment {
            return SongListFragment().apply {
                this.arguments = arguments
            }
        }
    }

    override fun getLayoutRes() = R.layout.fragment_song_list

    private var sourceBitmap: Bitmap? = null

    private val imgWidth by lazy { mActivity.deviceWidth }
    // 头部布局的原始高度
    private val imgHeight by lazy { headerView.layoutParams.height }
    // 布局的宽高比
    private val aspectRatio by lazy { imgWidth.toDouble() / imgHeight }

    // 放大按钮需要用到
    private val pullDownMaxDistance by lazy { dip(200) }
    private val playBtnWidthScope by lazy { Pair(dip(130), dip(210)) }
    private val playBtnHeightScope by lazy { Pair(dip(30), dip(48)) }
    private val imageSizeScope by lazy { Pair(dip(16), dip(26)) }
    private val btnTextSizeScope by lazy { Pair(12, 20) }

    private fun Pair<Int, Int>.calcValue(@FloatRange(from = 0.0, to = 1.0) ratio: Float): Int {
        return first + ((second - first) * ratio).toInt()
    }

    private fun Bundle.hasExtra(extra: String) = this.get(extra) != null

    override fun initViewCreated(savedInstanceState: Bundle?) {
        super.initViewCreated(savedInstanceState)
        toolbar.layoutParams = toolbar.layoutParams.apply {
            (this as FrameLayout.LayoutParams).topMargin = mActivity.statusBarHeight
        }
        backBtn.easyClick(bindLifecycle()).subscribe {
            pop()
        }
        arguments?.let { intent: Bundle ->
            when {
                intent.hasExtra("disstid") -> {
                    val disstid = intent.getString("disstid", "")
                    Api.instance.getRecommendSongList(disstid).checkResult()
                            .toMainThread().`as`(bindLifecycle()).subscribe({ recommendSongListBean: RecommendSongListBean ->
                                loadHeaderImage(recommendSongListBean.logo)
                                titleText.text = recommendSongListBean.dissname
                                initSongList(recommendSongListBean.songList)
                            }) {
                                it.printStackTrace()
                            }
                }
                intent.hasExtra("singermid") -> {
                    val singermid = intent.getString("singermid", "")
                    Api.instance.getSingerSongList(singermid).checkResult()
                            .toMainThread().`as`(bindLifecycle()).subscribe({ singerSongListBean: SingerSongListBean ->
                                loadHeaderImage(singerSongListBean.singerAvatar)
                                titleText.text = singerSongListBean.singerName
                                initSongList(singerSongListBean.songList)
                            }) {
                                it.printStackTrace()
                            }
                }
                intent.hasExtra("topid") -> {
                    val topid = intent.getInt("topid", -1)
                    Api.instance.getRankSongList(topid).checkResult()
                            .toMainThread().`as`(bindLifecycle()).subscribe({ rankSongListBean: RankSongListBean ->
                                loadHeaderImage(rankSongListBean.rankImage)
                                titleText.text = rankSongListBean.rankName
                                initSongList(rankSongListBean.songList)
                            }) {
                                it.printStackTrace()
                            }
                }
                else -> {

                }
            }
        }

        // 原始高度
        val firstHeight = imgHeight
        Observable
                .create<Int> {
                    // 监听滑动的距离
                    pullRefresh.setOnMoveTargetViewListener(object : PullRefreshLayout.OnMoveTargetViewListener {
                        override fun onMoveDistance(distance: Int) {
                            if (distance >= 0) {
                                // 下拉
                                it.onNext(distance)
                            }
                        }
                    })
                }
                .toMainThread()
                .`as`(bindLifecycle())
                .subscribe {
                    // 修改headerView的高度
                    headerView.layoutParams = headerView.layoutParams.apply {
                        height = firstHeight + it
                    }
                    val currentRatio = it.toFloat() / pullDownMaxDistance
                    playBtn.layoutParams = playBtn.layoutParams.apply {
                        width = playBtnWidthScope.calcValue(currentRatio)
                        height = playBtnHeightScope.calcValue(currentRatio)
                    }
                    playBtnImage.layoutParams = playBtnImage.layoutParams.apply {
                        width = imageSizeScope.calcValue(currentRatio)
                        height = width
                    }
                    playBtnText.textSize = btnTextSizeScope.calcValue(currentRatio).toFloat()
                }

        // 设置最大下拉距离
        pullRefresh.pullDownMaxDistance = pullDownMaxDistance
    }

    private fun loadHeaderImage(headerImageUrl: String) {
        Observable
                .create<Bitmap> {
                    try {
                        val bitmap = GlideApp.with(this).asBitmap().load(headerImageUrl).submit().get()
                        // 处理一下Bitmap
                        it.onNext(bitmap.clipTo(aspectRatio))
                        it.onComplete()
                    } catch (e: Throwable) {
                        it.onError(e)
                    }
                }
                .subscribeOn(Schedulers.io())
                .toMainThread()
                .`as`(bindLifecycle())
                .subscribe({
                    // 获取到的bitmap 保存到全局并设置给ImageView显示
                    sourceBitmap = it
                    songListLogo.setImageBitmap(it)
                }) {
                    it.printStackTrace()
                }
    }

    private fun initSongList(songList: List<SongBean>) {
        songListView.layoutManager = LinearLayoutManager(mActivity)
        songListView.withItems(songList.map { songBean: SongBean ->
            SongItem(songBean) {
                setOnClickListener {
                    // 这个方法执行起来比较耗时  这里使用异步执行
                    singleAsync(bindLifecycle(), onSuccess = {
                        // 设置成功  启动PlayerFragment
                        (mActivity as MainActivity).rootFragment.start(PlayerFragment.newInstance())
                    }) {
                        PlayManager.setPlaySong(songBean)
                    }
                }
            }
        })
    }

    override fun onBackPressedSupport(): Boolean {
        pop()
        return true
    }
}