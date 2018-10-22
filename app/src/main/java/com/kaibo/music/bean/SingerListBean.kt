package com.kaibo.music.bean

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo


/**
 * @author 56896
 * @createDate 2018/10/14 15:09
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class SingerListBean(
        @SerializedName("title") val title: String,
        @SerializedName("items") val items: List<SingerBean>
)