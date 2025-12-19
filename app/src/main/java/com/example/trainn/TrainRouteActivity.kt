package com.example.trainn
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject

class TrainRouteActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrainRouteAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var BASE_URL: String


    private val trainRouteList = mutableListOf<TrainRoute>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train_routes)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        progressBar = findViewById(R.id.progressBar)
        BASE_URL = getString(R.string.base_url) // Set base URL in strings.xml

        val btnAddTrain= findViewById<Button>(R.id.btnAddTrain)

        btnAddTrain.setOnClickListener {
            val intent = Intent(this, AddTrainRouteActivity::class.java)
            startActivity(intent)
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TrainRouteAdapter(trainRouteList,
            onEditClick = { trainRoute ->
                val intent = Intent(this, AddTrainRouteActivity::class.java)
                intent.putExtra("train_route", trainRoute)
                startActivity(intent)
            },
            onDeleteClick = { id ->
                deleteTrainRoute(id)
            }
        )
        recyclerView.adapter = adapter

        fetchTrainRoutes()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchTrainRoutes() {
        progressBar.visibility = View.VISIBLE
        val queue: RequestQueue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(Request.Method.GET, "$BASE_URL/crud_train_routes.php?action=fetch", null,
            { response ->
                progressBar.visibility = View.GONE
                try {
                    val data: JSONArray = response.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        val obj: JSONObject = data.getJSONObject(i)
                        val trainRoute = TrainRoute(
                            obj.getInt("id"),
                            obj.getInt("train_id"),
                            obj.getString("train_number"),
                            obj.getString("train_name"),
                            obj.getString("departure_station_name"),
                            obj.getString("departure_station_code"),
                            obj.getString("arrival_station_name"),
                            obj.getString("arrival_station_code"),
                            obj.getString("departure_time"),
                            obj.getString("arrival_time"),
                            obj.getString("duration"),
                            obj.getDouble("price"),
                            obj.getDouble("sleeper_price"),
                            obj.getDouble("ac_price"),
                            obj.getDouble("second_seating_price")
                        )
                        trainRouteList.add(trainRoute)
                    }
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { _ ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            })

        queue.add(request)
    }
    private fun deleteTrainRoute(id: Int) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Confirm Delete")
        alertDialog.setMessage("Are you sure you want to delete this train route?")

        alertDialog.setPositiveButton("Yes") { _, _ ->
            // If user confirms, delete the route
            val request = JsonObjectRequest(
                Request.Method.GET, "$BASE_URL/crud_train_routes.php?action=delete&id=$id", null,
                { response ->
                    Toast.makeText(this, "Train route deleted", Toast.LENGTH_SHORT).show()
                    fetchTrainRoutes()  // Refresh list after deletion
                },
                { error ->
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                })
            Volley.newRequestQueue(this).add(request)
        }

        alertDialog.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()  // Close the dialog if user clicks No
        }

        alertDialog.show()
    }


}
