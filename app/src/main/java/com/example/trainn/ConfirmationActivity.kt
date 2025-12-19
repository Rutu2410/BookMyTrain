package com.example.trainn

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request.*
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat

class ConfirmationActivity : AppCompatActivity() {

    private lateinit var trainNameTextView: TextView
    private lateinit var trainNumberTextView: TextView
    private lateinit var departureStationTextView: TextView
    private lateinit var arrivalStationTextView: TextView
    private lateinit var departureTimeTextView: TextView
    private lateinit var arrivalTimeTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var seatTypeTextView: TextView
    private lateinit var seatPriceTextView: TextView
    private lateinit var availableSeatsTextView: TextView
    private lateinit var selectedDateTextView: TextView
    private lateinit var totalPriceTextView: TextView
    private lateinit var passengersRecyclerView: RecyclerView
    private lateinit var payButton: Button
    private var generatedOTP: String = ""
    private var totalPrice: Double = 0.0
    private lateinit var passengers: ArrayList<Passenger>
    private lateinit var BASE_URL: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)
        loadUserData()

        // Initialize UI components
        trainNameTextView = findViewById(R.id.trainNameTextView)
        trainNumberTextView = findViewById(R.id.trainNumberTextView)
        departureStationTextView = findViewById(R.id.departureStationTextView)
        arrivalStationTextView = findViewById(R.id.arrivalStationTextView)
        departureTimeTextView = findViewById(R.id.departureTimeTextView)
        arrivalTimeTextView = findViewById(R.id.arrivalTimeTextView)
        durationTextView = findViewById(R.id.durationTextView)
        seatTypeTextView = findViewById(R.id.seatTypeTextView)
        seatPriceTextView = findViewById(R.id.seatPriceTextView)
        availableSeatsTextView = findViewById(R.id.availableSeatsTextView)
        selectedDateTextView = findViewById(R.id.selectedDateTextView)
        totalPriceTextView = findViewById(R.id.totalPriceTextView)
        passengersRecyclerView = findViewById(R.id.passengersRecyclerView)
        payButton = findViewById(R.id.payButton)
        BASE_URL = getString(R.string.base_url)


        // Retrieve data from intent
        val trainName = intent.getStringExtra("trainName") ?: "N/A"
        val trainNumber = intent.getStringExtra("trainNumber") ?: "N/A"
        val departureStation = intent.getStringExtra("departureStation") ?: "N/A"
        val arrivalStation = intent.getStringExtra("arrivalStation") ?: "N/A"
        val departureTime = intent.getStringExtra("departureTime") ?: "N/A"
        val arrivalTime = intent.getStringExtra("arrivalTime") ?: "N/A"
        val duration = intent.getStringExtra("duration") ?: "N/A"
        val seatType = intent.getStringExtra("seatType") ?: "N/A"
        val seatPrice = intent.getStringExtra("seatPrice") ?: "₹0"
        val availableSeats = intent.getStringExtra("availableSeats") ?: "N/A"
        val selectedDate = intent.getStringExtra("selectedDate") ?: "N/A"

        // Get passengers safely
        passengers = intent.getParcelableArrayListExtra("passengers") ?: arrayListOf()

        // Calculate total price
        val pricePerSeat = seatPrice.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
        totalPrice = pricePerSeat * passengers.size
        val decimalFormat = DecimalFormat("#,##0.00")
        totalPriceTextView.text = "Total Price: ₹${decimalFormat.format(totalPrice)}"

        // Set retrieved data to UI
        trainNameTextView.text = "$trainName"
        trainNumberTextView.text = "$trainNumber"
        departureStationTextView.text = "$departureStation  "
        arrivalStationTextView.text = "   $arrivalStation"
        departureTimeTextView.text = "$departureTime"
        arrivalTimeTextView.text = "$arrivalTime"
        durationTextView.text = "-----$duration-----"
        seatTypeTextView.text = "$seatType"
        seatPriceTextView.text = " $seatPrice"
        availableSeatsTextView.text = "($availableSeats)"
        selectedDateTextView.text = "$selectedDate"

        // Setup RecyclerView
        passengersRecyclerView.layoutManager = LinearLayoutManager(this)
        passengersRecyclerView.adapter = PassengerDetailsAdapter(passengers)

        payButton.setOnClickListener {
            showPaymentDialog()
        }
    }
    private fun showPaymentDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_payment)

        val paymentSpinner = dialog.findViewById<Spinner>(R.id.paymentMethodSpinner)
        val pinEditText = dialog.findViewById<EditText>(R.id.pinEditText)
        val generateOtpButton = dialog.findViewById<Button>(R.id.generateOtpButton)
        val payButton = dialog.findViewById<Button>(R.id.payButton)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val totalPriceTextView = dialog.findViewById<TextView>(R.id.totalPriceTextView)

        // Set the total price dynamically
        totalPriceTextView.text = "₹ $totalPrice"

        val paymentMethods = arrayOf("UPI", "Paytm")
        paymentSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paymentMethods)

        generateOtpButton.setOnClickListener {
            generatedOTP = (1000..9999).random().toString()
            Toast.makeText(this, "Your OTP: $generatedOTP", Toast.LENGTH_LONG).show()
        }

        payButton.setOnClickListener {
            val enteredOTP = pinEditText.text.toString()
            if (enteredOTP == generatedOTP) {
                Toast.makeText(this, "Payment of ₹$totalPrice successful!", Toast.LENGTH_LONG).show()
                storeBookingInDatabase()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Invalid OTP! Try again.", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadUserData() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val userId = sharedPreferences.getInt("userId", -1)
        val username = sharedPreferences.getString("username", "Guest")

        Log.d("UserData", "userId: $userId, isLoggedIn: $isLoggedIn")

        if (isLoggedIn && userId != -1) {
            Toast.makeText(this, "Welcome $username!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateSeatAvailability() {
        val url = "$BASE_URL/reducing_seat.php"

        val trainNumber = trainNumberTextView.text.toString().replace("Train Number: ", "").trim()
        val departureStation = departureStationTextView.text.toString().replace("From: ", "").trim()
        val arrivalStation = arrivalStationTextView.text.toString().replace("To: ", "").trim()
        val seatType = seatTypeTextView.text.toString().replace("Seat Type: ", "").trim().lowercase()
        val selectedDate = selectedDateTextView.text.toString().replace("Date: ", "").trim()
        val passengerCount = passengers.size.coerceAtLeast(1) // Ensure at least one passenger

        Log.d("SEAT_UPDATE", "Preparing request: Train: $trainNumber, From: $departureStation, To: $arrivalStation, Seat Type: $seatType, Date: $selectedDate, Count: $passengerCount")

        try {
            val jsonBody = JSONObject().apply {
                put("train_number", trainNumber)
                put("departure_station", departureStation)
                put("arrival_station", arrivalStation)
                put("seat_type", seatType)
                put("selected_date", selectedDate)
                put("passenger_count", passengerCount)
            }

            Log.d("JSON_BODY", "Sending: $jsonBody")

            val requestQueue = Volley.newRequestQueue(this)

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonBody,
                { response ->
                    try {
                        Log.d("SEAT_RESPONSE", response.toString())

                        val status = response.optString("status", "fail")
                        val message = response.optString("message", "Unknown error")

                        if (status == "success") {
                            val remainingSeats = response.optInt("remaining_seats", -1)
                            Toast.makeText(
                                this,
                                "Seats Updated Successfully! Remaining seats: $remainingSeats",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(this, "Seat Update Failed: $message", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        Log.e("SEAT_ERROR", "Parsing error: ${e.message}")
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    val responseBody = error.networkResponse?.data?.toString(Charsets.UTF_8) ?: "No Response"
                    Log.e("VOLLEY_ERROR", "Error: ${error.message}, Response: $responseBody")
                    Toast.makeText(this, "Error updating seats: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    return hashMapOf("Content-Type" to "application/json")
                }
            }

            requestQueue.add(jsonObjectRequest)
        } catch (e: JSONException) {
            Log.e("JSON_ERROR", "Error creating JSON: ${e.message}")
            Toast.makeText(this, "Failed to create request data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeBookingInDatabase() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)

        Log.d("BookingData", "userId in booking: $userId") // Debug log

        if (userId == -1) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$BASE_URL/store_booking.php"
        val requestQueue = Volley.newRequestQueue(this)

        val jsonObject = JSONObject().apply {
            put("user_id", userId)
            put("train_name", trainNameTextView.text.toString().replace("Train Name: ", ""))
            put("train_number", trainNumberTextView.text.toString().replace("Train Number: ", ""))
            put("departure_station", departureStationTextView.text.toString().replace("From: ", ""))
            put("arrival_station", arrivalStationTextView.text.toString().replace("To: ", ""))
            put("departure_time", departureTimeTextView.text.toString().replace("Departure: ", ""))
            put("arrival_time", arrivalTimeTextView.text.toString().replace("Arrival: ", ""))
            put("duration", durationTextView.text.toString().replace("Duration: ", ""))
            put("seat_type", seatTypeTextView.text.toString().replace("Seat Type: ", ""))
            put("total_price", totalPrice)
            put("selected_date", selectedDateTextView.text.toString().replace("Date: ", ""))
            put("passenger_details", Gson().toJson(passengers))
        }

        val request = JsonObjectRequest(
            Method.POST, url, jsonObject,
            { response ->
                Toast.makeText(this, "Booking stored successfully!", Toast.LENGTH_SHORT).show()
                updateSeatAvailability()
                val intent = Intent(this,BookingDetailsActivity::class.java)
                startActivity(intent)
            },
            { error ->
                Toast.makeText(this, "Failed to store booking: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("BookingError", "Error: ${error.toString()}") // Log the error
            }
        )

        requestQueue.add(request)
    }

}
