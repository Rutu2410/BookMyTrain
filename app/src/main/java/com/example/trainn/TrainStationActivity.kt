package com.example.trainn

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class TrainStationActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrainStationAdapter
    private lateinit var trainNameTextView: TextView
    private val stationList = mutableListOf<Station>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_route)

        val trainId = intent?.getIntExtra("trainId", -1) ?: -1
        val trainName = intent?.getStringExtra("trainName") ?: "Unknown Train"

        if (trainId == -1) {
            Log.e("TrainStationActivity", "Invalid Train ID received")
            Toast.makeText(this, "Invalid Train ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerViewRoutes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        trainNameTextView = findViewById(R.id.trainNameTextView)
        trainNameTextView.text = trainName

        adapter = TrainStationAdapter(stationList)
        recyclerView.adapter = adapter

        fetchTrainRoutes(trainId)
    }

    private fun fetchTrainRoutes(trainId: Int) {
        val url = "http://192.168.209.246/database1/get_train_route.php?mode=json&train_id=$trainId"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch data", e)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                response.body?.let { body ->
                    val responseBody = body.string()
                    Log.d("API_RESPONSE", responseBody) // Log the response

                    if (responseBody.isEmpty()) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "No data available", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    try {
                        val jsonResponse = JSONArray(responseBody)
                        val newStations = mutableListOf<Station>()

                        for (i in 0 until jsonResponse.length()) {
                            val obj = jsonResponse.getJSONObject(i)

                            val stationName = obj.optString("station_name", "Unknown")
                            val stationCode = obj.optString("station_code", "N/A")
                            val departureTime = obj.optString("departure_time", "")
                            val arrivalTime = obj.optString("arrival_time", "")

                            val time = when {
                                departureTime.isNotEmpty() -> "$departureTime (Dep)"
                                arrivalTime.isNotEmpty() -> "$arrivalTime (Arr)"
                                else -> "Unknown Time"
                            }

                            newStations.add(Station(id = i, stationName = stationName, stationCode = stationCode, time = time))
                        }

                        runOnUiThread {
                            stationList.clear()
                            stationList.addAll(newStations)
                            adapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        Log.e("JSON_ERROR", "Invalid JSON format: $responseBody", e)
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Error parsing data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        })
    }
}
