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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.taxidrive.app.screens.AdminScreen
import kz.taxidrive.app.screens.DriverScreen
import kz.taxidrive.app.screens.LoginScreen
import kz.taxidrive.app.screens.PassengerScreen
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
                    else -> pantalla = "login"
                }
            }
        )

        "registro" -> RegisterScreen(
            volverLogin = { pantalla = "login" }
        )

        "recuperar_perfil" -> RegisterScreen(
            volverLogin = { pantalla = "login" },
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
            Button(onClick = { pantalla = "login" }, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al inicio de sesión")
            }
        }

        "inicio" -> PantallaInicio(
            abrirPasajero = { pantalla = "pasajero" },
            abrirConductor = { pantalla = "conductor" },
            abrirAdministrador = { pantalla = "admin" }
        )

        "pasajero" -> PassengerScreen(
            volver = { pantalla = "inicio" }
        )

        "conductor" -> DriverScreen(
            volver = { pantalla = "inicio" },
            driverUid = telefonoActual
        )

        "admin" -> AdminScreen(
            volver = { pantalla = "inicio" }
        )
    }
}

@Composable
fun PantallaInicio(

    abrirPasajero: () -> Unit,

    abrirConductor: () -> Unit,

    abrirAdministrador: () -> Unit

) {

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center

        ) {

            Text(

                text = "🚖 TaxiDrive.kz",

                fontSize = 34.sp,

                fontWeight = FontWeight.Bold

            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(

                onClick = abrirPasajero,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Soy pasajero")

            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(

                onClick = abrirConductor,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Soy conductor")

            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(

                onClick = abrirAdministrador,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Administrador")

            }

            Spacer(modifier = Modifier.height(40.dp))

            Text("Idioma")

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🇪🇸 Español")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🇷🇺 Русский")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🇬🇧 English")
            }

        }

    }

}