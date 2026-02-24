package com.example.planetracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.planetracker.ui.theme.PlaneTrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PlaneTrackerTheme {
                // 1. Correct State Declaration
                // In Compose, we use remember { mutableStateOf(...) }.
                // .stateIn() is only for Kotlin Flows, not for UI State objects.
                val aircraftStates = remember { mutableStateOf<List<AircraftState>>(emptyList()) }

                // 2. Fetch data when the Composable enters the Composition
                LaunchedEffect(Unit) {
                    fetchAircraftStates { states ->
                        aircraftStates.value = states
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AircraftStatesScreen(
                        // Pass the .value of the state to the screen
                        aircraftStates = aircraftStates.value,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun fetchAircraftStates(onResult: (List<AircraftState>) -> Unit) {
        // Using lifecycleScope would be better, but sticking to your CoroutineScope structure
        // we ensure the callback happens on the Main thread for UI updates.
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getAllStates(40.0, -74.0, 41.0, -73.0).execute()
                if (response.isSuccessful) {
                    val body = response.body()
                    val statesList = body?.states?.mapNotNull { state ->
                        if (state.size >= 8) {
                            AircraftState(
                                callsign = (state[1] as? String)?.trim() ?: "Unknown",
                                originCountry = (state[2] as? String) ?: "Unknown",
                                position = Position(
                                    latitude = (state[6] as? Double) ?: 0.0,
                                    longitude = (state[5] as? Double) ?: 0.0
                                )
                            )
                        } else null
                    } ?: emptyList()

                    withContext(Dispatchers.Main) {
                        onResult(statesList)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching: ${e.message}")
            }
        }
    }
}

@Composable
fun AircraftStatesScreen(aircraftStates: List<AircraftState>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(
            items = aircraftStates,
            key = { it.callsign + it.position.latitude } // Unique key for better performance
        ) { state ->
            Text(
                text = "Callsign: ${state.callsign}, Country: ${state.originCountry}, Position: (${state.position.latitude}, ${state.position.longitude})",
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