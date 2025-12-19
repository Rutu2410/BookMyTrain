package com.example.trainn


data class train1(
    val trainId: Int,
    val trainNumber: String, // Added train number
    val trainName: String)
{
    override fun toString(): String = "$trainNumber - $trainName"
}