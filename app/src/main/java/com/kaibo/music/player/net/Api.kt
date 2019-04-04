package com.kaibo.music.player.net

import com.kaibo.core.http.BaseBean
import com.kaibo.core.http.HttpRequestManager
import com.kaibo.music.player.bean.*
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author kaibo
 * @createDate 2018/10/9 17:12
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */
interface Api {

    companion object {
        val instance = HttpRequestManager.retrofit.create(Api::class.java)
    }

    @GET("api/bannerList")
    fun getBannerList(): Observable<BaseBean<List<BannerDataBean>>>

    @GET("api/getRecommendList")
    fun getRecommendList(): Observable<BaseBean<List<RecommendBean>>>

    @GET("api/getSingerList")
    fun getSingerList(): Observable<BaseBean<List<SingerListBean>>>

    @GET("api/getRankList")
    fun getRankList(): Observable<BaseBean<List<RankBean>>>

    @GET("api/getHotSearch")
    fun getHotSearch(): Observable<BaseBean<List<HotSearchBean>>>

    @GET("api/getRecommendSongList")
    fun getRecommendSongList(@Query("disstid") disstid: String): Observable<BaseBean<RecommendSongListBean>>

    @GET("api/getSingerSongList")
    fun getSingerSongList(@Query("singermid") singermid: String): Observable<BaseBean<SingerSongListBean>>

    @GET("api/getRankSongList")
    fun getRankSongList(@Query("topid") topid: Int): Observable<BaseBean<RankSongListBean>>
}