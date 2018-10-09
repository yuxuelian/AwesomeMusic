package com.kaibo.music.bean

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo


/**
 * @author kaibo
 * @date 2018/10/9 11:02
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class BannerDataBean(
        @SerializedName("linkUrl") var linkUrl: String,
        @SerializedName("picUrl") var picUrl: String,
        @SerializedName("id") var id: Int
)
