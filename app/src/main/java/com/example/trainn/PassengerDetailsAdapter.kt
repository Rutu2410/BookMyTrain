package com.example.trainn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PassengerDetailsAdapter(private val passengers: List<Passenger>) :
    RecyclerView.Adapter<PassengerDetailsAdapter.PassengerViewHolder>() {

    class PassengerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val passengerIdTextView: TextView = itemView.findViewById(R.id.passengerNumberTextView)
        val passengerNameTextView: TextView = itemView.findViewById(R.id.passengerNameTextView)
        val passengerAgeTextView: TextView = itemView.findViewById(R.id.passengerAgeTextView)
        val passengerGenderTextView: TextView = itemView.findViewById(R.id.passengerGenderTextView)
        val seatNumberTextView: TextView = itemView.findViewById(R.id.passengerSeatTextView) // ✅ Added seat number
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PassengerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.passenger_item_details, parent, false) // ✅ Ensure layout has seatTextView
        return PassengerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PassengerViewHolder, position: Int) {
        val passenger = passengers[position]
        holder.passengerIdTextView.text = "${passenger.id}." // ✅ Display Passenger ID
        holder.passengerNameTextView.text = "${passenger.name}"
        holder.passengerAgeTextView.text = "(${passenger.age})"
        holder.passengerGenderTextView.text = "(${passenger.gender})"
        holder.seatNumberTextView.text = "${passenger.seatNumber}" // ✅ Display Seat Number
    }

    override fun getItemCount() = passengers.size
}
