package com.kaibo.music.player.bean

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo


/**
 * @author 56896
 * @createDate 2018/10/14 23:08
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class RankBean(
        @SerializedName("id") val id: Int,
        @SerializedName("listenCount") val listenCount: Int,
        @SerializedName("picUrl") val picUrl: String,
        @SerializedName("songList") val songBeanList: List<SongBean>,
        @SerializedName("topTitle") val topTitle: String,
        @SerializedName("type") val type: Int
)