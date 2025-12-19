package com.example.trainn


import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class TrainRouteCRUD(context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)
    private val BASE_URL: String = context.getString(R.string.base_url) // e.g., "http://yourserver.com/api"

    fun insertRoute(
        trainId: Int,
        departureStationId: Int,
        arrivalStationId: Int,
        departureTime: String,
        arrivalTime: String,
        duration: String,
        price: Double,
        sleeperPrice: Double,
        acPrice: Double,
        secondSeatingPrice: Double,
        listener: (String) -> Unit,
        errorListener: (VolleyError) -> Unit
    ) {
        val url = "http://192.168.24.246/database1/crud_train_routes.php"
        val request = object : StringRequest(Request.Method.POST, url,
            { response -> listener(response) },
            { error -> errorListener(error) }
        ) {
            override fun getParams(): MutableMap<String, String> = hashMapOf(
                "action" to "insert",
                "train_id" to trainId.toString(),
                "departure_station_id" to departureStationId.toString(),
                "arrival_station_id" to arrivalStationId.toString(),
                "departure_time" to departureTime,
                "arrival_time" to arrivalTime,
                "duration" to duration,
                "price" to price.toString(),
                "sleeper_price" to sleeperPrice.toString(),
                "ac_price" to acPrice.toString(),
                "second_seating_price" to secondSeatingPrice.toString()
            )
        }
        requestQueue.add(request)
    }

    fun updateRoute(
        id: Int,
        trainId: Int,
        departureStationId: Int,
        arrivalStationId: Int,
        departureTime: String,
        arrivalTime: String,
        duration: String,
        price: Double,
        sleeperPrice: Double,
        acPrice: Double,
        secondSeatingPrice: Double,
        listener: (String) -> Unit,
        errorListener: (VolleyError) -> Unit
    ) {
        val url = "http://192.168.24.246/database1/crud_train_routes.php"
        val request = object : StringRequest(Request.Method.POST, url,
            { response -> listener(response) },
            { error -> errorListener(error) }
        ) {
            override fun getParams(): MutableMap<String, String> = hashMapOf(
                "action" to "update",
                "id" to id.toString(),
                "train_id" to trainId.toString(),
                "departure_station_id" to departureStationId.toString(),
                "arrival_station_id" to arrivalStationId.toString(),
                "departure_time" to departureTime,
                "arrival_time" to arrivalTime,
                "duration" to duration,
                "price" to price.toString(),
                "sleeper_price" to sleeperPrice.toString(),
                "ac_price" to acPrice.toString(),
                "second_seating_price" to secondSeatingPrice.toString()
            )
        }
        requestQueue.add(request)
    }

    fun deleteRoute(
        id: Int,
        listener: (String) -> Unit,
        errorListener: (VolleyError) -> Unit
    ) {
        val url = "http://192.168.24.246/database1/crud_train_routes.php"
        val request = object : StringRequest(Request.Method.POST, url,
            { response -> listener(response) },
            { error -> errorListener(error) }
        ) {
            override fun getParams(): MutableMap<String, String> = hashMapOf(
                "action" to "delete",
                "id" to id.toString()
            )
        }
        requestQueue.add(request)
    }
}
