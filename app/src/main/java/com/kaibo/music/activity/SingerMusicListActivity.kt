package com.kaibo.music.activity

import com.kaibo.music.R
import com.kaibo.music.bean.SingerBean

/**
 * @author kaibo
 * @date 2018/10/15 15:40
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

class SingerMusicListActivity : BaseAnimActivity() {

    private val singerBean by lazy {
        intent.getParcelableExtra("singerBean") as SingerBean
    }

    override fun getLayoutRes() = R.layout.activity_singer_music_list

}