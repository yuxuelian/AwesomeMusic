package com.kaibo.music.net

import com.kaibo.core.http.BaseBean
import com.kaibo.core.http.HttpRequestManager
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author kaibo
 * @date 2018/11/3 19:47
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

interface LyricApi {

    companion object {
        val instance = HttpRequestManager.retrofit.create(LyricApi::class.java)
    }

    @GET("api/lyric")
    fun getLyricByMid(@Query("songmid") mid: String): Observable<BaseBean<String>>

}