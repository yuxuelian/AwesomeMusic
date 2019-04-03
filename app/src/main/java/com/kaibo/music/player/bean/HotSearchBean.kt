package com.kaibo.music.player.bean

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kaibo.core.annotation.PoKo


/**
 * @author kaibo
 * @createDate 2018/10/15 10:38
 * @GitHub：https://github.com/yuxuelian
 * @email：kaibo1hao@gmail.com
 * @description：
 */

@PoKo
data class HotSearchBean(
        @SerializedName("k") val searchKey: String,
        @SerializedName("n") val searchNo: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(searchKey)
        parcel.writeInt(searchNo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HotSearchBean> {
        override fun createFromParcel(parcel: Parcel): HotSearchBean {
            return HotSearchBean(parcel)
        }

        override fun newArray(size: Int): Array<HotSearchBean?> {
            return arrayOfNulls(size)
        }
    }
}