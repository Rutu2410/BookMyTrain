package com.example.trainn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trainn.Station
import java.text.SimpleDateFormat
import java.util.Locale

class TrainStationAdapter(private val stationList: MutableList<Station>) : RecyclerView.Adapter<TrainStationAdapter.RouteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_station_order, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val station = stationList[position]
        holder.stationName.text = station.stationName
        holder.stationCode.text = station.stationCode
        holder.departureTime.text = formatTimeToAMPM(station.time) // Convert to AM/PM
    }

    override fun getItemCount(): Int = stationList.size

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stationName: TextView = itemView.findViewById(R.id.stationNameTextView)
        val stationCode: TextView = itemView.findViewById(R.id.stationCodeTextView)
        val departureTime: TextView = itemView.findViewById(R.id.timeTextView)
    }

    // Function to convert 24-hour time to 12-hour AM/PM format
    private fun formatTimeToAMPM(time: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()) // 24-hour format
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format with AM/PM
            val date = inputFormat.parse(time) // Parse the original time
            outputFormat.format(date!!) // Format into AM/PM
        } catch (e: Exception) {
            time // Return original if conversion fails
        }
    }
}
