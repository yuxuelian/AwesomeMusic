package com.kaibo.core.http.interceptor

import com.kaibo.core.http.body.ProgressResponseBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * @author:Administrator
 * @date:2018/4/2 0002 下午 3:28
 * GitHub:
 * email:
 * description:
 */
class ProgressInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val url = request.url().toString()
        val response: Response = chain.proceed(request)
        response.body()?.let {
            return response
                    .newBuilder()
                    .body(ProgressResponseBody(url, it))
                    .build()
        }
        return response
    }
}