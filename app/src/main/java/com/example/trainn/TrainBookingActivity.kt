package com.example.trainn

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley


class TrainBookingActivity : AppCompatActivity() {

    private lateinit var trainNameTextView: TextView
    private lateinit var trainNumberTextView: TextView
    private lateinit var departureStationTextView: TextView
    private lateinit var arrivalStationTextView: TextView
    private lateinit var departureTimeTextView: TextView
    private lateinit var arrivalTimeTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var sleeperPriceTextView: TextView
    private lateinit var sleeperSeatsTextView: TextView
    private lateinit var acPriceTextView: TextView
    private lateinit var acSeatsTextView: TextView
    private lateinit var secondSeatingPriceTextView: TextView
    private lateinit var secondSeatingSeatsTextView: TextView
    private lateinit var BASE_URL: String


    // UI components for passenger-related information
    private lateinit var addPassengerButton: Button
    private lateinit var proceedButton: Button

    private var passengerCount = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PassengerAdapter
    private val passengers = mutableListOf<Passenger>()

    // UI components for dates
    private lateinit var dateRecyclerView: RecyclerView
    private lateinit var dateAdapter: DateAdapter
    private var selectedDate: String = ""
    private var selectedCardView: CardView? = null
    private val dateList = mutableListOf<DateModel>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainbooking)

        // Initialize Train UI components
        trainNameTextView = findViewById(R.id.trainNameTextView)
        trainNumberTextView = findViewById(R.id.trainNumberTextView)
        departureStationTextView = findViewById(R.id.departureStationTextView)
        arrivalStationTextView = findViewById(R.id.arrivalStationTextView)
        departureTimeTextView = findViewById(R.id.departureTimeTextView)
        arrivalTimeTextView = findViewById(R.id.arrivalTimeTextView)
        durationTextView = findViewById(R.id.durationTextView)
        BASE_URL = getString(R.string.base_url)



        // Initialize Seat Availability UI components
        sleeperPriceTextView = findViewById(R.id.sleeperPriceTextView)
        sleeperSeatsTextView = findViewById(R.id.sleeperSeatsTextView)
        acPriceTextView = findViewById(R.id.acPriceTextView)
        acSeatsTextView = findViewById(R.id.acSeatsTextView)
        secondSeatingPriceTextView = findViewById(R.id.secondSeatingPriceTextView)
        secondSeatingSeatsTextView = findViewById(R.id.secondSeatingSeatsTextView)
        proceedButton = findViewById(R.id.proceedButton)

        recyclerView = findViewById(R.id.passengerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PassengerAdapter(passengers)
        recyclerView.adapter = adapter
        // Fetch the data passed from MainActivity

        val trainName = intent.getStringExtra("trainName") ?: ""
        val trainNumber = intent.getStringExtra("trainNumber") ?: ""
        val departureStation = intent.getStringExtra("departureStation") ?: ""
        val arrivalStation = intent.getStringExtra("arrivalStation") ?: ""
        val departureTime = intent.getStringExtra("departureTime") ?: ""
        val arrivalTime = intent.getStringExtra("arrivalTime") ?: ""
        val duration = intent.getStringExtra("duration") ?: ""

        // Display train details
        trainNameTextView.text = trainName
        trainNumberTextView.text = " ($trainNumber)"
        departureStationTextView.text = "$departureStation"
        arrivalStationTextView.text = "$arrivalStation"
        departureTimeTextView.text = "  $departureTime"
        arrivalTimeTextView.text = "  $arrivalTime"
        durationTextView.text = "----$duration----"

        // Fetch seat availability and prices
        fetchSeatAvailability(trainNumber, departureStation, arrivalStation)

        // Initialize Passenger-related UI components
        addPassengerButton = findViewById(R.id.addPassengerButton)

        addPassengerButton.setOnClickListener {
            val (seatType, _, seatsTextView) = getSeatDetails()
            val availableSeats = seatsTextView.filter { it.isDigit() }.toIntOrNull() ?: 0

            if (passengers.size >= 6) {
                Toast.makeText(this, "Maximum 6 passengers allowed", Toast.LENGTH_SHORT).show()
            } else if (passengers.size < availableSeats) {
                showAddPassengerDialog()
            } else {
                Toast.makeText(this, "No more seats available for $seatType", Toast.LENGTH_SHORT).show()
            }
        }

        proceedButton.setOnClickListener {
            goToBookingSummary()
        }

        // Initialize RecyclerView for dates
        dateRecyclerView = findViewById(R.id.dateRecyclerView)
        dateRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Fetch dates from API
        fetchDatesFromApi()
        setupCardClickListeners()

        dateAdapter = DateAdapter(dateList) { selectedDateModel ->
            selectedDate = selectedDateModel.date  // Store selected date
            Toast.makeText(this, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()
            fetchSeatAvailability(trainNumber, departureStation, arrivalStation)
        }

        dateRecyclerView.adapter = dateAdapter
    }

    private fun setupCardClickListeners() {
        val defaultColor = ContextCompat.getColor(this, R.color.b3)
        val selectedColor = ContextCompat.getColor(this, R.color.b7)


        val sleeperCard = findViewById<CardView>(R.id.sleeperCardView)
        val secondSeatingCard = findViewById<CardView>(R.id.secondSeatingCardView)
        val acCard = findViewById<CardView>(R.id.acCardView)

        val cardViews = listOf(sleeperCard, secondSeatingCard, acCard)

        cardViews.forEach { cardView ->
            cardView.setOnClickListener {
                selectedCardView?.setCardBackgroundColor(defaultColor) // Reset previous selection
                cardView.setCardBackgroundColor(selectedColor) // Highlight selected card
                selectedCardView = cardView // Store current selection
            }
        }
    }

    private fun fetchSeatAvailability(
        trainNumber: String,
        departureStation: String,
        arrivalStation: String
    ) {
        val url = "$BASE_URL/seat_availability.php?train_number=$trainNumber&departure_station=$departureStation&arrival_station=$arrivalStation&date=$selectedDate"

        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                try {
                    if (response.getBoolean("success")) {
                        val trainData = response.getJSONArray("data").getJSONObject(0)

                        val sleeperAvailable = trainData.optInt("sleeper_available", 0)
                        val acAvailable = trainData.optInt("ac_available", 0)
                        val secondSeatingAvailable = trainData.optInt("second_sitting_available", 0)

                        // Update seat prices and availability
                        sleeperPriceTextView.text = "₹${trainData.optString("sleeper_price", "N/A")}"
                        sleeperSeatsTextView.text = "Available: $sleeperAvailable"
                        acPriceTextView.text = "₹${trainData.optString("ac_price", "N/A")}"
                        acSeatsTextView.text = "Available: $acAvailable"
                        secondSeatingPriceTextView.text = "₹${trainData.optString("second_seating_price", "N/A")}"
                        secondSeatingSeatsTextView.text = "Available: $secondSeatingAvailable"

                        // Hide card views if no seats are available
                        val sleeperCard = findViewById<CardView>(R.id.sleeperCardView)
                        val acCard = findViewById<CardView>(R.id.acCardView)
                        val secondSeatingCard = findViewById<CardView>(R.id.secondSeatingCardView)

                        sleeperCard.visibility = if (sleeperAvailable > 0) View.VISIBLE else View.GONE
                        acCard.visibility = if (acAvailable > 0) View.VISIBLE else View.GONE
                        secondSeatingCard.visibility = if (secondSeatingAvailable > 0) View.VISIBLE else View.GONE

                    } else {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        queue.add(jsonObjectRequest)
    }


    // Fetch dates from API and update RecyclerView
    private fun fetchDatesFromApi() {
        val url =
            "$BASE_URL/get_dates.php"

        val requestQueue = Volley.newRequestQueue(this)
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                dateList.clear()  // Clear previous data

                // Parse the API response and populate the date list
                for (i in 0 until response.length()) {
                    val dateString = response.getJSONObject(i).getString("date")
                    dateList.add(DateModel(dateString))
                }

                // Notify the adapter that data has changed
                dateAdapter.notifyDataSetChanged()
            },
            { error ->
                Log.e("API_ERROR", error.toString())
            })

        requestQueue.add(jsonArrayRequest)
    }


    fun showAddPassengerDialog(passenger: Passenger? = null, position: Int? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_passenger, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val ageEditText = dialogView.findViewById<EditText>(R.id.ageEditText)
        val genderRadioGroup = dialogView.findViewById<RadioGroup>(R.id.genderRadioGroup)

        passenger?.let {
            nameEditText.setText(it.name)
            ageEditText.setText(it.age)
            when (it.gender) {
                "Male" -> genderRadioGroup.check(R.id.maleRadioButton)
                "Female" -> genderRadioGroup.check(R.id.femaleRadioButton)
                "Other" -> genderRadioGroup.check(R.id.otherRadioButton)
            }
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setBackground(ColorDrawable(Color.TRANSPARENT)) // Transparent background
            .create()

        dialog.show()

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim()
            val selectedGenderId = genderRadioGroup.checkedRadioButtonId

            if (name.isEmpty()) {
                nameEditText.error = "Name is required"
                return@setOnClickListener
            }
            if (!name.matches(Regex("^[a-zA-Z ]+$"))) {
                nameEditText.error = "Only letters are allowed"
                return@setOnClickListener
            }

            if (age.isEmpty()) {
                ageEditText.error = "Age is required"
                return@setOnClickListener
            }
            if (!age.matches(Regex("^\\d{1,3}$"))) {
                ageEditText.error = "Enter a valid age (max 3 digits)"
                return@setOnClickListener
            }
            val ageInt = age.toInt()
            if (ageInt !in 1..120) {
                ageEditText.error = "Age must be between 1 and 120"
                return@setOnClickListener
            }

            if (selectedGenderId == -1) {
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = when (selectedGenderId) {
                R.id.maleRadioButton -> "Male"
                R.id.femaleRadioButton -> "Female"
                else -> "Other"
            }

            if (passenger == null) {
                passengers.add(Passenger((passengers.size + 1).toString(), name, age, gender))
            } else {
                passengers[position!!] = Passenger(passenger.id, name, age, gender)
            }

            adapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun goToBookingSummary() {
        if (!validateInputs()) return

        val (seatType, priceTextView, seatsTextView) = getSeatDetails()
        val availableSeats = seatsTextView.filter { it.isDigit() }.toIntOrNull() ?: 0

        if (passengers.size > availableSeats) {
            Toast.makeText(this, "Not enough seats available for $seatType", Toast.LENGTH_SHORT).show()
            return
        }

        val seatPrefix = when (seatType) {
            "Sleeper" -> "S"
            "AC" -> "AC"
            "Second Seating" -> "Ss"
            else -> "X"
        }
        val totalSeats = when (seatType) {
            "Sleeper" -> 20
            "AC" -> 16
            "Second Seating" -> 30
            else -> 0
        }

        val updatedPassengers = assignSeats(seatPrefix, seatsTextView, totalSeats)

        val intent = Intent(this, ConfirmationActivity::class.java).apply {
            putExtra("trainName", intent.getStringExtra("trainName") ?: "")
            putExtra("trainNumber", intent.getStringExtra("trainNumber") ?: "")
            putExtra("departureStation", intent.getStringExtra("departureStation") ?: "")
            putExtra("arrivalStation", intent.getStringExtra("arrivalStation") ?: "")
            putExtra("departureTime", intent.getStringExtra("departureTime") ?: "")
            putExtra("arrivalTime", intent.getStringExtra("arrivalTime") ?: "")
            putExtra("duration", intent.getStringExtra("duration") ?: "")

            putExtra("seatType", seatType)
            putExtra("seatPrice", priceTextView)
            putExtra("availableSeats", seatsTextView)
            putExtra("selectedDate", selectedDate)

            putParcelableArrayListExtra("passengers", ArrayList(updatedPassengers))
        }
        startActivity(intent)
    }

    private fun validateInputs(): Boolean {
        if (selectedCardView == null) {
            Toast.makeText(this, "Please select a seat type", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a travel date", Toast.LENGTH_SHORT).show()
            return false
        }
        if (passengers.isEmpty()) {
            Toast.makeText(this, "Please add at least one passenger", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun getSeatDetails(): Triple<String, String, String> {
        return when (selectedCardView?.id) {
            R.id.sleeperCardView -> Triple("Sleeper", sleeperPriceTextView.text.toString(), sleeperSeatsTextView.text.toString())
            R.id.acCardView -> Triple("AC", acPriceTextView.text.toString(), acSeatsTextView.text.toString())
            R.id.secondSeatingCardView -> Triple("Second Seating", secondSeatingPriceTextView.text.toString(), secondSeatingSeatsTextView.text.toString())
            else -> Triple("Unknown", "Price not available", "Seats not available")
        }
    }

    private fun assignSeats(seatPrefix: String, seatsTextView: String, totalSeats: Int): List<Passenger> {
        val availableSeats = seatsTextView.filter { it.isDigit() }.toIntOrNull() ?: totalSeats
        var seatNumber = availableSeats

        return passengers.map { passenger ->
            val assignedSeat = "$seatPrefix$seatNumber"
            seatNumber--
            passenger.copy(seatNumber = assignedSeat)
        }
    }}

