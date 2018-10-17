package com.kaibo.music.bean

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo


/**
 * @author kaibo
 * @date 2018/10/17 14:47
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class RecommendSongListBean(
        @SerializedName("disstid") val disstid: String,
        @SerializedName("dissname") val dissname: String,
        @SerializedName("logo") val logo: String,
        @SerializedName("songList") val songList: List<SongBean>
)

@PoKo
data class SingerSongListBean(
        @SerializedName("singerMid") val singerMid: String,
        @SerializedName("singerName") val singerName: String,
        @SerializedName("singerAvatar") val singerAvatar: String,
        @SerializedName("songList") val songList: List<SongBean>
)

@PoKo
data class RankSongListBean(
        @SerializedName("rankName") val rankName: String,
        @SerializedName("rankImage") val rankImage: String,
        @SerializedName("songList") val songList: List<SongBean>
)
