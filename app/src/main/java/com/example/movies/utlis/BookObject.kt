package com.example.movies.utlis

import android.os.Parcel
import android.os.Parcelable

data class BookObject(
    var bookTitle: String? = "",
    var bookAuthor: String? = "",
    var readed:Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bookTitle)
        parcel.writeString(bookAuthor)
        parcel.writeByte(if (readed) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BookObject> {
        override fun createFromParcel(parcel: Parcel): BookObject {
            return BookObject(parcel)
        }

        override fun newArray(size: Int): Array<BookObject?> {
            return arrayOfNulls(size)
        }
    }
}
