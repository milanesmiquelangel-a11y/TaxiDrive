package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.taxidrive.app.repository.AuthRepository
import kz.taxidrive.app.repository.AuthSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    abrirRegistro: () -> Unit,
    loginCorrecto: (AuthSession) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var telefono by remember { mutableStateOf("+7") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Iniciar sesión") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono internacional (+7...)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (mensaje.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                enabled = !cargando && telefono.isNotBlank() && password.isNotBlank(),
                onClick = {
                    cargando = true
                    mensaje = ""
                    scope.launch {
                        try {
                            val session = AuthRepository.login(context, telefono, password)
                            loginCorrecto(session)
                        } catch (e: Exception) {
                            mensaje = e.message ?: "No se pudo iniciar sesión."
                        } finally {
                            cargando = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (cargando) "Conectando..." else "Entrar") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = abrirRegistro, modifier = Modifier.fillMaxWidth()) {
                Text("Crear cuenta")
            }
        }
    }
}
