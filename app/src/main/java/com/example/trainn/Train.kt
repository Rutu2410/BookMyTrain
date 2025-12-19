package com.example.trainn

data class Train(
    val trainId: Int,
    val trainNumber: String,
    val trainName: String,
    val departureStation: String,
    val arrivalStation: String,
    val departureTime: String,
    val arrivalTime: String,
    val duration: String
)
