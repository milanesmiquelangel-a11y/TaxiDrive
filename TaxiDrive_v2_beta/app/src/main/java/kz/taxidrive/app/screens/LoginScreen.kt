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
import kz.taxidrive.app.auth.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(abrirRegistro: () -> Unit, loginCorrecto: (String) -> Unit) {
    val context = LocalContext.current
    var telefono by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    val telefonoCompleto = "+7$telefono"
    Scaffold(topBar = { TopAppBar(title = { Text("Iniciar sesión") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.Center) {
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it.filter(Char::isDigit).take(10); mensaje = "" },
                label = { Text("Teléfono") },
                prefix = { Text("+7") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando
            )
            OutlinedTextField(contrasena, { contrasena = it; mensaje = "" }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), enabled = !cargando)
            if (mensaje.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(mensaje, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                if (telefono.length != 10 || contrasena.isBlank()) mensaje = "Introduzca los 10 dígitos del teléfono y la contraseña"
                else {
                    cargando = true; mensaje = ""
                    AuthRepository.login(context, telefonoCompleto, contrasena, {
                        cargando = false; loginCorrecto(telefonoCompleto)
                    }, { error -> cargando = false; mensaje = error })
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !cargando) { Text(if (cargando) "Conectando..." else "Entrar") }
            Spacer(Modifier.height(12.dp))
            Button(onClick = abrirRegistro, modifier = Modifier.fillMaxWidth(), enabled = !cargando) { Text("Crear cuenta") }
        }
    }
}