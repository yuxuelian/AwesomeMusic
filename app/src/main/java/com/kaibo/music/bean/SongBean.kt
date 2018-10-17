package com.kaibo.music.bean

import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo

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
) {
    /**
     * 歌曲的播放时长
     */
    var duration: Int = 0
}