package com.kaibo.music.net

import com.kaibo.core.http.BaseBean
import com.kaibo.core.http.HttpRequestManager
import com.kaibo.music.bean.BannerDataBean
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

}