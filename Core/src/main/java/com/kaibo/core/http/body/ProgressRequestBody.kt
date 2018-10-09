package com.kaibo.core.http.body

import com.kaibo.core.http.HttpRequestManager
import com.kaibo.core.http.progress.ProgressMessage
import com.kaibo.core.http.progress.ProgressObservable
import io.reactivex.ObservableEmitter
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.File


/**
 * @author:Administrator
 * @date:2018/4/2 0002 下午 1:42
 * GitHub:
 * email:
 * description:上传文件  上传进度监听
 */

class ProgressRequestBody(key: String, private val requestBody: RequestBody) : RequestBody() {

    /**
     * 总长度
     */
    private var fillLength: Long = 0

    /**
     * 进度更新步长
     */
    private var step = 0L

    /**
     * 数据发射器
     */
    private val observableEmitter: ObservableEmitter<ProgressMessage>? = ProgressObservable[key]

    private var wrapperBufferedSink: BufferedSink? = null

    /**
     * 委派到主构造函数
     */
    constructor(key: String, file: File) : this(key, RequestBody.create(HttpRequestManager.FORM_DATA, file))

    /**
     * 返回了requestBody的类型，像什么form-data/MP3/MP4/png等等等格式
     */
    override fun contentType(): MediaType? = requestBody.contentType()

    /**
     * 返回了本RequestBody的长度，也就是上传的totalLength
     */
    override fun contentLength(): Long {
        //获取总长度,保存到全局变量中去
        this.fillLength = requestBody.contentLength()
        step = fillLength / 100L
        return fillLength
    }

    override fun writeTo(sink: BufferedSink) {
        //包装
        if (wrapperBufferedSink == null) {
            wrapperBufferedSink = Okio.buffer(sink(sink))
        }

        wrapperBufferedSink?.let {
            //写入
            requestBody.writeTo(it)
            //必须调用flush，否则最后一部分数据可能不会被写入
            it.flush()
        }
    }

    private fun sink(sink: Sink): Sink {
        return object : ForwardingSink(sink) {
            //当前写入的总字节数
            private var currentLength = 0L

            //上一次的进度值
            private var lastLength = 0L

            override fun write(source: Buffer, byteCount: Long) {
                try {
                    super.write(source, byteCount)
                    if (byteCount == -1L) {
                        //完成
                        observableEmitter?.onNext(ProgressMessage(fillLength, fillLength, true))
                        observableEmitter?.onComplete()
                    } else {
                        currentLength += byteCount
                        //过滤进度
                        if (currentLength - lastLength >= step) {
                            observableEmitter?.onNext(ProgressMessage(currentLength, fillLength))
                            lastLength = currentLength
                        }
                    }
                } catch (e: Throwable) {
                    //写入过程发生错误
                    observableEmitter?.onError(e)
                }
            }
        }
    }
}