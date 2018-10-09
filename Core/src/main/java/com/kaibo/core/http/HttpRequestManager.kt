package com.kaibo.core.http

import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author:Administrator
 * @date:2018/3/28 0028 上午 11:21
 * GitHub:
 * email:
 * description:
 */

object HttpRequestManager {

    internal var BASE_URL = "http://192.168.3.98:3010/mock/15/"

    //缓存大小   20M
    private const val CACHE_SIZE = 1024 * 1024 * 20L

    //连接超时时间  30s
    private const val CONNECT_TIMEOUT_TIME = 30L
    //读超时时间  30s
    private const val READ_TIMEOUT_TIME = 30L
    //写超时时间  30s
    private const val WRITE_TIMEOUT_TIME = 30L

    //上传图片
    val IMAGE_MEDIA_TYPE: MediaType? = MediaType.parse("image/*")
    //上传json
    val JSON: MediaType? = MediaType.parse("application/json; charset=utf-8")
    //普通文本
    val TEXT: MediaType? = MediaType.parse("text/plain; charset=utf-8")
    //文件
    val FORM_DATA: MediaType? = MediaType.parse("multipart/form-data")

    private val interceptors: MutableList<Interceptor> = ArrayList()

    /**
     * 此方法需要在  okHttpClient  第一次被使用之前调用   否则无效
     */
    fun setOtherInterceptor(vararg interceptors: Interceptor) {
        this.interceptors.addAll(interceptors)
    }

    /**
     * 全局唯一一个 OkHttpClient  实例
     */
    val okHttpClient: OkHttpClient by lazy {
        val builder: OkHttpClient.Builder = OkHttpClient
                .Builder()
                //进度拦截器
//                .addInterceptor(ProgressInterceptor())
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor {
                    it.proceed(it.request()
                            .newBuilder()
                            .addHeader("Connection", "close")
                            .build())
                }
                //失败重连
                .retryOnConnectionFailure(false)
                .connectTimeout(CONNECT_TIMEOUT_TIME, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_TIME, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_TIME, TimeUnit.SECONDS)
//           .cache(Cache(File("${BaseApplication.INSTANCE.cacheDir.absolutePath}${File.separator}okHttpCaches"), CACHE_SIZE))

        //添加别的拦截器
        interceptors.forEach {
            builder.addInterceptor(it)
        }

        builder.build()
    }

    /**
     * Retrofit   实例
     */
    val retrofit: Retrofit by lazy {
        Retrofit
                .Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                //同步发出请求
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                //使用okhttp的线程池
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                //指定在RxJava的线程池发出请求
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
    }
}