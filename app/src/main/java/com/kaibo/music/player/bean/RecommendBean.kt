package com.kaibo.music.player.bean

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo


/**
 * @author kaibo
 * @createDate 2018/10/10 11:51
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class RecommendBean(
        @SerializedName("disstid") val disstid: String,
        @SerializedName("dissname") val dissname: String,
        @SerializedName("name") val name: String,
        @SerializedName("imgurl") val imgurl: String
)