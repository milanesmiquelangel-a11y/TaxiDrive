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
import kz.taxidrive.app.repository.AuthRepository
import kz.taxidrive.app.repository.AuthSession
import kz.taxidrive.app.screens.LoginScreen
import kz.taxidrive.app.screens.RegisterScreen
import kz.taxidrive.app.ui.theme.TaxiDriveTheme
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    var session by remember { mutableStateOf<AuthSession?>(null) }
    var screen by remember { mutableStateOf("loading") }
    var startupError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            session = AuthRepository.restoreSession(context)
            screen = if (session == null) "login" else "account"
        } catch (e: Exception) {
            startupError = e.message ?: "No se pudo verificar la sesión."
            screen = "login"
        }
    }

    when (screen) {
        "loading" -> Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        "login" -> LoginScreen(
            abrirRegistro = { screen = "register" },
            loginCorrecto = {
                session = it
                screen = "account"
            }
        )
        "register" -> RegisterScreen(
            volverLogin = { screen = "login" },
            registroCorrecto = {
                session = it
                screen = "account"
            }
        )
        else -> session?.let { current ->
            AccountHome(
                session = current,
                onLogout = {
                    scope.launch {
                        AuthRepository.logout(context)
                        session = null
                        screen = "login"
                    }
                }
            )
        } ?: run { screen = "login" }
    }
    if (startupError.isNotBlank() && screen == "login") {
        // Login remains available if an old token cannot be verified.
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountHome(session: AuthSession, onLogout: () -> Unit) {
    val roleLabel = when (session.role) {
        "passenger" -> "Pasajero"
        "driver" -> "Conductor"
        "admin" -> "Administrador"
        else -> "Usuario"
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("TaxiDrive.kz") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🚖 TaxiDrive.kz", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))
            Text(session.fullName, style = MaterialTheme.typography.titleLarge)
            Text(roleLabel, style = MaterialTheme.typography.titleMedium)
            Text(session.phone, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(20.dp))
            when (session.role) {
                "driver" -> {
                    val status = session.driverApprovalStatus ?: "pending"
                    Text(
                        when (status) {
                            "approved" -> "Tu cuenta de conductor está aprobada. Estamos habilitando el módulo de viajes."
                            "rejected" -> "Tu solicitud de conductor fue rechazada. Revisa los datos antes de volver a enviarla."
                            else -> "Tu cuenta fue creada y está pendiente de aprobación administrativa."
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    session.freeTrialEndsAt?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("Fin del período gratuito: $it")
                    }
                }
                "passenger" -> Text(
                    "Tu cuenta de pasajero está autenticada. El módulo de solicitudes se conectará al backend en la siguiente etapa.",
                    style = MaterialTheme.typography.bodyLarge
                )
                "admin" -> Text(
                    "Sesión administrativa autenticada. El panel de administración se conectará al backend en su etapa correspondiente.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(Modifier.height(28.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}
