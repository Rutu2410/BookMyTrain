package com.example.trainn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrainRouteAdapter(private val trainRoutes: List<TrainRoute>,
                        private val onEditClick: (TrainRoute) -> Unit,
                        private val onDeleteClick: (Int) -> Unit) :
    RecyclerView.Adapter<TrainRouteAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTrainName: TextView = itemView.findViewById(R.id.tvTrainName)
        val tvTrainNumber: TextView = itemView.findViewById(R.id.tvTrainNumber)
        val tvDeparture: TextView = itemView.findViewById(R.id.tvDeparture)
        val tvArrival: TextView = itemView.findViewById(R.id.tvArrival)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvSleeperPrice: TextView = itemView.findViewById(R.id.tvSleeperPrice)
        val tvAcPrice: TextView = itemView.findViewById(R.id.tvAcPrice)
        val tvSecondSeatingPrice: TextView = itemView.findViewById(R.id.tvSecondSeatingPrice)
        val ivEdit: ImageView = itemView.findViewById(R.id.ivEdit)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_train_route, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val route = trainRoutes[position]
        holder.tvTrainName.text = "${route.trainName}"
        holder.tvTrainNumber.text = "${route.trainNumber}"
        holder.tvDeparture.text = "${route.departureStationName} (${route.departureStationCode})"
        holder.tvArrival.text = "${route.arrivalStationName} (${route.arrivalStationCode})"
        holder.tvTime.text = "${route.departureTime}            ${route.arrivalTime}"
        holder.tvDuration.text = "-----${route.duration}-----"
        holder.tvPrice.text = "General: ₹${route.price}"
        holder.tvSleeperPrice.text = "Sleeper: ₹${route.sleeperPrice}"
        holder.tvAcPrice.text = "AC: ₹${route.acPrice}"
        holder.tvSecondSeatingPrice.text = "Second Seating: ₹${route.secondSeatingPrice}"
        // Edit Click
        holder.ivEdit.setOnClickListener { onEditClick(route) }

        // Delete Click
        holder.ivDelete.setOnClickListener { onDeleteClick(route.id) }
    }

    override fun getItemCount(): Int = trainRoutes.size
}
