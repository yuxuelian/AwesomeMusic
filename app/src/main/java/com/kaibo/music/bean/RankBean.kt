package com.kaibo.music.bean

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo


/**
 * @author 56896
 * @date 2018/10/14 23:08
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class RankBean(
        @SerializedName("id") val id: Int,
        @SerializedName("listenCount") val listenCount: Int,
        @SerializedName("picUrl") val picUrl: String,
        @SerializedName("songList") val songList: List<Song>,
        @SerializedName("topTitle") val topTitle: String,
        @SerializedName("type") val type: Int
)

@PoKo
data class Song(
        @SerializedName("singername") val singername: String,
        @SerializedName("songname") val songname: String
)