package com.example.trainn

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var departureAutoComplete: AutoCompleteTextView
    private lateinit var arrivalAutoComplete: AutoCompleteTextView
    private lateinit var fetchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var trainAdapter: TrainAdapter
    private val trainList = mutableListOf<Train>()
    private val stationList = mutableListOf<Station>()
    private val client = OkHttpClient()
    private lateinit var BASE_URL: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val swapButton = findViewById<ImageView>(R.id.swapButton)

        // Check if user is logged in, if not go to login screen
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Setup Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, findViewById(R.id.toolbar),
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

        // Load and set user info
        loadUserInfo()

        // Initialize train search UI components
        departureAutoComplete = findViewById(R.id.departureAutoComplete)
        arrivalAutoComplete = findViewById(R.id.arrivalAutoComplete)
        fetchButton = findViewById(R.id.searchButton)
        recyclerView = findViewById(R.id.trainRecyclerView)
        BASE_URL = getString(R.string.base_url)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 👉 Fetch stations immediately so suggestions are available
        fetchStations { stations ->
            runOnUiThread {
                stationList.clear()
                stationList.addAll(stations)
                val stationNames = stations.map { "${it.stationName} (${it.stationCode})" }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stationNames)
                departureAutoComplete.setAdapter(adapter)
                arrivalAutoComplete.setAdapter(adapter)
            }
        }

        // Swap button functionality
        swapButton.setOnClickListener {
            // Rotate the swap button to indicate swapping
            animateSwapButton(swapButton)

            // Swap the text between the two fields
            val tempText = departureAutoComplete.text.toString()
            departureAutoComplete.setText(arrivalAutoComplete.text.toString())
            arrivalAutoComplete.setText(tempText)
        }

        // Search button functionality
        fetchButton.setOnClickListener {
            val departureInput = departureAutoComplete.text.toString()
            val arrivalInput = arrivalAutoComplete.text.toString()

            if (departureInput == arrivalInput) {
                Toast.makeText(this, "Departure and Arrival stations cannot be the same", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedDeparture = stationList.find { "${it.stationName} (${it.stationCode})" == departureInput }
            val selectedArrival = stationList.find { "${it.stationName} (${it.stationCode})" == arrivalInput }

            if (selectedDeparture == null || selectedArrival == null) {
                Toast.makeText(this, "Please select valid stations", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fetchTrains(selectedDeparture.id, selectedArrival.id) { trains ->
                runOnUiThread {
                    trainList.clear()
                    trainList.addAll(trains)
                    trainAdapter = TrainAdapter(trainList)
                    recyclerView.adapter = trainAdapter
                }
            }
        }
    }

    private fun animateSwapButton(button: ImageView) {
        val rotateAnimation = ObjectAnimator.ofFloat(button, "rotation", 0f, 180f, 360f)
        rotateAnimation.duration = 300 // Duration of animation
        rotateAnimation.interpolator = AccelerateDecelerateInterpolator()
        rotateAnimation.start()
    }
    private fun loadUserInfo() {
        val username = sharedPreferences.getString("username", "User")
        val email = sharedPreferences.getString("email", "user@example.com")

        // Get header view from navigation drawer
        val headerView: View = navigationView.getHeaderView(0)
        val navUsername: TextView = headerView.findViewById(R.id.userNameTextView)
        val navEmail: TextView = headerView.findViewById(R.id.userEmailTextView)

        // Set values
        navUsername.text = username
        navEmail.text = email
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_bookings -> {
                val intent = Intent(this, BookingDetailsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.nav_logout -> {
                logoutUser()
                return true
            }
        }
        return true
    }

    private fun logoutUser() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Fetch station list from the database
    private fun fetchStations(callback: (List<Station>) -> Unit) {
        val request = Request.Builder().url("$BASE_URL/fetch_stations.php").get().build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { e.printStackTrace() }
            override fun onResponse(call: Call, response: Response) {
                response.body?.let { body ->
                    val jsonArray = JSONArray(body.string())
                    val stations = mutableListOf<Station>()
                    for (i in 0 until jsonArray.length()) {
                        val station = jsonArray.getJSONObject(i)
                        stations.add(
                            Station(
                                id = station.getInt("station_id"),
                                stationName = station.getString("station_name"),
                                stationCode = station.getString("station_code")
                            )
                        )
                    }
                    callback(stations)
                }
            }
        })
    }

    private fun fetchTrains(departureId: Int, arrivalId: Int, callback: (List<Train>) -> Unit) {
        val url = "$BASE_URL/fetch_trains.php?departure_station_id=$departureId&arrival_station_id=$arrivalId"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to fetch trains. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { body ->
                    val bodyString = body.string()

                    // Debug: Print the response to logcat
                    android.util.Log.d("API_RESPONSE", bodyString)

                    try {
                        val jsonArray = JSONArray(bodyString)
                        val trains = mutableListOf<Train>()
                        for (i in 0 until jsonArray.length()) {
                            val train = jsonArray.getJSONObject(i)
                            trains.add(
                                Train(
                                    trainId = train.getInt("train_id"),
                                    trainNumber = train.getString("train_number"),
                                    trainName = train.getString("train_name"),
                                    departureStation = train.getString("departure_station"),
                                    arrivalStation = train.getString("arrival_station"),
                                    departureTime = train.getString("departure_time"),
                                    arrivalTime = train.getString("arrival_time"),
                                    duration = train.getString("duration")
                                )
                            )
                        }
                        callback(trains)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "Error: Invalid response format\n$bodyString",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        })
    }
}
