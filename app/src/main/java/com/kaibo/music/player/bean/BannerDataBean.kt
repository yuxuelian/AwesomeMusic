package com.kaibo.music.player.bean

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo


/**
 * @author kaibo
 * @createDate 2018/10/9 11:02
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class BannerDataBean(
        @SerializedName("linkUrl") val linkUrl: String,
        @SerializedName("picUrl") val picUrl: String,
        @SerializedName("id") val id: Int
)
