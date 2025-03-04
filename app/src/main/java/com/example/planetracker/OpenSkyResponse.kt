package com.example.planetracker

data class OpenSkyResponse(
    val time: Long,
    val states: List<List<Any>>
)

data class State(
    val callsign: String,
    val originCountry: String,
    val position: List<Double>
)