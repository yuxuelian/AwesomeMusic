package com.kaibo.music.player.bean

import android.os.Parcel
import android.os.Parcelable
import com.orhanobut.logger.Logger

/**
 * @author kaibo
 * @date 2018/11/3 21:22
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

data class LyricRowBean(
        // 当前行时间
        val timeMillis: Long,
        // 当前行文本
        var rowText: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(timeMillis)
        parcel.writeString(rowText)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LyricRowBean> {

        @JvmField
        var currentLyricMid: String = ""

        @JvmStatic
        fun parseLyric(lyricText: String): List<LyricRowBean> {
            // 拆分这个字符串为一行一句
            Logger.d(lyricText)
            val rowTexts = lyricText.split("\n")
            Logger.d(rowTexts.size)
            val resList = ArrayList<LyricRowBean>(rowTexts.size)
            // 开始标识
            resList.add(LyricRowBean(0, ""))
            val timeRegex = """^\[\d{2}:\d{2}\.\d{2}${'$'}""".toRegex()
            rowTexts.forEach {
                val row = it.split("]")
                Logger.d(row[0])
                if (row.size == 2 && timeRegex.matches(row[0]) && row[1].isNotEmpty()) {
                    // [00:00.00"
                    // 计算分
                    val minute = row[0].substring(1, 3).toInt()
                    // 计算秒
                    val second = row[0].substring(4, 6).toInt()
                    // 计算毫秒
                    val millis = row[0].substring(7).toLong()
                    // 计算时间戳
                    val timeMillis = (minute * 60 + second) * 1000 + millis
                    resList.add(LyricRowBean(timeMillis, row[1]))
                }
            }
            // 结束标识
            resList.add(LyricRowBean(Long.MAX_VALUE, ""))
            resList.trimToSize()
            return resList
        }

        override fun createFromParcel(parcel: Parcel): LyricRowBean {
            return LyricRowBean(parcel)
        }

        override fun newArray(size: Int): Array<LyricRowBean?> {
            return arrayOfNulls(size)
        }
    }
}