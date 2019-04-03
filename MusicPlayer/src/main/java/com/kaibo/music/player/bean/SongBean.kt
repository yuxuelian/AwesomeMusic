package com.kaibo.music.player.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * @author kaibo
 * @createDate 2018/10/17 10:57
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 * 歌曲详细信息
 */

data class SongBean(
        // 歌曲id
        var songmid: String,
        // 歌手名
        var singername: String,
        // 歌曲名
        var songname: String,
        // 歌曲的图片地址
        var image: String,
        // 歌曲的播放地址
        var url: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(songmid)
        parcel.writeString(singername)
        parcel.writeString(songname)
        parcel.writeString(image)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SongBean> {
        override fun createFromParcel(parcel: Parcel): SongBean {
            return SongBean(parcel)
        }

        override fun newArray(size: Int): Array<SongBean?> {
            return arrayOfNulls(size)
        }
    }

}