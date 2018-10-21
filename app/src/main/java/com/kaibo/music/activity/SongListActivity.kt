package com.kaibo.music.activity

import android.os.Bundle
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.kaibo.core.adapter.withItems
import com.kaibo.core.glide.GlideApp
import com.kaibo.core.util.checkResult
import com.kaibo.core.util.statusBarHeight
import com.kaibo.core.util.toMainThread
import com.kaibo.music.R
import com.kaibo.music.activity.base.BaseActivity
import com.kaibo.music.bean.RankSongListBean
import com.kaibo.music.bean.RecommendSongListBean
import com.kaibo.music.bean.SingerSongListBean
import com.kaibo.music.bean.SongBean
import com.kaibo.music.item.song.SongItem
import com.kaibo.music.net.Api
import com.kaibo.music.player.PlayManager
import kotlinx.android.synthetic.main.activity_song_list.*

/**
 * @author kaibo
 * @date 2018/10/15 15:40
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class SongListActivity : BaseActivity() {

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
                            // 加载Logo
                            GlideApp.with(this).load(recommendSongListBean.logo).into(songListLogo)
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
                            // 加载Logo
                            GlideApp.with(this).load(singerSongListBean.singerAvatar).into(songListLogo)
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
                            // 加载Logo
                            GlideApp.with(this).load(rankSongListBean.rankImage).into(songListLogo)
                            titleText.text = rankSongListBean.rankName
                            initSongList(rankSongListBean.songList)
                        }) {
                            it.printStackTrace()
                        }
            }
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