package com.kaibo.core

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * @author:Administrator
 * @date:2018/4/3 0003 上午 9:50
 * GitHub:
 * email:
 * description:
 */

interface TestApi {

    @Streaming
    @GET
    fun downLoadFile(@Url url: String): Observable<ResponseBody>

}