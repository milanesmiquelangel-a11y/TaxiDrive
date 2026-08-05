package kz.taxidrive.app.model

data class Offer(

    val id: String = "",

    val userId: String = "",

    val nombre: String = "",

    val telefono: String = "",

    val marca: String = "",

    val modelo: String = "",

    val color: String = "",

    val origin: String = "",

    val destination: String = "",

    val date: String = "",

    val time: String = "",

    val price: String = "",

    val comment: String = "",

    val status: String = "activo",

    val createdAt: Long = System.currentTimeMillis()

)