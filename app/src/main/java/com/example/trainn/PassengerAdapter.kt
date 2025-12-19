package com.example.trainn

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PassengerAdapter(private val passengers: MutableList<Passenger>) :
    RecyclerView.Adapter<PassengerAdapter.PassengerViewHolder>() {

    class PassengerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val passengerNumber: TextView = itemView.findViewById(R.id.passengerNumberTextView)
        val passengerName: TextView = itemView.findViewById(R.id.passengerNameTextView)
        val passengerAge: TextView = itemView.findViewById(R.id.passengerAgeTextView)
        val passengerGender: TextView = itemView.findViewById(R.id.passengerGenderTextView)
        val editButton: ImageView= itemView.findViewById(R.id.editPassengerButton)
        val deleteButton:ImageView = itemView.findViewById(R.id.deletePassengerButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PassengerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.passenger_item, parent, false)
        return PassengerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PassengerViewHolder, position: Int) {
        val passenger = passengers[position]
        holder.passengerNumber.text = "${passenger.id}."
        holder.passengerName.text = " ${passenger.name}"
        holder.passengerAge.text = "(${passenger.age})"
        holder.passengerGender.text = "(${passenger.gender})"

        holder.editButton.setOnClickListener {
            (holder.itemView.context as TrainBookingActivity).showAddPassengerDialog(passenger, position)
        }

        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Passenger")
                .setMessage("Are you sure you want to delete this passenger?")
                .setPositiveButton("Yes") { dialog, _ ->
                    passengers.removeAt(position)
                    notifyItemRemoved(position)
                    updatePassengerNumbers()
                    dialog.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun getItemCount() = passengers.size

    private fun updatePassengerNumbers() {
        for (i in passengers.indices) {
            passengers[i] = passengers[i].copy(id = (i + 1).toString())
        }
        notifyDataSetChanged()
    }
}
