package com.example.tricycledispatchandzoneapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog                    // NEW: enlarged map
import androidx.compose.ui.window.DialogProperties          // NEW: full-screen dialog
import androidx.compose.foundation.gestures.detectTransformGestures // NEW: pinch zoom
import androidx.compose.foundation.gestures.detectTapGestures       // NEW: double tap
import androidx.compose.ui.input.pointer.pointerInput       // NEW
import androidx.compose.ui.graphics.graphicsLayer           // NEW
import androidx.compose.ui.draw.clipToBounds                // NEW
import androidx.compose.ui.layout.onSizeChanged             // NEW
import androidx.compose.ui.unit.IntSize                     // NEW
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

import com.example.tricycledispatchandzoneapp.data.TODADatabase
import com.example.tricycledispatchandzoneapp.data.DriverEntity

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tricycledispatchandzoneapp.backend.AdminAuthentication
import com.example.tricycledispatchandzoneapp.backend.NasugbuCentralSystem
import com.example.tricycledispatchandzoneapp.backend.Ride
import com.example.tricycledispatchandzoneapp.worker.DriverSyncWorker

// ==========================================
// APP COLORS
// ==========================================

private val Navy = Color(0xFF102A43)
private val Teal = Color(0xFF087E78)
private val Amber = Color(0xFFF4A340)
private val Background = Color(0xFFF4F7FA)
private val White = Color.White
private val Gray = Color(0xFF64748B)
private val LightTeal = Color(0xFFE4F4F1)
private val LightAmber = Color(0xFFFFF3DF)
private val LightNavy = Color(0xFFE9EFF5)

// ==========================================
// MAIN ACTIVITY
// ==========================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Teal,
                    secondary = Amber,
                    background = Background,
                    surface = White
                )
            ) {
                AdminApp()
            }
        }
    }
}

// ==========================================
// MAIN ADMIN APPLICATION
// ==========================================

@Composable
fun AdminApp() {

    val centralSystem = remember { NasugbuCentralSystem() }
    val authentication = remember { AdminAuthentication() }

    // ==========================================
    // ROOM DATABASE CONNECTION
    // ==========================================

    val context = LocalContext.current.applicationContext

    val driverDao = remember {
        TODADatabase.getDatabase(context).driverDao()
    }

    val coroutineScope = rememberCoroutineScope()

    // ==========================================
    // LOGIN STATE
    // ==========================================

    var currentScreen by remember { mutableStateOf("login") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }

    var selectedBarangay by remember { mutableStateOf("") }
    var barangayDropdownExpanded by remember { mutableStateOf(false) }
    var selectedZone by remember { mutableStateOf("A") }
    var selectedRide by remember { mutableStateOf<Ride?>(null) }

    var showLogbookRefreshDialog by remember {
        mutableStateOf(false)
    }

    // ==========================================
    // FORM INPUTS
    // ==========================================

    var bodyNumber by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var driverId by remember { mutableStateOf("") }
    var passengerCapacity by remember { mutableStateOf("3") }
    var passengerCount by remember { mutableStateOf("1") }
    var cashReceived by remember { mutableStateOf("") }
    var fareInput by remember { mutableStateOf("") }

    // ==========================================
    // MESSAGES AND REFRESH
    // ==========================================

    var message by remember { mutableStateOf("") }
    var messageSuccess by remember { mutableStateOf(false) }

    var refreshKey by remember { mutableIntStateOf(0) }

    // ==========================================
    // RESTORE DRIVERS FROM ROOM DATABASE
    // RUNS WHEN THE APP OPENS
    // ==========================================

    LaunchedEffect(driverDao) {

        try {

            val savedDrivers = driverDao.getAllDrivers().first()

            for ((driverId1, name, barangay, bodyNumber1, zoneCode, capacity, available) in savedDrivers) {

                // Ignore old records without tricycle information.
                if (
                    bodyNumber1.isBlank() ||
                    zoneCode.isBlank()
                ) {
                    continue
                }

                // Only restore available drivers.
                if (!available) {
                    continue
                }

                val savedDriverId = driverId1.toIntOrNull() ?: continue

                // Return the tricycle to its original queue.
                centralSystem.timeInTricycle(
                    barangayName = barangay,
                    zoneCode = zoneCode,
                    bodyNumber = bodyNumber1,
                    driverName = name,
                    driverId = savedDriverId,
                    passengerCapacity = capacity
                )
            }

            // Refresh the UI after restoration.
            refreshKey++

        } catch (e: Exception) {

            message = "Unable to restore saved drivers: ${
                e.localizedMessage ?: "Unknown error"
            }"

            messageSuccess = false
        }
    }

    // ==========================================
    // BACKEND INFORMATION
    // ==========================================

    val barangays = remember(refreshKey) {
        centralSystem.getAllBarangays()
    }

    var totalWaiting = 0

    for (barangay in barangays) {
        for (zone in barangay.terminal.getAllZones()) {
            totalWaiting += zone.getQueueSize()
        }
    }

    val totalTrips = centralSystem.getTotalTrips()
    val ongoingTrips = centralSystem.getOngoingRides().size
    val completedTrips = centralSystem.getCompletedRides().size
    val totalCollections = centralSystem.getTotalCollections()

    // ==========================================
    // MAIN SURFACE
    // ==========================================

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {

            // ==========================================
            // 1. ADMIN LOGIN SCREEN
            // ==========================================

            if (currentScreen == "login") {

                Spacer(modifier = Modifier.height(45.dp))

                Image(
                    painter = painterResource(
                        id = R.drawable.tricycle_logo
                    ),
                    contentDescription = "Tricycle Dispatch Logo",
                    modifier = Modifier
                        .size(190.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )

                Text(
                    "Admin Login",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Navy,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Welcome to the Tricycle Dispatch and Service Zone Management System. Please enter your administrator credentials.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Gray,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {

                        Text(
                            "ADMINISTRATOR ACCOUNT",
                            color = Teal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Username")
                            },
                            placeholder = {
                                Text("Enter admin username")
                            },
                            singleLine = true,
                            enabled = !isLoggedIn
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Password")
                            },
                            placeholder = {
                                Text("Enter admin password")
                            },
                            singleLine = true,
                            visualTransformation = if (showPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                TextButton(
                                    onClick = {
                                        showPassword = !showPassword
                                    }
                                ) {
                                    Text(
                                        if (showPassword) "HIDE" else "SHOW",
                                        color = Teal,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        )

                        PrimaryButton(
                            text = "Login as Administrator",
                            onClick = {

                                if (username.isBlank() || password.isBlank()) {

                                    message = "Please enter your username and password."
                                    messageSuccess = false

                                } else {

                                    val loginSuccessful =
                                        authentication.authenticate(
                                            username,
                                            password
                                        )

                                    if (loginSuccessful) {

                                        isLoggedIn = true
                                        currentScreen = "barangaySelection"

                                        username = ""
                                        password = ""

                                        message = ""
                                        messageSuccess = true

                                    } else {

                                        message = "Incorrect username or password. Please try again."
                                        messageSuccess = false
                                    }
                                }
                            }
                        )

                        if (message.isNotEmpty()) {
                            MessageCard(message, messageSuccess)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    "AUTHORIZED ADMINISTRATORS ONLY",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // ==========================================
            // ADMIN FEATURES
            // ==========================================

            if (isLoggedIn) {

                // ==========================================
                // LOGBOOK REFRESH CONFIRMATION
                // ==========================================

                if (
                    currentScreen == "logbook" &&
                    showLogbookRefreshDialog
                ) {

                    AlertDialog(
                        onDismissRequest = {
                            showLogbookRefreshDialog = false
                        },
                        title = {
                            Text("Refresh Logbook?")
                        },
                        text = {
                            Text(
                                "Are you sure you want to refresh the Ride Logbook for $selectedBarangay?"
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {

                                    refreshKey++

                                    message = "Logbook refreshed successfully."
                                    messageSuccess = true

                                    showLogbookRefreshDialog = false
                                }
                            ) {
                                Text("Yes, Refresh")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showLogbookRefreshDialog = false
                                }
                            ) {
                                Text("Cancel")
                            }
                        },
                        containerColor = White,
                        titleContentColor = Navy,
                        textContentColor = Gray
                    )
                }

                when (currentScreen) {

                    // =================================
                    // 2. BARANGAY SELECTION
                    // =================================

                    "barangaySelection" -> {

                        Spacer(modifier = Modifier.height(28.dp))

                        AppHeader(subtitle = "TERMINAL ACCESS")

                        Text(
                            "Select Barangay Terminal",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Text(
                            "Choose the barangay where you are currently assigned. All terminal operations will use this selection.",
                            color = Gray,
                            lineHeight = 22.sp
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = White
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                Text(
                                    "BARANGAY TERMINAL",
                                    color = Teal,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    OutlinedTextField(
                                        value = selectedBarangay,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = {
                                            Text("Choose barangay")
                                        },
                                        placeholder = {
                                            Text("Select from registered barangays")
                                        },
                                        trailingIcon = {
                                            Text(
                                                if (barangayDropdownExpanded) "▲" else "▼",
                                                color = Teal
                                            )
                                        },
                                        singleLine = true
                                    )

                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable {
                                                barangayDropdownExpanded = true
                                            }
                                    )

                                    DropdownMenu(
                                        expanded = barangayDropdownExpanded,
                                        onDismissRequest = {
                                            barangayDropdownExpanded = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth(0.88f)
                                            .heightIn(max = 320.dp)
                                            .background(White)
                                    ) {

                                        for (barangay in barangays) {

                                            DropdownMenuItem(
                                                text = {
                                                    Text(barangay.barangayName)
                                                },
                                                onClick = {

                                                    selectedBarangay =
                                                        barangay.barangayName

                                                    barangayDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Text(
                                    "Registered terminals: ${barangays.size}",
                                    color = Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        PrimaryButton(
                            text = "Continue to Admin Dashboard",
                            onClick = {

                                if (selectedBarangay.isNotBlank()) {

                                    currentScreen = "dashboard"
                                    message = ""

                                } else {

                                    message = "Please select a barangay before continuing."
                                    messageSuccess = false
                                }
                            }
                        )

                        if (message.isNotEmpty()) {
                            MessageCard(message, messageSuccess)
                        }
                    }

                    // =================================
                    // 3. ADMIN DASHBOARD
                    // =================================

                    "dashboard" -> {

                        AppHeader(subtitle = "ADMIN CONTROL PANEL")

                        Text(
                            "Dashboard",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Text(
                            "Welcome back, Administrator.",
                            color = Gray
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(17.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = LightTeal
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {

                                Text(
                                    "CURRENTLY MANAGING",
                                    color = Teal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )

                                Text(
                                    selectedBarangay,
                                    color = Navy,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "$selectedBarangay TODA Terminal",
                                    color = Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                selectedBarangay = ""
                                currentScreen = "barangaySelection"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Change Barangay Terminal")
                        }

                        Text(
                            "SYSTEM OVERVIEW",
                            color = Gray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            StatCard(
                                number = centralSystem.getTotalBarangays().toString(),
                                label = "Barangays",
                                modifier = Modifier.weight(1f)
                            )

                            StatCard(
                                number = totalWaiting.toString(),
                                label = "Waiting Tricycles",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            StatCard(
                                number = ongoingTrips.toString(),
                                label = "Ongoing Trips",
                                modifier = Modifier.weight(1f)
                            )

                            StatCard(
                                number = completedTrips.toString(),
                                label = "Completed Trips",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        InfoCard(
                            title = "Total Dispatches",
                            description = "$totalTrips recorded trips"
                        )

                        InfoCard(
                            title = "Total Collections",
                            description = "PHP ${"%.2f".format(totalCollections)}"
                        )

                        Text(
                            "ADMIN OPERATIONS",
                            color = Gray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        MenuCard(
                            title = "Terminal Operations",
                            description = "Manage service zones and fares, register drivers, and monitor the tricycle queue for $selectedBarangay.",
                            iconText = "01",
                            onClick = {
                                currentScreen = "terminal"
                            }
                        )

                        MenuCard(
                            title = "Passenger Dispatch",
                            description = "Record passenger details, receive cash payment, and dispatch the next tricycle.",
                            iconText = "02",
                            onClick = {
                                currentScreen = "dispatch"
                            }
                        )

                        MenuCard(
                            title = "Ride Logbook",
                            description = "View and update ongoing and completed trip records.",
                            iconText = "03",
                            onClick = {
                                currentScreen = "logbook"
                                showLogbookRefreshDialog = false
                                message = ""
                            }
                        )

                        OutlinedButton(
                            onClick = {

                                isLoggedIn = false

                                selectedBarangay = ""
                                selectedZone = "A"
                                selectedRide = null

                                username = ""
                                password = ""

                                message = ""
                                showLogbookRefreshDialog = false

                                currentScreen = "login"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Text("Logout Admin")
                        }
                    }

                    // =================================
                    // 4. TERMINAL DASHBOARD
                    // =================================

                    "terminal" -> {

                        val barangay =
                            centralSystem.getBarangay(selectedBarangay)

                        AppHeader(subtitle = "TERMINAL OPERATIONS")

                        Text(
                            selectedBarangay,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Text("Barangay TODA Terminal", color = Gray)

                        Text(
                            "Manage service zones, driver time-in, and waiting tricycle queues from this page.",
                            color = Gray,
                            lineHeight = 21.sp
                        )

                        InfoCard(
                            title = "Terminal Information",
                            description = "Barangay: $selectedBarangay\nTerminal: $selectedBarangay TODA Terminal\nService Zones: 3"
                        )

                        // ===== NEW: MAP ON TERMINAL SCREEN =====
                        ZoneMapCard()

                        Text(
                            "SERVICE ZONES",
                            color = Gray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        if (barangay != null) {

                            val zones = barangay.terminal
                                .getAllZones()
                                .sortedBy { it.zoneCode }

                            for (zone in zones) {

                                val fareText = if (zone.baseFare > 0) {
                                    "PHP ${"%.2f".format(zone.baseFare)}"
                                } else {
                                    "Not configured"
                                }

                                MenuCard(
                                    title = "Zone ${zone.zoneCode} - ${zone.zoneName}",
                                    description = "${zone.description}\nBase Fare: $fareText\nWaiting Tricycles: ${zone.getQueueSize()}",
                                    iconText = zone.zoneCode,
                                    onClick = {

                                        selectedZone = zone.zoneCode

                                        fareInput = if (zone.baseFare > 0) {
                                            zone.baseFare.toString()
                                        } else {
                                            ""
                                        }

                                        message = ""
                                        currentScreen = "zone"
                                    }
                                )
                            }

                        } else {

                            InfoCard(
                                title = "Terminal Not Found",
                                description = "The selected barangay terminal could not be loaded."
                            )
                        }

                        PrimaryButton(
                            text = "Register Driver / Tricycle (Time-In)",
                            onClick = {
                                currentScreen = "timein"
                            }
                        )

                        PrimaryButton(
                            text = "Manage Tricycle Queue",
                            onClick = {
                                currentScreen = "queue"
                            }
                        )

                        BackButton {
                            currentScreen = "dashboard"
                        }
                    }

                    // =================================
                    // 5. SERVICE ZONE DETAILS
                    // =================================

                    "zone" -> {

                        val zone = centralSystem
                            .getBarangay(selectedBarangay)
                            ?.terminal
                            ?.getZone(selectedZone)

                        AppHeader(subtitle = "SERVICE ZONE INFORMATION")

                        Text(
                            "Zone $selectedZone",
                            fontSize = 29.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Text(zone?.zoneName ?: "Unknown Zone", color = Gray)

                        if (zone != null) {

                            InfoCard(
                                title = "Zone Description",
                                description = zone.description
                            )

                            StatCard(
                                number = zone.getQueueSize().toString(),
                                label = "Waiting Tricycles"
                            )

                            InfoCard(
                                title = "Current Base Fare",
                                description = "PHP ${"%.2f".format(zone.baseFare)}"
                            )

                            Text(
                                "CONFIGURE ZONE FARE",
                                color = Gray,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = fareInput,
                                onValueChange = {
                                    fareInput = it
                                },
                                label = {
                                    Text("New Base Fare (PHP)")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            PrimaryButton(
                                text = "Save Zone Fare",
                                onClick = {

                                    val fare = fareInput.toDoubleOrNull()

                                    if (fare == null || fare <= 0) {

                                        message = "Enter a valid fare."
                                        messageSuccess = false

                                    } else {

                                        val saved = centralSystem.setZoneFare(
                                            selectedBarangay,
                                            selectedZone,
                                            fare
                                        )

                                        messageSuccess = saved

                                        message = if (saved) {
                                            "Zone fare successfully updated."
                                        } else {
                                            "Unable to update zone fare."
                                        }

                                        refreshKey++
                                    }
                                }
                            )

                            if (message.isNotEmpty()) {
                                MessageCard(message, messageSuccess)
                            }

                        } else {

                            InfoCard(
                                title = "Zone Not Found",
                                description = "This service zone is not available."
                            )
                        }

                        BackButton {
                            currentScreen = "terminal"
                        }
                    }

                    // =================================
                    // 6. DRIVER TIME-IN
                    // =================================

                    "timein" -> {

                        AppHeader(subtitle = "DRIVER REGISTRATION")

                        Text(
                            "Driver Time-In",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Text(
                            "Enter the driver's information and assign the tricycle to its service zone.",
                            color = Gray,
                            lineHeight = 22.sp
                        )

                        InfoCard(
                            title = "Selected Barangay Terminal",
                            description = "$selectedBarangay TODA Terminal"
                        )

                        FormLabel("SERVICE ZONE")

                        // ===== NEW: MAP ON TIME-IN SCREEN =====
                        ZoneMapCard()

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            for (code in listOf("A", "B", "C")) {

                                Button(
                                    onClick = {
                                        selectedZone = code
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedZone == code) Teal else LightNavy,
                                        contentColor = if (selectedZone == code) White else Navy
                                    )
                                ) {
                                    Text("Zone $code")
                                }
                            }
                        }

                        FormLabel("TRICYCLE BODY NUMBER")

                        OutlinedTextField(
                            value = bodyNumber,
                            onValueChange = {
                                bodyNumber = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Example: 055")
                            },
                            singleLine = true
                        )

                        FormLabel("DRIVER NAME")

                        OutlinedTextField(
                            value = driverName,
                            onValueChange = {
                                driverName = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Enter driver name")
                            },
                            singleLine = true
                        )

                        FormLabel("DRIVER ID")

                        OutlinedTextField(
                            value = driverId,
                            onValueChange = {
                                driverId = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Enter driver ID")
                            },
                            singleLine = true
                        )

                        FormLabel("PASSENGER CAPACITY")

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            for (capacity in 1..5) {

                                Button(
                                    onClick = {
                                        passengerCapacity = capacity.toString()
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(5.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (passengerCapacity == capacity.toString()) Teal else LightNavy,
                                        contentColor = if (passengerCapacity == capacity.toString()) White else Navy
                                    )
                                ) {
                                    Text(capacity.toString())
                                }
                            }
                        }

                        PrimaryButton(
                            text = "Confirm Driver Time-In",
                            onClick = {

                                val id = driverId.toIntOrNull()
                                val capacity = passengerCapacity.toIntOrNull()

                                if (
                                    id == null ||
                                    driverName.isBlank() ||
                                    bodyNumber.isBlank() ||
                                    selectedBarangay.isBlank() ||
                                    capacity == null
                                ) {

                                    message = "Please complete all required fields."
                                    messageSuccess = false

                                } else {

                                    val result = centralSystem.timeInTricycle(
                                        selectedBarangay,
                                        selectedZone,
                                        bodyNumber.trim(),
                                        driverName.trim(),
                                        id,
                                        capacity
                                    )

                                    if (result.successful) {

                                        coroutineScope.launch {

                                            try {

                                                // SAVE DRIVER TO ROOM
                                                driverDao.insertDriver(
                                                    DriverEntity(
                                                        driverId = id.toString(),
                                                        name = driverName.trim(),
                                                        barangay = selectedBarangay,
                                                        bodyNumber = bodyNumber.trim(),
                                                        zoneCode = selectedZone,
                                                        capacity = capacity,
                                                        available = true,
                                                        needsSync = true
                                                    )
                                                )

                                                // FIREBASE SYNC THROUGH WORKMANAGER
                                                val constraints =
                                                    Constraints.Builder()
                                                        .setRequiredNetworkType(
                                                            NetworkType.CONNECTED
                                                        )
                                                        .build()

                                                val syncRequest =
                                                    OneTimeWorkRequestBuilder<DriverSyncWorker>()
                                                        .setConstraints(constraints)
                                                        .build()

                                                WorkManager.getInstance(context)
                                                    .enqueue(syncRequest)

                                                message = "${result.message} Driver information was saved locally. Firebase sync will run when internet is available."

                                                messageSuccess = true

                                                bodyNumber = ""
                                                driverName = ""
                                                driverId = ""
                                                passengerCapacity = "3"

                                            } catch (e: Exception) {

                                                message = "${result.message} However, saving the driver to the local database failed: ${
                                                    e.localizedMessage ?: "Unknown error"
                                                }"

                                                messageSuccess = false
                                            }

                                            refreshKey++
                                        }

                                    } else {

                                        message = result.message
                                        messageSuccess = false

                                        refreshKey++
                                    }
                                }
                            }
                        )

                        if (message.isNotEmpty()) {
                            MessageCard(message, messageSuccess)
                        }

                        BackButton {
                            currentScreen = "terminal"
                        }
                    }

                    // =================================
                    // 7. FIFO QUEUE MANAGEMENT
                    // =================================

                    "queue" -> {

                        AppHeader(subtitle = "FIFO QUEUE MANAGEMENT")

                        Text(
                            "Tricycle Queue",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Text(
                            "Monitor the waiting tricycles in each service zone.",
                            color = Gray
                        )

                        InfoCard(
                            title = "Selected Barangay",
                            description = "$selectedBarangay TODA Terminal"
                        )

                        val barangay =
                            centralSystem.getBarangay(selectedBarangay)

                        if (barangay != null) {

                            val zones = barangay.terminal
                                .getAllZones()
                                .sortedBy { it.zoneCode }

                            for (zone in zones) {

                                Text(
                                    "ZONE ${zone.zoneCode} - ${zone.zoneName}",
                                    color = Teal,
                                    fontWeight = FontWeight.Bold
                                )

                                val queue = zone.getQueue()

                                if (queue.isEmpty()) {

                                    InfoCard(
                                        title = "No Waiting Tricycles",
                                        description = "There are currently no tricycles in this zone."
                                    )

                                } else {

                                    for ((index, tricycle) in queue.withIndex()) {

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(15.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = White
                                            )
                                        ) {

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(15.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .background(
                                                            if (index == 0) LightAmber else LightTeal,
                                                            RoundedCornerShape(12.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "${index + 1}",
                                                        color = Navy,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column(
                                                    modifier = Modifier.weight(1f)
                                                ) {

                                                    Text(
                                                        "Tricycle #${tricycle.bodyNumber}",
                                                        color = Navy,
                                                        fontWeight = FontWeight.Bold
                                                    )

                                                    Text(
                                                        tricycle.driver.name,
                                                        color = Gray,
                                                        fontSize = 12.sp
                                                    )

                                                    Text(
                                                        "Capacity: ${tricycle.passengerCapacity}",
                                                        color = Gray,
                                                        fontSize = 12.sp
                                                    )
                                                }

                                                if (index == 0) {
                                                    StatusBadge("NEXT")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        } else {

                            InfoCard(
                                title = "Terminal Not Found",
                                description = "Return to Barangay Selection and choose a registered terminal."
                            )
                        }

                        PrimaryButton(
                            text = "Refresh Queue",
                            onClick = {

                                refreshKey++

                                message = "Queue information refreshed."
                                messageSuccess = true
                            }
                        )

                        if (message.isNotEmpty()) {
                            MessageCard(message, messageSuccess)
                        }

                        BackButton {
                            currentScreen = "terminal"
                        }
                    }

                    // =================================
                    // 8. PASSENGER PAYMENT & DISPATCH
                    // =================================

                    "dispatch" -> {

                        AppHeader(subtitle = "PASSENGER DISPATCH")

                        Text(
                            "Dispatch Next Tricycle",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Text(
                            "Enter the passenger's details and collect the cash payment before dispatching.",
                            color = Gray,
                            lineHeight = 22.sp
                        )

                        InfoCard(
                            title = "Selected Barangay Terminal",
                            description = "$selectedBarangay TODA Terminal"
                        )

                        FormLabel("DESTINATION / SERVICE ZONE")

                        // ===== NEW: MAP ON DISPATCH SCREEN (MAIN SPOT) =====
                        ZoneMapCard()

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            for (code in listOf("A", "B", "C")) {

                                Button(
                                    onClick = {
                                        selectedZone = code
                                        refreshKey++
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedZone == code) Teal else LightNavy,
                                        contentColor = if (selectedZone == code) White else Navy
                                    )
                                ) {
                                    Text("Zone $code")
                                }
                            }
                        }

                        val zone = centralSystem
                            .getBarangay(selectedBarangay)
                            ?.terminal
                            ?.getZone(selectedZone)

                        if (zone != null) {

                            InfoCard(
                                title = zone.zoneName,
                                description = "Base Fare: PHP ${"%.2f".format(zone.baseFare)}\nWaiting Tricycles: ${zone.getQueueSize()}"
                            )

                            val nextTricycle = zone.peekNext()

                            InfoCard(
                                title = "Next Tricycle in Queue",
                                description = if (nextTricycle != null) {
                                    "Body Number: #${nextTricycle.bodyNumber}\nDriver: ${nextTricycle.driver.name}\nPassenger Capacity: ${nextTricycle.passengerCapacity}"
                                } else {
                                    "No waiting tricycle available."
                                }
                            )

                        } else {

                            InfoCard(
                                title = "Terminal Information",
                                description = "The selected barangay terminal or service zone could not be found."
                            )
                        }

                        FormLabel("NUMBER OF PASSENGERS")

                        OutlinedTextField(
                            value = passengerCount,
                            onValueChange = {
                                passengerCount = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Enter passenger count")
                            },
                            singleLine = true
                        )

                        FormLabel("CASH RECEIVED (PHP)")

                        OutlinedTextField(
                            value = cashReceived,
                            onValueChange = {
                                cashReceived = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Enter amount received")
                            },
                            singleLine = true
                        )

                        Card(
                            shape = RoundedCornerShape(15.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = LightAmber
                            )
                        ) {
                            Text(
                                "Reminder: The passenger pays the admin directly before the tricycle is dispatched.",
                                modifier = Modifier.padding(15.dp),
                                color = Navy,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }

                        PrimaryButton(
                            text = "Dispatch Next - Zone $selectedZone",
                            onClick = {

                                val passengers = passengerCount.toIntOrNull()
                                val cash = cashReceived.toDoubleOrNull()

                                if (passengers == null || cash == null) {

                                    message = "Please enter valid passenger and payment information."
                                    messageSuccess = false

                                } else {

                                    val result = centralSystem.dispatchNext(
                                        selectedBarangay,
                                        selectedZone,
                                        passengers,
                                        cash
                                    )

                                    message = result.message
                                    messageSuccess = result.successful

                                    if (result.successful && result.ride != null) {

                                        selectedRide = result.ride
                                        currentScreen = "ticket"

                                        cashReceived = ""
                                        passengerCount = "1"
                                    }

                                    refreshKey++
                                }
                            }
                        )

                        if (message.isNotEmpty()) {
                            MessageCard(message, messageSuccess)
                        }

                        BackButton {
                            currentScreen = "dashboard"
                        }
                    }

                    // =================================
                    // 9. DISPATCH TICKET
                    // =================================

                    "ticket" -> {

                        val ride = selectedRide

                        AppHeader(subtitle = "DISPATCH CONFIRMATION")

                        Text(
                            "Dispatch Successful!",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Text(
                            "The next tricycle has been successfully dispatched.",
                            color = Gray
                        )

                        if (ride != null) {

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Teal
                                )
                            ) {

                                Column(
                                    modifier = Modifier.padding(22.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {

                                    Text(
                                        "ASSIGNED TRICYCLE",
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )

                                    Text(
                                        "#${ride.assignedTricycle.bodyNumber}",
                                        color = White,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )

                                    Text(
                                        "Driver: ${ride.assignedTricycle.driver.name}",
                                        color = White
                                    )

                                    HorizontalDivider(
                                        color = White.copy(alpha = 0.4f)
                                    )

                                    Text(
                                        "ZONE ${ride.zoneCode}",
                                        color = Amber,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        "PAYMENT: PAID",
                                        color = White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            InfoCard(
                                title = "Payment Summary",
                                description = "Fare: PHP ${"%.2f".format(ride.fare)}\nCash Received: PHP ${"%.2f".format(ride.cashReceived)}\nChange: PHP ${"%.2f".format(ride.change)}"
                            )

                            InfoCard(
                                title = "Ride Information",
                                description = "Ride ID: ${ride.rideId}\nBarangay: ${ride.barangayName}\nZone: ${ride.zoneCode} - ${ride.zoneName}\nPassengers: ${ride.passengerCount}\nStatus: ${ride.status}"
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(15.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = White
                                )
                            ) {

                                Column(
                                    modifier = Modifier.padding(15.dp)
                                ) {

                                    Text(
                                        "PHYSICAL TICKET / STUB",
                                        color = Teal,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        "TRICYCLE #${ride.assignedTricycle.bodyNumber} - ZONE ${ride.zoneCode} - PAID",
                                        color = Navy,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp
                                    )

                                    Text(
                                        "Give this ticket to the passenger or use it as a dispatch reference.",
                                        color = Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            PrimaryButton(
                                text = "View Complete Ticket",
                                onClick = {
                                    message = ride.generateTicket()
                                    currentScreen = "ticketDetails"
                                }
                            )
                        }

                        PrimaryButton(
                            text = "Return to Dashboard",
                            onClick = {
                                selectedRide = null
                                message = ""
                                currentScreen = "dashboard"
                            }
                        )
                    }

                    // =================================
                    // 10. FULL TICKET DETAILS
                    // =================================

                    "ticketDetails" -> {

                        AppHeader(subtitle = "PHYSICAL DISPATCH TICKET")

                        Text(
                            "Ticket Details",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = White
                            )
                        ) {
                            Text(
                                selectedRide?.generateTicket()
                                    ?: "No ticket available.",
                                modifier = Modifier.padding(18.dp),
                                color = Navy,
                                fontSize = 14.sp,
                                lineHeight = 23.sp
                            )
                        }

                        PrimaryButton(
                            text = "Back to Dispatch Result",
                            onClick = {
                                currentScreen = "ticket"
                            }
                        )
                    }

                    // =================================
                    // 11. RIDE LOGBOOK
                    // =================================

                    "logbook" -> {

                        AppHeader(subtitle = "TERMINAL LOGBOOK")

                        Text(
                            "Ride Logbook",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Text(
                            "Monitor and manage all recorded dispatch transactions.",
                            color = Gray,
                            lineHeight = 22.sp
                        )

                        val terminalRideRecords = centralSystem
                            .getAllRides()
                            .filter {
                                it.barangayName == selectedBarangay
                            }

                        val terminalCompletedCount =
                            terminalRideRecords.count {
                                it.status == "COMPLETED"
                            }

                        val terminalCollections =
                            terminalRideRecords.sumOf {
                                it.fare
                            }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            StatCard(
                                number = terminalRideRecords.size.toString(),
                                label = "Total Trips",
                                modifier = Modifier.weight(1f)
                            )

                            StatCard(
                                number = terminalCompletedCount.toString(),
                                label = "Completed",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        InfoCard(
                            title = "Total Fare Collections",
                            description = "PHP ${"%.2f".format(terminalCollections)}"
                        )

                        val rides = terminalRideRecords.reversed()

                        if (rides.isEmpty()) {

                            InfoCard(
                                title = "No Records Yet",
                                description = "Dispatched trips for $selectedBarangay will appear here automatically."
                            )

                        } else {

                            for (ride in rides) {

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(17.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = White
                                    )
                                ) {

                                    Column(
                                        modifier = Modifier.padding(17.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Text(
                                                "RIDE #${ride.rideId}",
                                                color = Navy,
                                                fontWeight = FontWeight.Bold
                                            )

                                            StatusBadge(ride.status)
                                        }

                                        Text(
                                            "Tricycle #${ride.assignedTricycle.bodyNumber}",
                                            color = Teal,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )

                                        Text(
                                            "Driver: ${ride.assignedTricycle.driver.name}\nBarangay: ${ride.barangayName}\nZone: ${ride.zoneCode} - ${ride.zoneName}\nPassengers: ${ride.passengerCount}\nFare: PHP ${"%.2f".format(ride.fare)}\nPayment: PAID",
                                            color = Gray,
                                            fontSize = 13.sp,
                                            lineHeight = 21.sp
                                        )

                                        if (ride.status == "ONGOING") {

                                            Button(
                                                onClick = {

                                                    val result =
                                                        centralSystem.completeRide(ride.rideId)

                                                    message = result.message
                                                    messageSuccess = result.successful

                                                    refreshKey++
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Teal
                                                )
                                            ) {
                                                Text("Mark Ride as Completed")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (message.isNotEmpty()) {
                            MessageCard(message, messageSuccess)
                        }

                        PrimaryButton(
                            text = "Refresh Logbook",
                            onClick = {
                                showLogbookRefreshDialog = true
                            }
                        )

                        BackButton {
                            showLogbookRefreshDialog = false
                            currentScreen = "dashboard"
                        }
                    }
                }

                // ==========================================
                // FOOTER
                // ==========================================

                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(color = Color.LightGray)

                Text(
                    "TRICYCLE DISPATCH & ZONE APP",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Gray,
                    letterSpacing = 1.sp
                )

                Text(
                    "NASUGBU, BATANGAS • ADMIN SYSTEM",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 9.sp,
                    color = Gray
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

// ==========================================
// REUSABLE APP HEADER
// ==========================================

@Composable
fun AppHeader(subtitle: String) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(
                id = R.drawable.tricycle_logo
            ),
            contentDescription = "Tricycle Dispatch Logo",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {

            Text(
                "TRICYCLE DISPATCH",
                color = Navy,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )

            Text(
                subtitle,
                color = Gray,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==========================================
// NEW: REUSABLE ZONE MAP CARD
// Small preview on the screen. Tap to open the
// full-screen zoomable map.
// Requires: res/drawable/nasugbu_map.png
// ==========================================

@Composable
fun ZoneMapCard() {

    var enlarged by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { enlarged = true },
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                "NASUGBU SERVICE AREA MAP",
                color = Teal,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )

            Image(
                painter = painterResource(
                    id = R.drawable.nasugbu_map
                ),
                contentDescription = "Map of Nasugbu",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Text(
                "Tap the map to enlarge and zoom",
                color = Gray,
                fontSize = 11.sp
            )
        }
    }

    if (enlarged) {
        ZoomableMapDialog(
            onDismiss = { enlarged = false }
        )
    }
}

// ==========================================
// NEW: FULL-SCREEN ZOOMABLE MAP
// - Pinch with two fingers to zoom in / out
// - Drag with one finger to move the map
// - Double-tap to zoom in, double-tap again to reset
// - + / - / Reset buttons as an alternative
// ==========================================

@Composable
fun ZoomableMapDialog(onDismiss: () -> Unit) {

    val minScale = 1f
    val maxScale = 6f

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    // Applies zoom and pan, and keeps the map inside the screen.
    fun applyZoom(newScale: Float, panX: Float, panY: Float) {

        val s = newScale.coerceIn(minScale, maxScale)

        val maxX = boxSize.width * (s - 1f) / 2f
        val maxY = boxSize.height * (s - 1f) / 2f

        offsetX = (offsetX + panX).coerceIn(-maxX, maxX)
        offsetY = (offsetY + panY).coerceIn(-maxY, maxY)
        scale = s
    }

    fun resetZoom() {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Navy)
        ) {

            // ---------- ZOOMABLE IMAGE ----------
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .onSizeChanged { boxSize = it }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            applyZoom(scale * zoom, pan.x, pan.y)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.1f) {
                                    resetZoom()
                                } else {
                                    applyZoom(2.5f, 0f, 0f)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(
                        id = R.drawable.nasugbu_map
                    ),
                    contentDescription = "Zoomable map of Nasugbu",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            // ---------- TOP BAR ----------
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Navy.copy(alpha = 0.85f))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        "NASUGBU MAP",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Pinch or double-tap to zoom, drag to move",
                        color = White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        "CLOSE",
                        color = Amber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ---------- BOTTOM CONTROLS ----------
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Navy.copy(alpha = 0.85f))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Button(
                    onClick = { applyZoom(scale / 1.5f, 0f, 0f) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightNavy,
                        contentColor = Navy
                    )
                ) {
                    Text("−  Zoom Out", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { applyZoom(scale * 1.5f, 0f, 0f) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal,
                        contentColor = White
                    )
                ) {
                    Text("+  Zoom In", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { resetZoom() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = White
                    )
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

// ==========================================
// REUSABLE PRIMARY BUTTON
// ==========================================

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Teal,
            contentColor = White
        )
    ) {

        Text(
            text,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

// ==========================================
// REUSABLE BACK BUTTON
// ==========================================

@Composable
fun BackButton(onClick: () -> Unit) {

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text("Back")
    }
}

// ==========================================
// REUSABLE INFORMATION CARD
// ==========================================

@Composable
fun InfoCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {

            Text(
                title,
                color = Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                description,
                color = Gray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// ==========================================
// REUSABLE STATISTICS CARD
// ==========================================

@Composable
fun StatCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        )
    ) {

        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                number,
                color = Teal,
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                label,
                color = Gray,
                fontSize = 12.sp
            )
        }
    }
}

// ==========================================
// REUSABLE MENU CARD
// ==========================================

@Composable
fun MenuCard(
    title: String,
    description: String,
    iconText: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        LightTeal,
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    iconText,
                    color = Teal,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    title,
                    color = Navy,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    description,
                    color = Gray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Text(
                "›",
                color = Teal,
                fontSize = 25.sp
            )
        }
    }
}

// ==========================================
// REUSABLE FORM LABEL
// ==========================================

@Composable
fun FormLabel(text: String) {

    Text(
        text,
        color = Navy,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

// ==========================================
// REUSABLE STATUS BADGE
// ==========================================

@Composable
fun StatusBadge(text: String) {

    val isNext = text == "NEXT"
    val isOngoing = text == "ONGOING"

    Box(
        modifier = Modifier
            .background(
                when {
                    isNext || isOngoing -> LightAmber
                    else -> LightTeal
                },
                RoundedCornerShape(8.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
    ) {

        Text(
            text,
            color = if (isNext || isOngoing) Navy else Teal,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==========================================
// REUSABLE MESSAGE CARD
// ==========================================

@Composable
fun MessageCard(
    message: String,
    successful: Boolean
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (successful) LightTeal else LightAmber
        )
    ) {

        Text(
            message,
            modifier = Modifier.padding(15.dp),
            color = Navy,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp
        )
    }
}
