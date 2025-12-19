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

class Trainopration : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val trains = mutableListOf<train1>()
    private lateinit var BASE_URL: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train)

        recyclerView = findViewById(R.id.recyclerViewTrains)
        recyclerView.layoutManager = LinearLayoutManager(this)
        BASE_URL = getString(R.string.base_url) // Set base URL in strings.xml


        fetchTrains()

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            showTrainDialog(null)
        }
    }

    private fun fetchTrains() {
        val requestQueue = Volley.newRequestQueue(this)
        val url = "$BASE_URL/train_operations.php?action=fetch"

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                trains.clear()
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    trains.add(
                        train1(
                            obj.getInt("id"),
                            obj.getString("train_number"),
                            obj.getString("train_name")
                        )
                    )
                }
                recyclerView.adapter = TrainAdapter_train(trains, ::showTrainDialog, ::deleteTrain)
            },
            { error -> Log.e("API_ERROR", error.toString()) })
        requestQueue.add(request)
    }
    private fun showTrainDialog(train: train1?) {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_edit_train, null)
        val numberInput = view.findViewById<EditText>(R.id.edit_train_number)
        val nameInput = view.findViewById<EditText>(R.id.edit_train_name)
        val btnSubmit = view.findViewById<Button>(R.id.btn_save)

        train?.let {
            numberInput.setText(it.trainNumber)
            nameInput.setText(it.trainName)
        }

        val alertDialog = dialog.setView(view).create() // Create dialog

        btnSubmit.setOnClickListener {
            val number = numberInput.text.toString().trim()
            val name = nameInput.text.toString().trim()

            // Check if fields are empty
            if (number.isEmpty()) {
                Toast.makeText(this, "Train number cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (name.isEmpty()) {
                Toast.makeText(this, "Train name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validation rules
            if (!number.matches(Regex("\\d{5}"))) { // Train number must be exactly 5 digits
                Toast.makeText(this, "Train number must be exactly 5 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!name.matches(Regex("^[a-zA-Z ]+\$"))) { // Train name must contain only letters
                Toast.makeText(this, "Train name must contain only letters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save or Update
            if (train == null) {
                addTrain(number, name)
            } else {
                updateTrain(train.trainId, number, name)
            }

            alertDialog.dismiss() // Close dialog after saving
        }

        alertDialog.show() // Show dialog
    }


    private fun addTrain(number: String, name: String) {
        val url = "$BASE_URL/train_operations.php?action=add"
        val request = object : StringRequest(
            Method.POST, url,
            {
                fetchTrains()  // Refresh the list of trains
                Toast.makeText(this, "Train added successfully!", Toast.LENGTH_SHORT).show()  // Show success message
                 // Close the activity and go back
            },
            { error ->
                Log.e("API_ERROR", error.toString())
                Toast.makeText(this, "Error adding train", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams() = hashMapOf("train_number" to number, "train_name" to name)
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun updateTrain(id: Int, number: String, name: String) {
        val url = "$BASE_URL/train_operations.php?action=update"
        val request = object : StringRequest(
            Method.POST, url,
            {
                fetchTrains()  // Refresh the list of trains
                Toast.makeText(this, "Train updated successfully!", Toast.LENGTH_SHORT).show()  // Show success message
                 // Close the activity and go back
            },
            { error ->
                Log.e("API_ERROR", error.toString())
                Toast.makeText(this, "Error updating train", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams() =
                hashMapOf("id" to id.toString(), "train_number" to number, "train_name" to name)
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun deleteTrain(id: Int) {
        // Show confirmation dialog before deleting
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Train")
            .setMessage("Are you sure you want to delete this train?")
            .setPositiveButton("Yes") { _, _ ->
                // Proceed with the delete operation if the user confirms
                val url = "$BASE_URL/train_operations.php?action=delete"
                val request = object : StringRequest(
                    Method.POST, url,
                    {
                        fetchTrains()  // Refresh the list of trains
                        Toast.makeText(this, "Train deleted successfully!", Toast.LENGTH_SHORT)
                            .show()  // Show success message
                    },
                    { Log.e("API_ERROR", it.toString()) }) {
                    override fun getParams() = hashMapOf("id" to id.toString())
                }
                Volley.newRequestQueue(this).add(request)
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()  // Dismiss the dialog if user cancels
            }

        dialog.show()  // Show the confirmation dialog
    }

}