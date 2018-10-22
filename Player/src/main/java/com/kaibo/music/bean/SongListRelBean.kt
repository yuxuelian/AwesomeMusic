package com.kaibo.music.bean

import androidx.room.*
import com.kaibo.core.annotation.PoKo

/**
 * @author kaibo
 * @date 2018/10/22 11:57
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 * 跟单跟歌曲的关联表
 */

@Entity(tableName = "song_list_rel",
        indices = [Index(value = ["mid", "play_list_id"], unique = true), Index(value = ["mid"]), Index(value = ["play_list_id"])],
        foreignKeys = [
            ForeignKey(entity = SongBean::class, parentColumns = ["mid"], childColumns = ["mid"], onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = PlayListBean::class, parentColumns = ["play_list_id"], childColumns = ["play_list_id"], onDelete = ForeignKey.CASCADE)
        ])
@PoKo
class SongListRelBean(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        var id: Long,
        /**
         * 歌曲id
         */
        @ColumnInfo(name = "mid")
        var mid: String,
        /**
         * 歌单id
         */
        @ColumnInfo(name = "play_list_id")
        var playListId: String
)
