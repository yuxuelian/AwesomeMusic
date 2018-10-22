package com.kaibo.music.bean

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo

/**
 * @author kaibo
 * @createDate 2018/10/17 10:57
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 * 歌曲详细信息
 */

@Entity(tableName = "song",
        indices = [Index("mid")])
@PoKo
data class SongBean(
        @PrimaryKey
        // 歌曲id
        @ColumnInfo(name = "mid")
        @SerializedName("songmid")
        var mid: String,
        // 歌手名
        @ColumnInfo(name = "singer_name")
        @SerializedName("singername")
        var singername: String,
        // 歌曲名
        @ColumnInfo(name = "song_name")
        @SerializedName("songname")
        var songname: String,
        // 歌曲的图片地址
        @ColumnInfo(name = "image")
        @SerializedName("image")
        var image: String,
        // 歌曲的播放地址
        @ColumnInfo(name = "url")
        @SerializedName("url")
        var url: String
) : Parcelable {

    @Ignore
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mid)
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