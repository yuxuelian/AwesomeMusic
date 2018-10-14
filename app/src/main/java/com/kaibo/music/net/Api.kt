package com.kaibo.music.net

import com.kaibo.core.http.BaseBean
import com.kaibo.core.http.HttpRequestManager
import com.kaibo.music.bean.BannerDataBean
import com.kaibo.music.bean.RankBean
import com.kaibo.music.bean.RecommendBean
import com.kaibo.music.bean.SingerContractBean
import io.reactivex.Observable
import retrofit2.http.GET

/**
 * @author kaibo
 * @date 2018/10/9 17:12
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
    fun getSingerList(): Observable<BaseBean<List<SingerContractBean>>>

    @GET("api/getRankList")
    fun getRankList(): Observable<BaseBean<List<RankBean>>>
}