package backend

class Admin(
    private val username: String,
    private val password: String
) {
    fun login(
        enteredUsername: String,
        enteredPassword: String
    ): Boolean {
        return enteredUsername == username &&
                enteredPassword == password
    }
}
