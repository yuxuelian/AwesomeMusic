package com.kaibo.core

import com.kaibo.core.http.HttpRequestManager
import com.kaibo.core.http.progress.ProgressObservable
import com.kaibo.core.util.CommandUtil
import com.kaibo.core.util.LunarCalendar
import com.kaibo.core.util.ThreadUtils
import com.kaibo.core.util.leaveTwoDecimal
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ThreadFactory

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }


    @Test
    fun test() {
        val file = File("""D:\\qq.apk""")
        if (file.exists()) {
            file.createNewFile()
        }
        val outputStream = FileOutputStream(file)
        val url = "https://qd.myapp.com/myapp/qqteam/Androidlite/qqlite_3.6.3.697_android_r110028_GuanWang_537055374_release_10000484.apk"
//        val url = "https://www.baidu.com/"
//        ProgressListener.downloadProgressListeners[url] = { currentLength, fillLength, done ->
//            //            println("currentLength=$currentLength fillLength=$fillLength  done=$done")
//            println(Thread.currentThread().name)
//        }

        ProgressObservable
                .listener(url)
                .subscribe({
                    println("it.currentLength=${it.currentLength}   it.fillLength=${it.fillLength}")
                }, {
                    it.printStackTrace()
                    println("下载出错")
                }, {
                    println("下载完成")
                })

        HttpRequestManager
                .retrofit
                .create(TestApi::class.java)
                .downLoadFile(url)
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val buff = ByteArray(2048)
                    val byteStream = it.byteStream()
                    while (byteStream.read(buff) != -1) {
                        outputStream.write(buff)
                    }
                    outputStream.flush()
                    byteStream.close()
                    outputStream.close()
                })

        while (true) {

        }
    }

    @Test
    fun test2() {
        println(System.getProperty("java.vm.version"))
//        val str: String? = null
//        println(str.toString())
//        println("123".isNotEmpty())

        val threadFactory: ThreadFactory = ThreadUtils.threadFactory("kaibo")
        val newThread = threadFactory.newThread {
            while (true) {
                println("123")
                Thread.sleep(1000)
            }
        }
        newThread.start()
        while (true) {

        }
    }

    @Test
    fun test3() {
//        println((1000L / 1234L).toFloat())
//        println(0.125456.leaveTwoDecimal())

        println(CommandUtil.exec("netstat -ano"))
    }

    @Test
    fun test4() {
        val l = LunarCalendar(System.currentTimeMillis())
        println("节气:" + l.termString)
        println("干支历:" + l.cyclicalDateString)
        println("星期:" + l.week)
        println("农历:" + l.lunarDateString)
    }
}