package com.example.tricycledispatchandzoneapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                TricycleApp()
            }
        }
    }
}

@Composable
fun TricycleApp() {

    // CONNECT BACKEND
    val dispatchService = remember {
        DispatchService()
    }

    // GET TODA ZONES
    val todaNames = remember {
        dispatchService.getAllTODAs().map { it.name }
    }

    // FRONTEND STATES
    var currentScreen by remember {
        mutableStateOf("welcome")
    }

    var selectedTODA by remember {
        mutableStateOf("")
    }

    var passengerCount by remember {
        mutableIntStateOf(1)
    }

    var selectedRide by remember {
        mutableStateOf<Ride?>(null)
    }

    var message by remember {
        mutableStateOf("")
    }

    var requestFailed by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        when (currentScreen) {

            // WELCOME SCREEN
            "welcome" -> {

                Spacer(modifier = Modifier.height(80.dp))

                Text(
                    "TRICYCLE DISPATCH",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    "Welcome to the Tricycle Dispatch and Service Zone App."
                )

                Button(
                    onClick = {
                        currentScreen = "home"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Get Started")
                }
            }

            // HOME SCREEN
            "home" -> {

                Text(
                    "Home",
                    style = MaterialTheme.typography.headlineLarge
                )

                Text("Book a tricycle ride in your selected barangay.")

                Button(
                    onClick = {
                        currentScreen = "toda"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Book a Ride")
                }

                OutlinedButton(
                    onClick = {
                        currentScreen = "welcome"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back")
                }
            }

            // TODA SELECTION
            "toda" -> {

                Text(
                    "Select Your TODA Zone",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text("Available barangay zones: ${todaNames.size}")

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(
                        items = todaNames,
                        key = { it }
                    ) { todaName ->

                        Button(
                            onClick = {
                                selectedTODA = todaName
                                message = ""
                                requestFailed = false
                                currentScreen = "passengers"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(todaName)
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        currentScreen = "home"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back")
                }
            }

            // PASSENGER COUNT
            "passengers" -> {

                Text(
                    "Passenger Information",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text("Selected TODA: $selectedTODA")

                Text("Select Passenger Count:")

                for (number in 1..5) {

                    OutlinedButton(
                        onClick = {
                            passengerCount = number
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (passengerCount == number) {
                                "$number Passenger(s) - Selected"
                            } else {
                                "$number Passenger(s)"
                            }
                        )
                    }
                }

                Text("Selected passengers: $passengerCount")

                Button(
                    onClick = {

                        // SEND REQUEST TO BACKEND
                        val result = dispatchService.requestRide(
                            selectedTODA,
                            passengerCount
                        )

                        message = result.message
                        requestFailed = !result.successful

                        if (result.successful) {

                            selectedRide = result.ride

                            if (selectedRide != null) {
                                currentScreen = "assigned"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Request Ride")
                }

                if (requestFailed) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedButton(
                    onClick = {
                        currentScreen = "toda"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back")
                }
            }

            // DRIVER ASSIGNMENT
            "assigned" -> {

                Text(
                    "Driver Assigned!",
                    style = MaterialTheme.typography.headlineMedium
                )

                val ride = selectedRide

                if (ride != null) {

                    Text("Ride ID: ${ride.rideId}")

                    Text("TODA: ${ride.pickupZone}")

                    Text("Passenger Count: ${ride.passengerCount}")

                    Text("Assigned Driver: ${ride.assignedDriver.name}")

                    Text("Driver ID: ${ride.assignedDriver.driverId}")

                    Text(
                        "Passenger Capacity: ${ride.assignedDriver.passengerCapacity}"
                    )

                    Text("Driver Status: On Work")

                    Text("Ride Status: ${ride.status}")
                }

                Text(message)

                Button(
                    onClick = {
                        currentScreen = "status"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Ride Status")
                }
            }

            // RIDE STATUS
            "status" -> {

                Text(
                    "Ride Status",
                    style = MaterialTheme.typography.headlineMedium
                )

                val ride = selectedRide

                if (ride != null) {

                    Text("Ride ID: ${ride.rideId}")

                    Text("TODA: ${ride.pickupZone}")

                    Text("Driver: ${ride.assignedDriver.name}")

                    Text("Passengers: ${ride.passengerCount}")

                    Text("Status: ${ride.status}")

                    Text(
                        if (ride.assignedDriver.available) {
                            "Driver Status: Available"
                        } else {
                            "Driver Status: On Work"
                        }
                    )

                    if (ride.status == "ONGOING") {

                        Button(
                            onClick = {

                                // COMPLETE RIDE IN BACKEND
                                val result = dispatchService.completeRide(
                                    ride.rideId
                                )

                                message = result.message
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Complete Ride")
                        }
                    }

                    Text(message)
                }

                Button(
                    onClick = {

                        // RESET BOOKING
                        selectedTODA = ""
                        passengerCount = 1
                        selectedRide = null
                        message = ""
                        requestFailed = false

                        currentScreen = "home"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}
