package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.model.User
import kz.taxidrive.app.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(volverLogin: () -> Unit) {
    var tipo by remember { mutableStateOf("pasajero") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var licencia by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Crear cuenta") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tipo de usuario")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(tipo == "pasajero", { tipo = "pasajero" }); Text("Pasajero")
                Spacer(Modifier.width(20.dp))
                RadioButton(tipo == "conductor", { tipo = "conductor" }); Text("Conductor")
            }
            Spacer(Modifier.height(20.dp))
            fun field(value: String, label: String, set: (String) -> Unit) {
                OutlinedTextField(value, set, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }
            field(nombre, "Nombre") { nombre = it }
            field(apellidos, "Apellidos") { apellidos = it }
            field(telefono, "Teléfono") { telefono = it }
            if (tipo == "conductor") {
                Text("Datos del vehículo")
                Spacer(Modifier.height(12.dp))
                field(marca, "Marca") { marca = it }
                field(modelo, "Modelo") { modelo = it }
                field(color, "Color") { color = it }
                field(anio, "Año") { anio = it }
                field(matricula, "Matrícula") { matricula = it }
                field(licencia, "Licencia") { licencia = it }
            }
            if (mensaje.isNotEmpty()) Text(mensaje, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    if (nombre.isBlank() || apellidos.isBlank() || telefono.isBlank()) {
                        mensaje = "Complete todos los campos obligatorios"; return@Button
                    }
                    val user = User(
                        uid = telefono.trim(), nombre = nombre, apellidos = apellidos,
                        telefono = telefono.trim(), tipo = tipo,
                        aprobado = tipo == "pasajero",
                        marca = marca, modelo = modelo, color = color, anio = anio,
                        matricula = matricula, licencia = licencia,
                        trialUntil = 0L, activationUntil = 0L,
                        activationPending = false, activationAmount = 500L
                    )
                    UserRepository.registerUser(user, {
                        mensaje = "Cuenta creada correctamente"; volverLogin()
                    }, { mensaje = it.message ?: "Error al registrar el usuario" })
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Crear cuenta") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(volverLogin, modifier = Modifier.fillMaxWidth()) { Text("Volver al inicio de sesión") }
        }
    }
}