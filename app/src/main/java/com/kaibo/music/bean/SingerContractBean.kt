package com.kaibo.music.bean

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo


/**
 * @author 56896
 * @date 2018/10/14 15:09
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class SingerContractBean(
        @SerializedName("title") val title: String,
        @SerializedName("items") val items: List<SingerBean>
)

@PoKo
data class SingerBean(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("name") val name: String,
        @SerializedName("avatar") val avatar: String
)