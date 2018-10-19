package com.kaibo.music.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * 功能：本地歌单
 * 作者：yonglong on 2016/9/13 21:59
 * 邮箱：643872807@qq.com
 * 版本：2.5
 */
class Playlist() : Parcelable {
    var id: Long = 0
    //歌单id
    var pid: String? = null
    //歌单名
    var name: String? = null
    //歌曲数量
    var total: Long = 0
    //更新日期
    var updateDate: Long = 0
    //创建日期
    var date: Long = 0
    //描述
    var des: String? = null
    //排列顺序
    var order: String? = null
    //封面
    var coverUrl: String? = null

    var playCount: Long = 0

    //歌曲集合
    var musicList = mutableListOf<SongBean>()

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        pid = parcel.readString()
        name = parcel.readString()
        total = parcel.readLong()
        updateDate = parcel.readLong()
        date = parcel.readLong()
        des = parcel.readString()
        order = parcel.readString()
        coverUrl = parcel.readString()
        playCount = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(pid)
        parcel.writeString(name)
        parcel.writeLong(total)
        parcel.writeLong(updateDate)
        parcel.writeLong(date)
        parcel.writeString(des)
        parcel.writeString(order)
        parcel.writeString(coverUrl)
        parcel.writeLong(playCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Playlist> {
        override fun createFromParcel(parcel: Parcel): Playlist {
            return Playlist(parcel)
        }

        override fun newArray(size: Int): Array<Playlist?> {
            return arrayOfNulls(size)
        }
    }

}
