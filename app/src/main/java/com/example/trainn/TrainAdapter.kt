package com.example.trainn

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TrainAdapter(private val trainList: List<Train>) : RecyclerView.Adapter<TrainAdapter.TrainViewHolder>() {

    class TrainViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trainName: TextView = view.findViewById(R.id.trainName)
        val departureStation: TextView = view.findViewById(R.id.departureStation)
        val arrivalStation: TextView = view.findViewById(R.id.arrivalStation)
        val departureTime: TextView = view.findViewById(R.id.departureTime)
        val arrivalTime: TextView = view.findViewById(R.id.arrivalTime)
        val duration: TextView = view.findViewById(R.id.duration)
        val bookNowButton: Button = view.findViewById(R.id.btnBookNow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_train, parent, false)
        return TrainViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int) {
        val train = trainList[position]
        val context = holder.itemView.context

        // Format the departure time, arrival time, and duration
        val departureTimeFormatted = convertTo12HourFormat(train.departureTime)
        val arrivalTimeFormatted = convertTo12HourFormat(train.arrivalTime)
        val durationFormatted = formatDuration(train.departureTime, train.arrivalTime)

        // Set values to TextViews
        holder.trainName.text = "${train.trainName} (${train.trainNumber})"
        holder.departureStation.text = train.departureStation
        holder.arrivalStation.text = train.arrivalStation
        holder.departureTime.text = "$departureTimeFormatted  "
        holder.arrivalTime.text = "  $arrivalTimeFormatted"
        holder.duration.text = "------$durationFormatted------"

        // Set click listener for train item
        holder.itemView.setOnClickListener {
            val intent = Intent(context, TrainStationActivity::class.java).apply {
                putExtra("trainId", train.trainId)
                putExtra("trainName", train.trainName)
            }
            Log.d("TrainAdapter", "Navigating to TrainStationActivity with trainId: ${train.trainId}")
            context.startActivity(intent)
        }

        holder.bookNowButton.setOnClickListener {
            val intent = Intent(context, TrainBookingActivity::class.java).apply {
                putExtra("trainName", train.trainName)
                putExtra("trainNumber", train.trainNumber)
                putExtra("departureStation", train.departureStation)
                putExtra("arrivalStation", train.arrivalStation)
                putExtra("departureTime", departureTimeFormatted)
                putExtra("arrivalTime", arrivalTimeFormatted)
                putExtra("duration", durationFormatted)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = trainList.size

    // Convert time to 12-hour format (e.g., 14:30 → 02:30 PM)
    private fun convertTo12HourFormat(time: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // 24-hour format
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format with AM/PM
            val date = inputFormat.parse(time)
            outputFormat.format(date ?: time)
        } catch (e: Exception) {
            time // Return original time if parsing fails
        }
    }

    // Calculate duration between departure and arrival times
    private fun formatDuration(departure: String, arrival: String): String {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val depTime = format.parse(departure)
            val arrTime = format.parse(arrival)

            if (depTime != null && arrTime != null) {
                var durationMillis = arrTime.time - depTime.time

                // Handle cases where arrival is past midnight (next day)
                if (durationMillis < 0) {
                    durationMillis += TimeUnit.DAYS.toMillis(1)
                }

                val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60

                "${hours}h ${minutes}m"
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }
}
