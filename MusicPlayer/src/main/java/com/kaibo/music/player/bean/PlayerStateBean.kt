package com.kaibo.music.player.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * @author kaibo
 * @date 2019/4/3 10:07
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

data class PlayerStateBean(
        val isPlaying: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isPlaying) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlayerStateBean> {
        override fun createFromParcel(parcel: Parcel): PlayerStateBean {
            return PlayerStateBean(parcel)
        }

        override fun newArray(size: Int): Array<PlayerStateBean?> {
            return arrayOfNulls(size)
        }
    }

}