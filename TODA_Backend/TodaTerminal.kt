package com.example.tricycledispatchandzoneapp.backend

class TodaTerminal(
    val terminalName: String
) {

    private val serviceZones = mutableMapOf<String, ServiceZone>()

    init {
        initializeServiceZones()
    }

    private fun initializeServiceZones() {

        serviceZones["A"] = ServiceZone(
            "A",
            "Poblacion Core",
            "Short distance routes",
            15.00
        )

        serviceZones["B"] = ServiceZone(
            "B",
            "Highway / Subdivision",
            "Medium distance routes",
            0.00
        )

        serviceZones["C"] = ServiceZone(
            "C",
            "Bukid / Upland Area",
            "Long distance and rough road routes",
            40.00
        )
    }

    fun getZone(zoneCode: String): ServiceZone? {
        return serviceZones[zoneCode.uppercase()]
    }

    fun getAllZones(): List<ServiceZone> {
        return serviceZones.values.toList()
    }

    fun getZoneQueue(zoneCode: String): List<Tricycle> {

        val zone = getZone(zoneCode)

        if (zone != null) {
            return zone.getQueue()
        }

        return emptyList()
    }
}
