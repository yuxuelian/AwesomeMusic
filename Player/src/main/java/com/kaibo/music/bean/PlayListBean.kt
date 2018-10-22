package com.kaibo.music.bean

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.kaibo.core.annotation.PoKo

/**
 * 歌单实体
 * 歌单跟歌曲是多对多的关系
 */
@Entity(tableName = "play_list", indices = [Index("play_list_id")])
@PoKo
class PlayListBean(
        //歌单id
        @PrimaryKey
        @ColumnInfo(name = "play_list_id")
        var playListId: String,
        //歌单名
        @ColumnInfo(name = "list_name")
        var listName: String,
        //歌单描述
        @ColumnInfo(name = "desc")
        var desc: String,
        //封面
        @ColumnInfo(name = "image_url")
        var imageUrl: String,
        //更新日期
        @ColumnInfo(name = "update_date")
        var updateDate: Long,
        //创建日期
        @ColumnInfo(name = "create_date")
        var createDate: Long
) : Parcelable {

    @Ignore
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readLong()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(playListId)
        parcel.writeString(listName)
        parcel.writeString(desc)
        parcel.writeString(imageUrl)
        parcel.writeLong(updateDate)
        parcel.writeLong(createDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlayListBean> {
        override fun createFromParcel(parcel: Parcel): PlayListBean {
            return PlayListBean(parcel)
        }

        override fun newArray(size: Int): Array<PlayListBean?> {
            return arrayOfNulls(size)
        }

        const val PLAYLIST_LOVE_ID = "playlist_love_id"
        const val PLAYLIST_QUEUE_ID = "playlist_queue_id"
        const val PLAYLIST_HISTORY_ID = "playlist_history_id"

        fun love(): PlayListBean {
            return PlayListBean(playListId = PLAYLIST_LOVE_ID,
                    listName = "我的收藏",
                    desc = "我的收藏",
                    imageUrl = "",
                    updateDate = 0L,
                    createDate = 0L)
        }

        fun queue(): PlayListBean {
            return PlayListBean(
                    playListId = PLAYLIST_QUEUE_ID,
                    listName = "正在播放",
                    desc = "正在播放",
                    imageUrl = "",
                    updateDate = 0L,
                    createDate = 0L)
        }

        fun history(): PlayListBean {
            return PlayListBean(
                    playListId = PLAYLIST_HISTORY_ID,
                    listName = "播放历史",
                    desc = "播放历史",
                    imageUrl = "",
                    updateDate = 0L,
                    createDate = 0L)
        }
    }
}
