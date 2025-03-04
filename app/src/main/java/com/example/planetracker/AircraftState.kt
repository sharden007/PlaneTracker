package com.example.planetracker

data class Position(val latitude: Double, val longitude: Double)

data class AircraftState(
    val callsign: String,
    val originCountry: String,
    val position: Position
)
