package com.kaibo.music.bean

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo
import com.kaibo.music.player.MusicPlayerService

/**
 * @author kaibo
 * @date 2018/10/17 10:57
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 * 歌曲详细信息
 */

@PoKo
data class SongBean(
        // 歌曲id
        @SerializedName("songmid") val mid: String = "",
        // 歌手名
        @SerializedName("singername") val singername: String = "",
        // 歌曲名
        @SerializedName("songname") val songname: String = "",
        // 歌曲的图片地址
        @SerializedName("image") val image: String = "",
        // 歌曲的播放地址
        @SerializedName("url") val url: String = ""
) : Parcelable {
    /**
     * 歌曲的播放时长
     */
    var duration: Int = 0

    /**
     * 歌曲类型  local  qq  等
     */
    var type: String = MusicPlayerService.QQ

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
        duration = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mid)
        parcel.writeString(singername)
        parcel.writeString(songname)
        parcel.writeString(image)
        parcel.writeString(url)
        parcel.writeInt(duration)
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