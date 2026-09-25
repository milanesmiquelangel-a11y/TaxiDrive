package kz.taxidrive.app.model

data class User(
    val uid: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val tipo: String = "",
    val aprobado: Boolean = false,
    val marca: String = "",
    val modelo: String = "",
    val color: String = "",
    val anio: String = "",
    val matricula: String = "",
    val licencia: String = "",
    val trialUntil: Long = 0L,
    val activationUntil: Long = 0L,
    val activationPending: Boolean = false,
    val activationAmount: Long = 500L
)