package kz.taxidrive.app.model

data class TripPost(

    val id: String = "",

    val type: String = "",          // pasajero | conductor

    val origin: String = "",

    val destination: String = "",

    val date: String = "",

    val time: String = "",

    val price: String = "",

    val comment: String = "",

    val phone: String = "",

    val userId: String = "",

    val status: String = "activo",

    val createdAt: Long = System.currentTimeMillis()

)