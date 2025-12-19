package com.example.trainn

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

class AdminActivity : AppCompatActivity() {

    private lateinit var BASE_URL: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_details)
        BASE_URL = getString(R.string.base_url)

        fetchBookingsFromDatabase() // Fetch all bookings
    }

    private fun fetchBookingsFromDatabase() {
        val url = "$BASE_URL/fetch_bookings_all.php" // Fetches all bookings
        val requestQueue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val bookingsArray = response.getJSONArray("bookings")
                    val bookingList = mutableListOf<Booking>()

                    for (i in 0 until bookingsArray.length()) {
                        val bookingObject = bookingsArray.getJSONObject(i)
                        val passengerDetailsRaw =
                            bookingObject.getJSONArray("passenger_details").toString()
                        val passengerDetailsFormatted = formatPassengerDetails(passengerDetailsRaw)

                        val booking = Booking(
                            bookingObject.getInt("id"), // Booking ID
                            bookingObject.getString("train_name"),
                            bookingObject.getString("train_number"),
                            bookingObject.getString("departure_station"),
                            bookingObject.getString("arrival_station"),
                            bookingObject.getString("departure_time"),
                            bookingObject.getString("arrival_time"),
                            bookingObject.getString("duration"),
                            bookingObject.optString("seat_type", ""),
                            bookingObject.getDouble("total_price").toString(),
                            bookingObject.getString("selected_date"),
                            passengerDetailsFormatted
                        )
                        bookingList.add(booking)
                    }
                    setupRecyclerView(bookingList)

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this,
                        "Failed to fetch bookings: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("BookingError", "JSON Parsing Error: ${e.message}")
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Failed to fetch bookings: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("BookingError", "Error: ${error.toString()}")
            }
        )

        requestQueue.add(request)
    }

    private fun formatPassengerDetails(passengerDetailsRaw: String): String {
        return try {
            val passengers = mutableListOf<String>()
            val jsonArray = JSONArray(passengerDetailsRaw)

            for (i in 0 until jsonArray.length()) {
                val passengerObject = jsonArray.getJSONObject(i)
                val name = passengerObject.getString("name")
                val gender = passengerObject.getString("gender")
                val age = passengerObject.getInt("age")
                val seatNumber = passengerObject.getString("seat_number")

                passengers.add("$name ($gender, Age: $age, Seat: $seatNumber)")
            }
            passengers.joinToString(" | ")
        } catch (e: JSONException) {
            "No passenger details available"
        }
    }

    private fun setupRecyclerView(bookingList: List<Booking>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = Bookingadpaterall(bookingList)
    }
}
