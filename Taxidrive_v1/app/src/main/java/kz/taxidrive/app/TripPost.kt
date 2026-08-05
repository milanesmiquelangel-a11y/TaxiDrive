package kz.taxidrive.app

data class TripPost(

    val id: String = "",

    // PASSENGER o DRIVER
    val type: String = "",

    val origin: String = "",
    val destination: String = "",

    val date: String = "",
    val time: String = "",

    val phone: String = "",

    // ACTIVE, MATCHED, CANCELLED, FINISHED
    val status: String = "ACTIVE",

    val createdAt: Long = System.currentTimeMillis()
)