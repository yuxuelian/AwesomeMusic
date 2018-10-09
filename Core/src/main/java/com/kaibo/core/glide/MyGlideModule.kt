package com.kaibo.core.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.engine.executor.GlideExecutor
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.kaibo.core.http.HttpRequestManager
import java.io.InputStream


/**
 * @author:Administrator
 * @date:2018/3/28 0028 上午 11:40
 * GitHub:
 * email:
 * description:
 */

@GlideModule
class MyGlideModule : AppGlideModule() {

    /**
     * 配置选项
     */
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        //手动分配缓存  缓存4屏图片，默认是2
        val calculator = MemorySizeCalculator
                .Builder(context)
                .setMemoryCacheScreens(4F)
                .build()

        val myUncaughtThrowableStrategy = GlideExecutor.UncaughtThrowableStrategy {
            it?.printStackTrace()
        }

        //将Glide图片缓存到SD卡
        builder

                .setMemoryCache(LruResourceCache(calculator.memoryCacheSize.toLong()))
                //请求选项
                .setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565).disallowHardwareConfig())
                .setDiskCacheExecutor(GlideExecutor.newDiskCacheExecutor(myUncaughtThrowableStrategy))
                .setSourceExecutor(GlideExecutor.newSourceExecutor(myUncaughtThrowableStrategy))
                //日志级别
                .setLogLevel(Log.DEBUG)
    }

    /**
     * 注册组件
     */
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        //更换网络请求为okHttp
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(HttpRequestManager.okHttpClient))
    }

    /**
     * 禁用清单文件解析
     */
    override fun isManifestParsingEnabled() = false

}