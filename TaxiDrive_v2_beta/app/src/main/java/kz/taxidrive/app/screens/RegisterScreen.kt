package kz.taxidrive.app.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    volverLogin: () -> Unit,
    recoveryMode: Boolean = false,
    existingProfile: JSONObject? = null
) {
    val context = LocalContext.current
    var tipo by remember { mutableStateOf(if (recoveryMode) "conductor" else "pasajero") }
    var accountCreated by remember { mutableStateOf(recoveryMode) }
    val completingProfile = recoveryMode || accountCreated

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var marca by remember(existingProfile) { mutableStateOf(existingProfile?.optString("vehicle_make").orEmpty().takeUnless { it == "null" }.orEmpty()) }
    var modelo by remember(existingProfile) { mutableStateOf(existingProfile?.optString("vehicle_model").orEmpty().takeUnless { it == "null" }.orEmpty()) }
    var color by remember { mutableStateOf("") }
    var anio by remember(existingProfile) { mutableStateOf(existingProfile?.optString("vehicle_year").orEmpty().takeUnless { it == "null" }.orEmpty()) }
    var matricula by remember(existingProfile) { mutableStateOf(existingProfile?.optString("license_plate").orEmpty().takeUnless { it == "null" }.orEmpty()) }
    var licencia by remember { mutableStateOf("") }
    var privacidadAceptada by remember { mutableStateOf(false) }
    var declaracionConductorAceptada by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var fotoFrontal by remember { mutableStateOf<android.net.Uri?>(null) }
    var fotoVolante by remember { mutableStateOf<android.net.Uri?>(null) }
    var frontDocumentId by remember(existingProfile) { mutableStateOf(existingProfile?.optString("front_photo_url").orEmpty().takeUnless { it == "null" }.orEmpty()) }
    var steeringWheelDocumentId by remember(existingProfile) { mutableStateOf(existingProfile?.optString("steering_wheel_photo_url").orEmpty().takeUnless { it == "null" }.orEmpty()) }

    val elegirFotoFrontal = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { fotoFrontal = it }
    val elegirFotoVolante = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { fotoVolante = it }
    val telefonoCompleto = "+7$telefono"

    fun guardarPerfil(frontId: String, wheelId: String, volverTrasGuardar: Boolean) {
        AuthRepository.submitDriverProfile(
            context, marca, modelo, anio.toInt(), matricula, frontId, wheelId,
            {
                cargando = false
                mensaje = "Perfil enviado correctamente. Queda pendiente de aprobación administrativa."
                if (volverTrasGuardar) volverLogin()
            },
            { error ->
                cargando = false
                mensaje = "Los documentos se conservaron, pero no se pudo guardar el perfil: $error"
            }
        )
    }

    fun subirDocumentosYGuardar(volverTrasGuardar: Boolean) {
        fun subirVolante(frontId: String) {
            val selectedWheel = fotoVolante
            if (selectedWheel == null) {
                if (steeringWheelDocumentId.isBlank()) {
                    cargando = false
                    mensaje = "Añada la foto del volante para completar el perfil."
                } else {
                    guardarPerfil(frontId, steeringWheelDocumentId, volverTrasGuardar)
                }
            } else {
                AuthRepository.uploadDriverDocument(context, "steering_wheel", selectedWheel, { wheelId ->
                    steeringWheelDocumentId = wheelId
                    fotoVolante = null
                    guardarPerfil(frontId, wheelId, volverTrasGuardar)
                }, { error ->
                    cargando = false
                    mensaje = "No se pudo subir la foto del volante: $error"
                })
            }
        }

        val selectedFront = fotoFrontal
        if (selectedFront == null) {
            if (frontDocumentId.isBlank()) {
                cargando = false
                mensaje = "Añada la foto frontal del vehículo para completar el perfil."
            } else {
                subirVolante(frontDocumentId)
            }
        } else {
            AuthRepository.uploadDriverDocument(context, "front", selectedFront, { frontId ->
                frontDocumentId = frontId
                fotoFrontal = null
                subirVolante(frontId)
            }, { error ->
                cargando = false
                mensaje = "No se pudo subir la foto frontal: $error"
            })
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(if (completingProfile) "Completar perfil de conductor" else "Crear cuenta") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!completingProfile) {
                Text("Tipo de usuario")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(tipo == "pasajero", { tipo = "pasajero" }); Text("Pasajero")
                    Spacer(Modifier.width(20.dp))
                    RadioButton(tipo == "conductor", { tipo = "conductor" }); Text("Conductor")
                }
                Spacer(Modifier.height(20.dp))
            }

            @Composable
            fun field(value: String, label: String, set: (String) -> Unit) {
                OutlinedTextField(value, set, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), enabled = !cargando)
                Spacer(Modifier.height(12.dp))
            }

            if (!completingProfile) {
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
                OutlinedTextField(
                    contrasena, { contrasena = it },
                    label = { Text("Contraseña (mínimo 10 caracteres)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                )
                Spacer(Modifier.height(12.dp))
            }

            if (tipo == "conductor") {
                Text("Datos del vehículo")
                Spacer(Modifier.height(12.dp))
                field(marca, "Marca") { marca = it }
                field(modelo, "Modelo") { modelo = it }
                if (!completingProfile) field(color, "Color") { color = it }
                field(anio, "Año") { anio = it }
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
                if (!completingProfile) field(licencia, "Licencia") { licencia = it }
                OutlinedButton(
                    onClick = { elegirFotoFrontal.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                ) {
                    Text(
                        when {
                            fotoFrontal != null -> "Foto frontal seleccionada"
                            frontDocumentId.isNotBlank() -> "Foto frontal guardada en el servidor"
                            else -> "Añadir foto frontal del vehículo"
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { elegirFotoVolante.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                ) {
                    Text(
                        when {
                            fotoVolante != null -> "Foto del volante seleccionada"
                            steeringWheelDocumentId.isNotBlank() -> "Foto del volante guardada en el servidor"
                            else -> "Añadir foto del volante"
                        }
                    )
                }
                Text("JPG, PNG o WEBP; máximo 5 MB por imagen.", style = MaterialTheme.typography.bodySmall)
                if (!completingProfile) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(declaracionConductorAceptada, { declaracionConductorAceptada = it }, enabled = !cargando)
                        Text("Confirmo que tengo 18 años o más, licencia y seguro vigentes, y que mi vehículo está en condiciones adecuadas.")
                    }
                }
            }

            if (!completingProfile) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(privacidadAceptada, { privacidadAceptada = it }, enabled = !cargando)
                    Text("Acepto la Política de Privacidad de TaxiDrive.kz.")
                }
            }

            if (mensaje.isNotEmpty()) Text(mensaje, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    if (completingProfile) {
                        when {
                            marca.isBlank() || modelo.isBlank() -> mensaje = "Complete la marca y el modelo del vehículo"
                            anio.toIntOrNull()?.let { it !in 1980..2100 } != false -> mensaje = "Introduzca un año válido entre 1980 y 2100"
                            !matricula.matches(Regex("[0-9]{3}[A-Z]{3}[0-9]{2}")) -> mensaje = "La matrícula debe tener el formato 123ABC45"
                            frontDocumentId.isBlank() && fotoFrontal == null -> mensaje = "Añada la foto frontal del vehículo"
                            steeringWheelDocumentId.isBlank() && fotoVolante == null -> mensaje = "Añada la foto del volante"
                            else -> {
                                cargando = true
                                mensaje = ""
                                subirDocumentosYGuardar(volverTrasGuardar = !recoveryMode)
                            }
                        }
                    } else {
                        when {
                            nombre.isBlank() || apellidos.isBlank() -> mensaje = "Complete todos los campos obligatorios"
                            telefono.length != 10 -> mensaje = "Introduzca los 10 dígitos del teléfono después de +7"
                            contrasena.length < 10 -> mensaje = "La contraseña debe tener al menos 10 caracteres"
                            tipo == "conductor" && (marca.isBlank() || modelo.isBlank()) -> mensaje = "Complete la marca y el modelo del vehículo"
                            tipo == "conductor" && anio.toIntOrNull()?.let { it !in 1980..2100 } != false -> mensaje = "Introduzca un año válido entre 1980 y 2100"
                            tipo == "conductor" && !matricula.matches(Regex("[0-9]{3}[A-Z]{3}[0-9]{2}")) -> mensaje = "La matrícula debe tener el formato 123ABC45"
                            tipo == "conductor" && fotoFrontal == null -> mensaje = "Añada la foto frontal del vehículo"
                            tipo == "conductor" && fotoVolante == null -> mensaje = "Añada la foto del volante"
                            !privacidadAceptada -> mensaje = "Debe aceptar la Política de Privacidad"
                            tipo == "conductor" && !declaracionConductorAceptada -> mensaje = "Debe aceptar la declaración del conductor"
                            else -> {
                                cargando = true
                                mensaje = ""
                                AuthRepository.register(
                                    context, telefonoCompleto, "$nombre $apellidos", contrasena,
                                    if (tipo == "conductor") "driver" else "passenger",
                                    privacidadAceptada, declaracionConductorAceptada,
                                    {
                                        if (tipo == "pasajero") {
                                            cargando = false
                                            mensaje = "Cuenta creada correctamente. Ya puede iniciar sesión."
                                            volverLogin()
                                        } else {
                                            accountCreated = true
                                            subirDocumentosYGuardar(volverTrasGuardar = true)
                                        }
                                    },
                                    { error -> cargando = false; mensaje = error }
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando
            ) {
                Text(
                    when {
                        cargando -> if (completingProfile) "Guardando perfil..." else "Registrando..."
                        completingProfile -> "Guardar perfil"
                        else -> "Crear cuenta"
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(volverLogin, modifier = Modifier.fillMaxWidth(), enabled = !cargando) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}
