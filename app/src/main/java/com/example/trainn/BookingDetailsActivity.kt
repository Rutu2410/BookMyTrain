package com.example.trainn

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class BookingDetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingAdapter: BookingAdapter
    private val bookingsList: MutableList<Booking> = mutableListOf()
    private lateinit var BASE_URL: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_details)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookingAdapter = BookingAdapter(this, bookingsList)
        recyclerView.adapter = bookingAdapter

        BASE_URL = getString(R.string.base_url)
        fetchBookingsFromDatabase()
    }

    private fun fetchBookingsFromDatabase() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$BASE_URL/fetch_bookings.php"
        val requestQueue = Volley.newRequestQueue(this)

        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                try {
                    val bookingsArray = response.getJSONArray("bookings")
                    bookingsList.clear()

                    for (i in 0 until bookingsArray.length()) {
                        val bookingObject = bookingsArray.getJSONObject(i)

                        // Format passenger details properly
                        val passengerDetailsRaw = bookingObject.getString("passenger_details")
                        val passengerDetailsFormatted = formatPassengerDetails(passengerDetailsRaw)

                        val booking = Booking(
                            bookingObject.getInt("id"),
                            bookingObject.getString("train_name"),
                            bookingObject.getString("train_number"),
                            bookingObject.getString("departure_station"),
                            bookingObject.getString("arrival_station"),
                            bookingObject.getString("departure_time"),
                            bookingObject.getString("arrival_time"),
                            bookingObject.getString("duration"),
                            bookingObject.getString("seat_type"),
                            bookingObject.getDouble("total_price").toString(),
                            bookingObject.getString("selected_date"),
                            passengerDetailsFormatted // Properly formatted passenger details
                        )
                        bookingsList.add(booking)
                    }
                    bookingAdapter.notifyDataSetChanged()

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "not book any ticket", Toast.LENGTH_SHORT).show()
                    Log.e("BookingError", "JSON Parsing Error: ${e.message}")
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch bookings: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("BookingError", "Error: ${error.toString()}")
            }
        )

        requestQueue.add(request)
    }

    // Function to format passenger details properly
    private fun formatPassengerDetails(passengerDetailsRaw: String): String {
        return try {
            val passengers = mutableListOf<String>()
            val pattern = Regex("\\{[^}]+\\}")
            val matches = pattern.findAll(passengerDetailsRaw)

            for (match in matches) {
                val passengerObject = JSONObject(match.value)
                val name = passengerObject.getString("name")
                val gender = passengerObject.getString("gender")
                val age = passengerObject.getInt("age")
                val seatNumber = passengerObject.getString("seat_number")

                // Format: "Name (Gender) [Age]    Seat Number"
                val formattedPassenger = "$name ($gender) [$age]".padEnd(25) + seatNumber
                passengers.add(formattedPassenger)
            }

            passengers.joinToString("\n") // Each passenger on a new line
        } catch (e: JSONException) {
            e.printStackTrace()
            "Invalid passenger details"
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }

}