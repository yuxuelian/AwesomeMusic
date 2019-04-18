package com.kaibo.player

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.*
import java.nio.charset.Charset
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    private val aaa = Aaa()

    @Test
    fun testByLazy() {
        println("testByLazy1")
        aaa.test()
        println("testByLazy2")
        aaa.test()
        aaa.test()
        aaa.test()
    }


    @Test
    fun test1() {
        val str1 = ""
        val str2 = ""
        val str3 = ""
        val str4 = ""

        val arrayList = ArrayList<String>()

        arrayList.add(str1)
        arrayList.add(str2)
        arrayList.add(str3)
        arrayList.add(str4)

        val str5 = ""
        val str6 = ""
        val str7 = ""
        val str8 = ""

        val arrayList2 = ArrayList<String>()

        arrayList2.add(str5)
        arrayList2.add(str6)
        arrayList2.add(str7)
        arrayList2.add(str8)

        arrayList2.forEachIndexed { index, s ->
            arrayList[index] = s
        }
    }

    @Test
    fun test2() {
        println(listOf(1, 2, 3, 3, 3) - listOf(3))
        println(Runtime.getRuntime().availableProcessors())
        val exec: Process = Runtime.getRuntime().exec("netstat -ano ")
        exec
                .inputStream
                .bufferedReader(Charset.forName("gb2312"))
                .lineSequence()
                .forEach {
                    println(it)
                }
    }

    class Aaa() {
        init {
            println("执行构造")
        }

        fun test() {
            println("测试----")
        }
    }

    @Test
    fun test3() {
        println(String.format("%02x", 20 and 0xFF))
    }

    fun fab(): (() -> Int) {
        var last = 0
        var current = 1

        return {
            val result = current
            current += last
            last = result
            result
        }

    }

    @Test
    fun test5() {
        val fab = fab()
        (1..10).forEach {
            println(fab())
        }
    }


    @Test
    fun test4() {
        //        println(System.`in`.bufferedReader().readLine())
        fun lambda(it: Int): Int {
            return if (it <= 2) {
                1
            } else {
                lambda(it - 1) + lambda(it - 2)
            }
        }
        println(lambda(10))
        (1..10).map(::lambda).forEach(::println)
    }

    @Test
    fun test6() {
        (0..100).forEach {
            println(UUID.randomUUID())
        }
    }

    @Test
    fun text7() {
        val src = File("""C:\Users\kaibo\Desktop\temp_src.txt""")
        val target = File("""C:\Users\kaibo\Desktop\target_src.txt""")
        BufferedReader(InputStreamReader(FileInputStream(src))).useLines {
            val bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(target)))
            var isMulti = false
            var lastLine = ""
            it.iterator().forEach { line ->
                if (line.contains("""/*""") && line.contains("""*/""")) {
                    // 本行删除
                } else if (line.contains("""/*""")) {
                    isMulti = true
                } else if (line.contains("""*/""")) {
                    isMulti = false
                } else {
                    if (!isMulti) {
                        if (line.contains("""//""")) {
                            val split = line.split("""//""")
                            if (!split[0].allEmpty()) {
                                bufferedWriter.write(split[0])
                                bufferedWriter.newLine()
                            }
                        } else {
                            if (lastLine.contains("""import""") && !line.contains("""import""")) {
                                // 上一行有import  本行没有
                                bufferedWriter.newLine()
                                bufferedWriter.write("""/**""")
                                bufferedWriter.newLine()
                                bufferedWriter.write(""" * @author 王开波""")
                                bufferedWriter.newLine()
                                bufferedWriter.write(""" */""")
                                bufferedWriter.newLine()
                                bufferedWriter.newLine()
                            }
                            bufferedWriter.write(line)
                            bufferedWriter.newLine()
                            lastLine = line
                        }
                    }
                }
            }
            bufferedWriter.flush()
            bufferedWriter.close()
        }
    }
}

fun String.allEmpty() = this.trim().isEmpty()
