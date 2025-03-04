package com.example.planetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.util.Log
import com.example.planetracker.ui.theme.PlaneTrackerTheme
import com.example.planetracker.AircraftState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

// Import the AircraftState and Position from the new file
import com.example.planetracker.Position

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Main started")
        enableEdgeToEdge()
        setContent {
            PlaneTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var aircraftStates by remember { mutableStateOf<List<AircraftState>>(emptyList()) }

                    LaunchedEffect(Unit) {
                        fetchAircraftStates { states ->
                            aircraftStates = states
                        }
                    }

                    AircraftStatesScreen(
                        aircraftStates = aircraftStates,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun fetchAircraftStates(onResult: (List<AircraftState>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllStates(40.0, -74.0, 41.0, -73.0).execute()
                if (response.isSuccessful) {
                    val body = response.body()
                    val aircraftStates = body?.states?.mapNotNull { state ->
                        if (state.size >= 8) {
                            AircraftState(
                                callsign = (state[1] as? String)?.trim() ?: "",
                                originCountry = (state[2] as? String) ?: "",
                                position = Position(
                                    latitude = (state[6] as? Double) ?: 0.0,
                                    longitude = (state[5] as? Double) ?: 0.0
                                )
                            )
                        } else null
                    } ?: emptyList()
                    Log.d("MainActivity", "Fetched ${aircraftStates.size} aircraft states")
                    withContext(Dispatchers.Main) {
                        onResult(aircraftStates)
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch aircraft states: ${response.errorBody()?.string()}")
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "Network error while fetching aircraft states", e)
            } catch (e: Exception) {
                Log.e("MainActivity", "Exception while fetching aircraft states", e)
            }
        }
    }


    @Composable
    fun AircraftStatesScreen(aircraftStates: List<AircraftState>, modifier: Modifier = Modifier) {
        LazyColumn(modifier = modifier) {
            items(aircraftStates) { state ->
                Text(
                    text = "Callsign: ${state.callsign}, Country: ${state.originCountry}, Position: ${state.position}",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun AircraftStatesScreenPreview() {
        PlaneTrackerTheme {
            AircraftStatesScreen(
                aircraftStates = listOf(
                    AircraftState("ABC123", "USA", Position(40.0, -74.0)),
                    AircraftState("XYZ789", "Canada", Position(45.0, -75.0))
                )
            )
        }
    }
}
