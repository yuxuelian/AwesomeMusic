package com.kaibo.music.bean

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo

/**
 * @author kaibo
 * @date 2018/10/17 14:42
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class SingerBean(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("name") val name: String,
        @SerializedName("avatar") val avatar: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(name)
        parcel.writeString(avatar)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SingerBean> {
        override fun createFromParcel(parcel: Parcel): SingerBean {
            return SingerBean(parcel)
        }

        override fun newArray(size: Int): Array<SingerBean?> {
            return arrayOfNulls(size)
        }
    }
}