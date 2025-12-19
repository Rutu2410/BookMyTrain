package com.example.trainn

import android.app.TimePickerDialog
import android.net.ParseException
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddTrainRouteActivity : AppCompatActivity() {
    private lateinit var autoCompleteTrain: AutoCompleteTextView
    private lateinit var autoCompleteDepartureStation: AutoCompleteTextView
    private lateinit var autoCompleteArrivalStation: AutoCompleteTextView
    private lateinit var btnSelectDepartureTime: Button
    private lateinit var btnSelectArrivalTime: Button
    private lateinit var tvDuration: TextView
    private lateinit var etPrice: EditText
    private lateinit var etSleeperPrice: EditText
    private lateinit var etAcPrice: EditText
    private lateinit var etSecondSeatingPrice: EditText
    private lateinit var btnSaveRoute: Button
    private lateinit var BASE_URL: String
    private var trainRoute: TrainRoute? = null

    private var departureTime: String = ""
    private var arrivalTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_edit_route)

        autoCompleteTrain = findViewById(R.id.autoCompleteTrain)
        autoCompleteDepartureStation = findViewById(R.id.autoCompleteDepartureStation)
        autoCompleteArrivalStation = findViewById(R.id.autoCompleteArrivalStation)
        btnSelectDepartureTime = findViewById(R.id.btnSelectDepartureTime)
        btnSelectArrivalTime = findViewById(R.id.btnSelectArrivalTime)
        tvDuration = findViewById(R.id.tvDuration)
        etPrice = findViewById(R.id.etPrice)
        etSleeperPrice = findViewById(R.id.etSleeperPrice)
        etAcPrice = findViewById(R.id.etAcPrice)
        etSecondSeatingPrice = findViewById(R.id.etSecondSeatingPrice)
        btnSaveRoute = findViewById(R.id.btnSaveRoute)
        BASE_URL = getString(R.string.base_url)

        btnSelectDepartureTime.setOnClickListener {
            pickTime { time ->
                departureTime = time
                updateDuration()
            }
        }

        btnSelectArrivalTime.setOnClickListener {
            pickTime { time ->
                arrivalTime = time
                updateDuration()
            }
        }

        loadTrains()
        loadStations()

        val trainRoute = intent.getParcelableExtra<TrainRoute>("train_route")

        if (trainRoute != null) {
            populateFields(trainRoute)
            btnSaveRoute.text = "Update Route"  // Change button text
        }

        btnSaveRoute.setOnClickListener {
            Log.d("DEBUG", "Save button clicked")
            trainRoute?.let {
                updateTrainRoute(it.id)
            } ?: saveTrainRoute()
        }



    }
    private fun populateFields(trainRoute: TrainRoute) {
        autoCompleteTrain.setText(trainRoute.trainName)
        autoCompleteDepartureStation.setText(trainRoute.departureStationName)
        autoCompleteArrivalStation.setText(trainRoute.arrivalStationName)
        departureTime = trainRoute.departureTime
        arrivalTime = trainRoute.arrivalTime
        tvDuration.text = "Duration: ${trainRoute.duration}"
        etPrice.setText(trainRoute.price.toString())
        etSleeperPrice.setText(trainRoute.sleeperPrice.toString())
        etAcPrice.setText(trainRoute.acPrice.toString())
        etSecondSeatingPrice.setText(trainRoute.secondSeatingPrice.toString())
    }
    private fun updateTrainRoute(id: Int) {
        Log.d("DEBUG", "updateTrainRoute called with ID: $id")

        val trainName = autoCompleteTrain.text.toString().trim()
        val departureStation = autoCompleteDepartureStation.text.toString().trim()
        val arrivalStation = autoCompleteArrivalStation.text.toString().trim()

        val trainId = trainMap[trainName] ?: -1
        val departureStationId = stationMap[departureStation] ?: -1
        val arrivalStationId = stationMap[arrivalStation] ?: -1

        Log.d("DEBUG", "Train ID: $trainId, Departure ID: $departureStationId, Arrival ID: $arrivalStationId")

        if (trainId == -1 || departureStationId == -1 || arrivalStationId == -1) {
            Log.e("DEBUG", "Invalid Train or Station selection")
            Toast.makeText(this, "Invalid Train or Station selection", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$BASE_URL/crud_train_routes.php?action=update"
        Log.d("DEBUG", "Update URL: $url")

        val request = object : StringRequest(Method.POST, url, {
            Log.d("DEBUG", "Train Route Updated Successfully")
            Toast.makeText(this, "Train Route Updated", Toast.LENGTH_SHORT).show()
            finish()
        }, { error ->
            Log.e("API_ERROR", "Volley Error: ${error.networkResponse?.statusCode} - ${error.message}")
        }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()

                // Ensure these values are dynamically assigned
                val departureTime = "09:28:00"  // Replace with actual input
                val arrivalTime = "10:30:00"    // Replace with actual input

                Log.d("DEBUG", "Start Time: $departureTime, End Time: $arrivalTime")  // Debug log

                try {
                    val duration = calculateDuration(departureTime, arrivalTime).toString()
                    params["id"] = id.toString()
                    params["train_id"] = trainId.toString()
                    params["departure_station_id"] = departureStationId.toString()
                    params["arrival_station_id"] = arrivalStationId.toString()
                    params["departure_time"] = departureTime
                    params["arrival_time"] = arrivalTime
                    params["duration"] = duration
                    params["price"] = etPrice.text.toString()
                    params["sleeper_price"] = etSleeperPrice.text.toString()
                    params["ac_price"] = etAcPrice.text.toString()
                    params["second_seating_price"] = etSecondSeatingPrice.text.toString()
                } catch (e: Exception) {
                    Log.e("DEBUG", "Error in getParams: ${e.message}")
                    e.printStackTrace()
                }

                Log.d("DEBUG", "POST Params: $params")
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }



    private val trainMap = mutableMapOf<String, Int>()
    private val stationMap = mutableMapOf<String, Int>()

    private fun updateDuration() {
        if (departureTime.isNotEmpty() && arrivalTime.isNotEmpty()) {
            val duration = calculateDuration(departureTime, arrivalTime)

            val hours = duration / 60
            val minutes = duration % 60

            tvDuration.text = if (duration >= 0) {
                String.format("Duration: %02d:%02d hrs", hours, minutes)
            } else {
                "Duration: --:--"
            }
        } else {
            tvDuration.text = "Duration: --:--"
        }
    }




    private fun loadTrains() {
        val url = "$BASE_URL/train_operations.php?action=fetch"
        val request = StringRequest(Request.Method.GET, url, { response ->
            val trains = JSONArray(response)
            val trainList = mutableListOf<String>()
            for (i in 0 until trains.length()) {
                val obj = trains.getJSONObject(i)
                val trainName = obj.getString("train_name")
                val trainId = obj.getInt("id")
                trainList.add(trainName)
                trainMap[trainName] = trainId
            }
            autoCompleteTrain.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, trainList))
        }, { error -> Log.e("API_ERROR", error.toString()) })

        Volley.newRequestQueue(this).add(request)
    }

    private fun loadStations() {
        val url = "$BASE_URL/fetch_stations.php"
        val request = StringRequest(Request.Method.GET, url, { response ->
            val stations = JSONArray(response)
            val stationList = mutableListOf<String>()
            for (i in 0 until stations.length()) {
                val obj = stations.getJSONObject(i)
                val stationName = obj.getString("station_name")
                val stationId = obj.getInt("station_id")
                stationList.add(stationName)
                stationMap[stationName] = stationId
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stationList)
            autoCompleteDepartureStation.setAdapter(adapter)
            autoCompleteArrivalStation.setAdapter(adapter)
        }, { error -> Log.e("API_ERROR", error.toString()) })

        Volley.newRequestQueue(this).add(request)
    }

    private fun pickTime(callback: (String) -> Unit) {
        TimePickerDialog(this, { _, hour, minute ->
            val formattedTime = String.format("%02d:%02d:00", hour, minute) // 24-hour format with seconds
            callback(formattedTime) // Pass the correctly formatted time
            updateDuration()
        }, 12, 0, true).show() // Set 'true' for 24-hour format
    }



    fun calculateDuration(startTime: String, endTime: String): Long {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        Log.d("DEBUG", "Start Time: $startTime, End Time: $endTime")

        return try {
            val start = timeFormat.parse(startTime)
            val end = timeFormat.parse(endTime)

            if (start != null && end != null) {
                var diff = end.time - start.time
                if (diff < 0) {
                    diff += 24 * 60 * 60 * 1000 // Handle next-day arrival
                }
                diff / (1000 * 60) // Convert milliseconds to minutes
            } else {
                Log.e("DEBUG", "Parsing failed: start or end time is null")
                0
            }
        } catch (e: ParseException) {
            Log.e("DEBUG", "ParseException: ${e.message}")
            e.printStackTrace()
            0
        }
    }


    private fun saveTrainRoute() {
        val trainName = autoCompleteTrain.text.toString().trim()
        val departureStation = autoCompleteDepartureStation.text.toString().trim()
        val arrivalStation = autoCompleteArrivalStation.text.toString().trim()

        val trainId = trainMap[trainName] ?: -1
        val departureStationId = stationMap[departureStation] ?: -1
        val arrivalStationId = stationMap[arrivalStation] ?: -1

        if (trainName.isEmpty() || departureStation.isEmpty() || arrivalStation.isEmpty()) {
            Toast.makeText(this, "Please select a train and stations", Toast.LENGTH_SHORT).show()
            return
        }

        if (trainId == -1 || departureStationId == -1 || arrivalStationId == -1) {
            Toast.makeText(this, "Invalid Train or Station selection", Toast.LENGTH_SHORT).show()
            return
        }

        if (departureStationId == arrivalStationId) {
            Toast.makeText(this, "Departure and Arrival stations must be different", Toast.LENGTH_SHORT).show()
            return
        }

        if (departureTime.isEmpty() || arrivalTime.isEmpty()) {
            Toast.makeText(this, "Please select both departure and arrival times", Toast.LENGTH_SHORT).show()
            return
        }

        val priceFields = listOf(etPrice, etSleeperPrice, etAcPrice, etSecondSeatingPrice)
        for (field in priceFields) {
            val price = field.text.toString().trim()
            if (price.isEmpty() || price.toDoubleOrNull() == null || price.toDouble() <= 0) {
                Toast.makeText(this, "Enter valid positive prices", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val url = "$BASE_URL/crud_train_routes.php?action=insert"

        val request = object : StringRequest(Method.POST, url, {
            Toast.makeText(this, "Train Route Added", Toast.LENGTH_SHORT).show()
            finish()
        }, { error -> Log.e("API_ERROR", error.toString()) }) {
            override fun getParams(): MutableMap<String, String> {
                val params = hashMapOf(
                    "train_id" to trainId.toString(),
                    "departure_station_id" to departureStationId.toString(),
                    "arrival_station_id" to arrivalStationId.toString(),
                    "departure_time" to departureTime,
                    "arrival_time" to arrivalTime,
                    "price" to etPrice.text.toString(),
                    "sleeper_price" to etSleeperPrice.text.toString(),
                    "ac_price" to etAcPrice.text.toString(),
                    "second_seating_price" to etSecondSeatingPrice.text.toString()
                )

                Log.d("DEBUG", "Departure Time: $departureTime, Arrival Time: $arrivalTime")

                try {
                    val duration = calculateDuration(departureTime, arrivalTime) // Pass actual times
                    params["duration"] = duration.toString()
                } catch (e: Exception) {
                    Log.e("DEBUG", "Error calculating duration: ${e.message}")
                    e.printStackTrace()
                }

                Log.d("DEBUG", "POST Params: $params")
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }


}
