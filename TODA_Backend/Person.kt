package com.example.tricycledispatchandzoneapp

abstract class Person(
    val name: String,
    val personId: Int
) {

    abstract val role: String

    fun getPersonInformation(): String {
        return "Name: $name | ID: $personId | Role: $role"
    }
}
