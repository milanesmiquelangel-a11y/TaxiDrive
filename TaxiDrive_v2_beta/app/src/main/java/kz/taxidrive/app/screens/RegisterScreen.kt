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
import kz.taxidrive.app.auth.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(volverLogin: () -> Unit) {
    val context = LocalContext.current
    var tipo by remember { mutableStateOf("pasajero") }
    var nombre by remember { mutableStateOf("") }; var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }; var contrasena by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }; var modelo by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }; var anio by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }; var licencia by remember { mutableStateOf("") }
    var privacidadAceptada by remember { mutableStateOf(false) }
    var declaracionConductorAceptada by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }; var cargando by remember { mutableStateOf(false) }
    val telefonoCompleto = "+7$telefono"
    Scaffold(topBar = { TopAppBar(title = { Text("Crear cuenta") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tipo de usuario")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(tipo == "pasajero", { tipo = "pasajero" }); Text("Pasajero")
                Spacer(Modifier.width(20.dp))
                RadioButton(tipo == "conductor", { tipo = "conductor" }); Text("Conductor")
            }
            Spacer(Modifier.height(20.dp))
            @Composable fun field(value: String, label: String, set: (String) -> Unit) {
                OutlinedTextField(value, set, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), enabled = !cargando)
                Spacer(Modifier.height(12.dp))
            }
            field(nombre, "Nombre") { nombre = it }
            field(apellidos, "Apellidos") { apellidos = it }
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it.filter(Char::isDigit).take(10) },
                label = { Text("Teléfono") },
                prefix = { Text("+7") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(contrasena, { contrasena = it }, label = { Text("Contraseña (mínimo 10 caracteres)") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), enabled = !cargando)
            Spacer(Modifier.height(12.dp))
            if (tipo == "conductor") {
                Text("Datos del vehículo"); Spacer(Modifier.height(12.dp))
                field(marca, "Marca") { marca = it }; field(modelo, "Modelo") { modelo = it }
                field(color, "Color") { color = it }; field(anio, "Año") { anio = it }
                OutlinedTextField(
                    value = matricula,
                    onValueChange = { input ->
                        val clean = input.uppercase().filter { it.isLetterOrDigit() }
                        val formatted = buildString {
                            clean.forEach { ch ->
                                val index = length
                                if (index < 3 && ch.isDigit()) append(ch)
                                else if (index in 3..5 && ch.isLetter() && ch.code < 128) append(ch)
                                else if (index in 6..7 && ch.isDigit()) append(ch)
                            }
                        }
                        matricula = formatted.take(8)
                    },
                    label = { Text("Matrícula (123ABC45)") },
                    placeholder = { Text("123ABC45") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                )
                Spacer(Modifier.height(12.dp))
                field(licencia, "Licencia") { licencia = it }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(declaracionConductorAceptada, { declaracionConductorAceptada = it }, enabled = !cargando)
                    Text("Confirmo que tengo 18 años o más, licencia y seguro vigentes, y que mi vehículo está en condiciones adecuadas.")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(privacidadAceptada, { privacidadAceptada = it }, enabled = !cargando)
                Text("Acepto la Política de Privacidad de TaxiDrive.kz.")
            }
            if (mensaje.isNotEmpty()) Text(mensaje, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                when {
                    nombre.isBlank() || apellidos.isBlank() -> mensaje = "Complete todos los campos obligatorios"
                    telefono.length != 10 -> mensaje = "Introduzca los 10 dígitos del teléfono después de +7"
                    contrasena.length < 10 -> mensaje = "La contraseña debe tener al menos 10 caracteres"
                    tipo == "conductor" && !matricula.matches(Regex("[0-9]{3}[A-Z]{3}[0-9]{2}")) -> mensaje = "La matrícula debe tener el formato 123ABC45"
                    !privacidadAceptada -> mensaje = "Debe aceptar la Política de Privacidad"
                    tipo == "conductor" && !declaracionConductorAceptada -> mensaje = "Debe aceptar la declaración del conductor"
                    else -> {
                        cargando = true; mensaje = ""
                        AuthRepository.register(context, telefonoCompleto, "$nombre $apellidos", contrasena,
                            if (tipo == "conductor") "driver" else "passenger", privacidadAceptada,
                            declaracionConductorAceptada, {
                                cargando = false; mensaje = "Cuenta creada correctamente. Ya puede iniciar sesión."; volverLogin()
                            }, { error -> cargando = false; mensaje = error })
                    }
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !cargando) { Text(if (cargando) "Registrando..." else "Crear cuenta") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(volverLogin, modifier = Modifier.fillMaxWidth(), enabled = !cargando) { Text("Volver al inicio de sesión") }
        }
    }
}