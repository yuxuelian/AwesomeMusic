package com.kaibo.core.http.body

import com.kaibo.core.http.progress.ProgressMessage
import com.kaibo.core.http.progress.ProgressObservable
import io.reactivex.ObservableEmitter
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*


/**
 * @author:Administrator
 * @date:2018/4/2 0002 下午 1:54
 * GitHub:
 * email:
 * description:下载文件下载进度监听
 */


class ProgressResponseBody(key: String, private val response: ResponseBody) : ResponseBody() {

    /**
     * 总进度
     */
    private var fillLength = 0L

    /**
     * 进度步长
     */
    private var step = 0L

    /**
     * 包裹BufferedSource
     */
    private val wrapperBufferedSource: BufferedSource = Okio.buffer(wrapperSource(response.source()))

    /**
     * 获取数据发射器
     */
    private val observableEmitter: ObservableEmitter<ProgressMessage>? = ProgressObservable[key]

    override fun contentType(): MediaType? = response.contentType()

    override fun contentLength(): Long {
        this.fillLength = response.contentLength()
        this.step = fillLength / 100L
        println("步长  $step")
        return fillLength
    }

    override fun source(): BufferedSource = wrapperBufferedSource

    private fun wrapperSource(source: Source): Source {
        return object : ForwardingSource(source) {
            //当前读取的字节数
            private var currentLength = 0L

            //上次进度
            private var lastLength = 0L

            override fun read(sink: Buffer, byteCount: Long): Long {
                try {
                    val bytesRead: Long = super.read(sink, byteCount)
                    if (bytesRead == -1L) {
                        //下载完成
                        observableEmitter?.onNext(ProgressMessage(currentLength, fillLength, true))
                        observableEmitter?.onComplete()
                    } else {
                        currentLength += bytesRead
                        if (currentLength - lastLength >= step) {
                            //发送进度
                            observableEmitter?.onNext(ProgressMessage(currentLength, fillLength))
                            //更新进度
                            lastLength = currentLength
                        }
                    }
                    return bytesRead
                } catch (e: Throwable) {
                    //读取过程中遇到异常
                    observableEmitter?.onError(e)
                    return 0L
                }
            }
        }
    }
}