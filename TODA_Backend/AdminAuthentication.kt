package com.example.tricycledispatchandzoneapp.backend

class AdminAuthentication {

    private val admin = Admin(
        "admin",
        "admin123"
    )

    fun authenticate(
        username: String,
        password: String
    ): Boolean {

        if (username.isBlank() || password.isBlank()) {
            return false
        }

        return admin.login(username.trim(), password)
    }
}
