package com.kaibo.music.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.adapter.withItems
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.util.*
import com.kaibo.music.R
import com.kaibo.music.activity.base.BaseActivity
import com.kaibo.music.bean.RankSongListBean
import com.kaibo.music.bean.RecommendSongListBean
import com.kaibo.music.bean.SingerSongListBean
import com.kaibo.music.bean.SongBean
import com.kaibo.music.item.song.SongItem
import com.kaibo.music.net.Api
import com.kaibo.music.player.manager.PlayManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_song_list.*
import java.util.concurrent.TimeUnit

/**
 * @author kaibo
 * @createDate 2018/10/15 15:40
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class SongListActivity : BaseActivity() {

    private var sourceBitmap: Bitmap? = null
    private var transform: Bitmap? = null

    private val imgWidth by lazy { deviceWidth }
    // 原始高度
    private val imgHeight by lazy { headerView.layoutParams.height }
    private val aspectRatio by lazy { imgWidth.toDouble() / imgHeight }

    override fun getLayoutRes() = R.layout.activity_song_list

    override fun initOnCreate(savedInstanceState: Bundle?) {
        super.initOnCreate(savedInstanceState)
        toolbar.layoutParams = toolbar.layoutParams.apply {
            (this as FrameLayout.LayoutParams).topMargin = statusBarHeight
        }
        backBtn.clicks().`as`(bindLifecycle()).subscribe {
            onBackPressed()
        }
        when {
            intent.hasExtra("disstid") -> {
                val disstid = intent.getStringExtra("disstid")
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
                val singermid = intent.getStringExtra("singermid")
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
                val topid = intent.getIntExtra("topid", 0)
                Api.instance.getRankSongList(topid).checkResult()
                        .toMainThread().`as`(bindLifecycle()).subscribe({ rankSongListBean: RankSongListBean ->
                            loadHeaderImage(rankSongListBean.rankImage)
                            titleText.text = rankSongListBean.rankName
                            initSongList(rankSongListBean.songList)
                        }) {
                            it.printStackTrace()
                        }
            }
        }

        // 原始高度
        val firstHeight = imgHeight
        Observable
                .create<Int> {
                    // 监听滑动的距离
                    pullRefresh.setOnMoveTargetViewtListener { distance ->
                        if (distance >= 0) {
                            // 下拉
                            it.onNext(distance)
                        }
                    }
                }
                .toMainThread()
                .`as`(bindLifecycle())
                .subscribe {
                    // 修改headerView的高度
                    headerView.layoutParams = headerView.layoutParams.apply {
                        height = firstHeight + it
                    }
                }
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
        songListView.layoutManager = LinearLayoutManager(this)
        songListView.withItems(songList.map { songBean: SongBean ->
            SongItem(songBean) {
                setOnClickListener {
                    PlayManager.playOnline(songBean)
                }
            }
        })
    }
}