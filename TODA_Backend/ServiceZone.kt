package com.example.tricycledispatchandzoneapp

class ServiceZone(
    val zoneCode: String,
    val zoneName: String,
    val description: String,
    var baseFare: Double
) {

    private val tricycleQueue = mutableListOf<Tricycle>()

    fun timeIn(tricycle: Tricycle): Boolean {

        if (tricycle.passengerCapacity < 1) return false
        if (tricycle.status != "AVAILABLE") return false
        if (containsBodyNumber(tricycle.bodyNumber)) return false

        if (!tricycle.setWaiting()) return false

        tricycleQueue.add(tricycle)
        return true
    }

    fun containsBodyNumber(bodyNumber: String): Boolean {

        for (tricycle in tricycleQueue) {

            if (tricycle.bodyNumber.equals(
                    bodyNumber,
                    ignoreCase = true
                )
            ) {
                return true
            }
        }

        return false
    }

    fun peekNext(): Tricycle? {
        return tricycleQueue.firstOrNull()
    }

    fun dispatchNext(): Tricycle? {

        if (tricycleQueue.isEmpty()) return null

        return tricycleQueue.removeAt(0)
    }

    fun getQueue(): List<Tricycle> {
        return tricycleQueue.toList()
    }

    fun getQueueSize(): Int {
        return tricycleQueue.size
    }
}
