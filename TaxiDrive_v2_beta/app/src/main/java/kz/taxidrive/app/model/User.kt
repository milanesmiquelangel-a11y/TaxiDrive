package kz.taxidrive.app.model

data class User(

    val uid: String = "",

    val nombre: String = "",

    val apellidos: String = "",

    val telefono: String = "",

    val tipo: String = "",      // pasajero | conductor | administrador

    val aprobado: Boolean = false,

    val marca: String = "",

    val modelo: String = "",

    val color: String = "",

    val anio: String = "",

    val matricula: String = "",

    val licencia: String = ""
)