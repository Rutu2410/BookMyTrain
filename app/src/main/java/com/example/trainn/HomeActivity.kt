package com.example.trainn

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val stations = mutableListOf<Station>()
    private lateinit var BASE_URL: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerViewStations)
        recyclerView.adapter = StationAdapter(stations, ::showStationDialog, ::showDeleteConfirmationDialog)
        recyclerView.layoutManager = LinearLayoutManager(this)

        BASE_URL = getString(R.string.base_url)

        fetchStations()

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            showStationDialog(null)
        }
    }

    private fun fetchStations() {
        val requestQueue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(Request.Method.GET, "$BASE_URL/fetch_stations.php", null,
            { response ->
                stations.clear()
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    stations.add(Station(obj.getInt("station_id"), obj.getString("station_name"), obj.getString("station_code")))
                }
                recyclerView.adapter = StationAdapter(stations, ::showStationDialog, ::showDeleteConfirmationDialog)
            },
            { error -> Log.e("API_ERROR", error.toString()) })
        requestQueue.add(request)
    }

    private fun showStationDialog(station: Station?) {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_edit_station, null)
        val nameInput = view.findViewById<EditText>(R.id.edit_name)
        val codeInput = view.findViewById<EditText>(R.id.edit_code)
        val btnSubmit = view.findViewById<Button>(R.id.btn_save)

        station?.let {
            nameInput.setText(it.stationName)
            codeInput.setText(it.stationCode)
        }

        val alertDialog = dialog.setView(view).show()

        btnSubmit.setOnClickListener {
            val name = formatStationName(nameInput.text.toString().trim())
            val code = codeInput.text.toString().trim().uppercase()

            if (!validateInput(name, code)) {
                return@setOnClickListener
            }

            if (station == null) {
                addStation(name, code)
            } else {
                updateStation(station.id, name, code)
            }

            alertDialog.dismiss()
        }
    }

    private fun formatStationName(name: String): String {
        return name.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    private fun validateInput(name: String, code: String): Boolean {
        if (name.isEmpty() || code.isEmpty()) {
            Toast.makeText(this, "Please enter both station name and code", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!code.matches(Regex("^[A-Z]+$"))) {
            Toast.makeText(this, "Station code must contain only uppercase letters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!name.matches(Regex("^[A-Z][a-z]*( [A-Z][a-z]*)*$"))) {
            Toast.makeText(this, "Station name must have each word start with a capital letter", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun addStation(name: String, code: String) {
        val request = object : StringRequest(Request.Method.POST, "$BASE_URL/add_station.php",
            {
                fetchStations()
                Toast.makeText(this, "Station added successfully", Toast.LENGTH_SHORT).show()
            },
            { Log.e("API_ERROR", it.toString()) }) {
            override fun getParams() = hashMapOf("station_name" to name, "station_code" to code)
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun updateStation(id: Int, name: String, code: String) {
        val request = object : StringRequest(Request.Method.POST, "$BASE_URL/update_station.php",
            {
                fetchStations()
                Toast.makeText(this, "Station updated successfully", Toast.LENGTH_SHORT).show()
            },
            { Log.e("API_ERROR", it.toString()) }) {
            override fun getParams() = hashMapOf("station_id" to id.toString(), "station_name" to name, "station_code" to code)
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun showDeleteConfirmationDialog(id: Int) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this station?")
            .setPositiveButton("Yes") { _, _ ->
                deleteStation(id)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteStation(id: Int) {
        val request = object : StringRequest(Request.Method.POST, "$BASE_URL/delete_station.php",
            {
                fetchStations()
                Toast.makeText(this, "Station deleted successfully", Toast.LENGTH_SHORT).show()
            },
            {
                Log.e("API_ERROR", it.toString())
                Toast.makeText(this, "Failed to delete station", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams() = hashMapOf("station_id" to id.toString())
        }
        Volley.newRequestQueue(this).add(request)
    }
}
