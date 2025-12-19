package com.example.trainn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Bookingadpaterall(private val bookingList: List<Booking>) :
    RecyclerView.Adapter<Bookingadpaterall.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookingId: TextView = itemView.findViewById(R.id.BookingId)
        val trainName: TextView = itemView.findViewById(R.id.trainName)
        val trainNumber: TextView = itemView.findViewById(R.id.trainNumber)
        val departureStation: TextView = itemView.findViewById(R.id.departureStation)
        val arrivalStation: TextView = itemView.findViewById(R.id.arrivalStation)
        val departureTime: TextView = itemView.findViewById(R.id.departureTime)
        val arrivalTime: TextView = itemView.findViewById(R.id.arrivalTime)
        val duration: TextView = itemView.findViewById(R.id.duration)
        val seatType: TextView = itemView.findViewById(R.id.seatType)
        val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        val selectedDate: TextView = itemView.findViewById(R.id.selectedDate)
        val passengerDetails: TextView = itemView.findViewById(R.id.passengerDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]
        holder.bookingId.text = "Booking ID: ${booking.id}"  // Added Booking ID
        holder.trainName.text = booking.trainName
        holder.trainNumber.text = "${booking.trainNumber}"
        holder.departureStation.text = "${booking.departureStation}"
        holder.arrivalStation.text = "${booking.arrivalStation}"
        holder.departureTime.text = "${booking.departureTime}"
        holder.arrivalTime.text = "${booking.arrivalTime}"
        holder.duration.text = "${booking.duration}"
        holder.seatType.text = "${booking.seatType}"
        holder.totalPrice.text = "₹${booking.totalPrice}"
        holder.selectedDate.text = "${booking.selectedDate}"
        holder.passengerDetails.text = "${booking.passengerDetails}"
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }
}
