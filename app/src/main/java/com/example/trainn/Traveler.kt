package com.example.trainn

import java.io.Serializable

data class Traveler(
    val id: Int,
    val name: String,
    val age: Int,
    val gender: String,
    val seatNumber: String // ✅ Added seat number field
) : Serializable
