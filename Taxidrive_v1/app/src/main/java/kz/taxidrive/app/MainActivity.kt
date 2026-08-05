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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kz.taxidrive.app.screens.AdminScreen
import kz.taxidrive.app.screens.DriverScreen
import kz.taxidrive.app.screens.LoginScreen
import kz.taxidrive.app.screens.PassengerScreen
import kz.taxidrive.app.screens.RegisterScreen
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

    var pantalla by remember {
        mutableStateOf("login")
    }

    when (pantalla) {

        "login" -> LoginScreen(
            abrirRegistro = {
                pantalla = "registro"
            },
            loginCorrecto = {
                pantalla = "inicio"
            }
        )

        "registro" -> RegisterScreen(
            volverLogin = {
                pantalla = "login"
            }
        )

        "inicio" -> PantallaInicio(

            abrirPasajero = {
                pantalla = "pasajero"
            },

            abrirConductor = {
                pantalla = "conductor"
            },

            abrirAdministrador = {
                pantalla = "admin"
            }

        )

        "pasajero" -> PassengerScreen(
            volver = {
                pantalla = "inicio"
            }
        )

        "conductor" -> DriverScreen(
            volver = {
                pantalla = "inicio"
            }
        )

        "admin" -> AdminScreen(
            volver = {
                pantalla = "inicio"
            }
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