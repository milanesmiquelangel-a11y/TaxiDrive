package kz.taxidrive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.screens.AdminScreen
import kz.taxidrive.app.screens.DriverScreen
import kz.taxidrive.app.screens.LoginScreen
import kz.taxidrive.app.screens.PassengerScreen
import kz.taxidrive.app.screens.OfferChatScreen
import kz.taxidrive.app.screens.RegisterScreen
import kz.taxidrive.app.auth.AuthRepository
import kz.taxidrive.app.ui.theme.TaxiDriveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaxiDriveTheme {
                TaxiDriveApp()
            }
        }
    }
}

@Composable
fun TaxiDriveApp() {
    val context = LocalContext.current
    var pantalla by remember { mutableStateOf("login") }
    var telefonoActual by remember { mutableStateOf("") }
    var perfilRecuperacion by remember { mutableStateOf<org.json.JSONObject?>(null) }
    var mensajeEstado by remember { mutableStateOf("") }
    var chatOfferId by remember { mutableIntStateOf(0) }
    var chatReturnScreen by remember { mutableStateOf("pasajero") }

    // Each account stays in the role returned by the authenticated backend.
    // Leaving a role screen ends the session instead of exposing a role chooser.
    val cerrarSesion: () -> Unit = {
        AuthRepository.clearSession(context)
        telefonoActual = ""
        perfilRecuperacion = null
        mensajeEstado = ""
        pantalla = "login"
    }

    when (pantalla) {
        "login" -> LoginScreen(
            abrirRegistro = { pantalla = "registro" },
            loginCorrecto = { telefono, role ->
                telefonoActual = telefono
                when (role) {
                    "passenger" -> pantalla = "pasajero"
                    "admin" -> pantalla = "admin"
                    "driver" -> {
                        mensajeEstado = ""
                        pantalla = "comprobando_conductor"
                        AuthRepository.getDriverProfile(context, { profile ->
                            val requiredFields = listOf(
                                "vehicle_make", "vehicle_model", "vehicle_year", "license_plate",
                                "front_photo_url", "steering_wheel_photo_url"
                            )
                            val incomplete = requiredFields.any { key ->
                                val value = profile.optString(key, "")
                                value.isBlank() || value == "null"
                            }
                            val rejected = profile.optString("approval_status") == "rejected"
                            if (incomplete || rejected) {
                                perfilRecuperacion = profile
                                pantalla = "recuperar_perfil"
                            } else {
                                pantalla = "conductor"
                            }
                        }, { error ->
                            mensajeEstado = error
                            pantalla = "error_conductor"
                        })
                    }
                    else -> {
                        AuthRepository.clearSession(context)
                        telefonoActual = ""
                        pantalla = "login"
                    }
                }
            }
        )

        "registro" -> RegisterScreen(
            volverLogin = cerrarSesion
        )

        "recuperar_perfil" -> RegisterScreen(
            volverLogin = cerrarSesion,
            recoveryMode = true,
            existingProfile = perfilRecuperacion
        )

        "comprobando_conductor" -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Comprobando el perfil del conductor...")
            }
        }

        "error_conductor" -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(mensajeEstado)
            Spacer(Modifier.height(16.dp))
            Button(onClick = cerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al inicio de sesión")
            }
        }

        "pasajero" -> PassengerScreen(
            volver = cerrarSesion,
            abrirChat = { offerId ->
                chatOfferId = offerId
                chatReturnScreen = "pasajero"
                pantalla = "chat_pasajero"
            }
        )

        "chat_pasajero" -> OfferChatScreen(
            offerId = chatOfferId,
            volver = { pantalla = chatReturnScreen }
        )

        "conductor" -> DriverScreen(
            volver = cerrarSesion,
            abrirChat = { offerId ->
                chatOfferId = offerId
                chatReturnScreen = "conductor"
                pantalla = "chat_pasajero"
            },
            driverUid = telefonoActual
        )

        "admin" -> AdminScreen(
            volver = cerrarSesion
        )
    }
}