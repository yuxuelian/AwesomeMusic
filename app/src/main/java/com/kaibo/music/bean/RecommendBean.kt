package com.kaibo.music.bean

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo


/**
 * @author kaibo
 * @date 2018/10/10 11:51
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class RecommendBean(
        @SerializedName("disstid") var disstid: String,
        @SerializedName("dissname") var dissname: String,
        @SerializedName("name") var name: String,
        @SerializedName("imgurl") var imgurl: String
)