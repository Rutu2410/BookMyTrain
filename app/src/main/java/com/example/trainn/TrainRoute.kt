package com.example.trainn
import android.os.Parcel
import android.os.Parcelable

data class TrainRoute(
    val id: Int,
    val trainId: Int,
    val trainNumber: String,
    val trainName: String,
    val departureStationName: String,
    val departureStationCode: String,
    val arrivalStationName: String,
    val arrivalStationCode: String,
    val departureTime: String,
    val arrivalTime: String,
    val duration: String,
    val price: Double,
    val sleeperPrice: Double,
    val acPrice: Double,
    val secondSeatingPrice: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(trainId)
        parcel.writeString(trainNumber)
        parcel.writeString(trainName)
        parcel.writeString(departureStationName)
        parcel.writeString(departureStationCode)
        parcel.writeString(arrivalStationName)
        parcel.writeString(arrivalStationCode)
        parcel.writeString(departureTime)
        parcel.writeString(arrivalTime)
        parcel.writeString(duration)
        parcel.writeDouble(price)
        parcel.writeDouble(sleeperPrice)
        parcel.writeDouble(acPrice)
        parcel.writeDouble(secondSeatingPrice)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TrainRoute> {
        override fun createFromParcel(parcel: Parcel): TrainRoute {
            return TrainRoute(parcel)
        }

        override fun newArray(size: Int): Array<TrainRoute?> {
            return arrayOfNulls(size)
        }
    }
}
