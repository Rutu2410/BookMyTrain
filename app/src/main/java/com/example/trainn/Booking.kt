package com.example.trainn
data class Booking(
    val id: Int,
    val trainName: String,
    val trainNumber: String,
    val departureStation: String,
    val arrivalStation: String,
    val departureTime: String,
    val arrivalTime: String,
    val duration: String,
    val seatType: String="",
    val totalPrice: String,
    val selectedDate: String,
    val passengerDetails: String ="",
    val seatNumber: String="",
    val passengerCount: Int  = 1
)
