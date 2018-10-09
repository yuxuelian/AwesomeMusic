package com.kaibo.core.http

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo

/**
 * @author kaibo
 * @date 2018/6/28 18:31
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：Data是Object
 */

@PoKo
data class BaseBean<T>(
        @SerializedName("code") val code: Int,
        @SerializedName("msg") val msg: String,
        @SerializedName("data") val data: T
)