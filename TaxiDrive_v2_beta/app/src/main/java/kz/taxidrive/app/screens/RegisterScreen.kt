package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
fun RegisterScreen(
    volverLogin: () -> Unit,
    registroCorrecto: (AuthSession) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var role by remember { mutableStateOf("passenger") }
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("+7") }
    var password by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Crear cuenta") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tipo de usuario", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = role == "passenger", onClick = { role = "passenger" })
                Text("Pasajero")
                Spacer(Modifier.width(18.dp))
                RadioButton(selected = role == "driver", onClick = { role = "driver" })
                Text("Conductor")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text("Nombre y apellidos") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = telefono, onValueChange = { telefono = it },
                label = { Text("Teléfono internacional (+7...)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Contraseña (mínimo 10 caracteres)") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmar, onValueChange = { confirmar = it },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            if (role == "driver") {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Después de crear la cuenta deberás completar los datos del vehículo y subir la foto frontal con matrícula y la foto del volante. No podrás publicar hasta que administración apruebe tu perfil.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (mensaje.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(18.dp))
            Button(
                enabled = !cargando && nombre.isNotBlank() && telefono.isNotBlank() &&
                    password.length >= 10 && confirmar == password,
                onClick = {
                    cargando = true
                    mensaje = ""
                    scope.launch {
                        try {
                            val session = AuthRepository.register(
                                context, telefono, nombre, password, role
                            )
                            registroCorrecto(session)
                        } catch (e: Exception) {
                            mensaje = e.message ?: "No se pudo crear la cuenta."
                        } finally {
                            cargando = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (cargando) "Creando cuenta..." else "Crear cuenta") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = volverLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}
