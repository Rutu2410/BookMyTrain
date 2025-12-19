package com.example.trainn

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class BookingAdapter(
    private val context: Context,
    private val bookings: MutableList<Booking>
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_item, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trainName: TextView = itemView.findViewById(R.id.trainName)
        private val trainNumber: TextView = itemView.findViewById(R.id.trainNumber)
        private val departureStation: TextView = itemView.findViewById(R.id.departureStation)
        private val arrivalStation: TextView = itemView.findViewById(R.id.arrivalStation)
        private val departureTime: TextView = itemView.findViewById(R.id.departureTime)
        private val arrivalTime: TextView = itemView.findViewById(R.id.arrivalTime)
        private val seatType: TextView = itemView.findViewById(R.id.seatType)
        private val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        private val selectedDate: TextView = itemView.findViewById(R.id.selectedDate)
        private val duration: TextView = itemView.findViewById(R.id.duration)
        private val passengerDetails: TextView = itemView.findViewById(R.id.passengerDetails)
        private val cancelButton: Button = itemView.findViewById(R.id.btnCancel)

        fun bind(booking: Booking) {
            trainName.text = booking.trainName
            trainNumber.text = booking.trainNumber
            departureStation.text = booking.departureStation
            arrivalStation.text = booking.arrivalStation
            departureTime.text = booking.departureTime
            arrivalTime.text = booking.arrivalTime
            seatType.text = booking.seatType
            totalPrice.text = "₹${booking.totalPrice}"
            selectedDate.text = booking.selectedDate
            duration.text = booking.duration
            passengerDetails.text = booking.passengerDetails

            cancelButton.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Cancel Booking")
                    .setMessage("Are you sure you want to cancel this booking?")
                    .setPositiveButton("Yes") { _, _ ->
                        cancelBooking(booking, adapterPosition)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        private fun cancelBooking(booking: Booking, position: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val urlString = "http://192.168.48.246/database1/cancelBooking.php"
                    val url = URL(urlString)
                    val postData = "booking_id=" + URLEncoder.encode(booking.id.toString(), "UTF-8")

                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                    conn.outputStream.use { it.write(postData.toByteArray()) }

                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    withContext(Dispatchers.Main) {
                        if (jsonResponse.getBoolean("success")) {
                            bookings.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, bookings.size)
                            Toast.makeText(
                                context,
                                jsonResponse.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Error: ${jsonResponse.getString("message")}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: JSONException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Invalid server response", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: java.net.UnknownHostException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Unable to reach server", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
