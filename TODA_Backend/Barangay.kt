package com.example.tricycledispatchandzoneapp.backend

class Barangay(
    val barangayName: String
) {

    val terminal = TodaTerminal(
        "$barangayName TODA Terminal"
    )
}
