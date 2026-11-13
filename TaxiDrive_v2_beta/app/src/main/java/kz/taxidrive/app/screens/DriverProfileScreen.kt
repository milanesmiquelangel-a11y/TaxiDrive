package kz.taxidrive.app.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kz.taxidrive.app.repository.DriverRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2020") }
    var plate by remember { mutableStateOf("") }
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var steeringUri by remember { mutableStateOf<Uri?>(null) }
    var frontId by remember { mutableStateOf("") }
    var steeringId by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val frontPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        frontUri = it
        frontId = ""
    }
    val steeringPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        steeringUri = it
        steeringId = ""
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Perfil de conductor") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Completa los datos del vehículo y sube las dos fotografías obligatorias. Tu perfil quedará pendiente de revisión.")
            OutlinedTextField(make, { make = it }, label = { Text("Marca del vehículo") }, enabled = !submitted, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(model, { model = it }, label = { Text("Modelo") }, enabled = !submitted, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(year, { year = it.filter(Char::isDigit).take(4) }, label = { Text("Año (1980–2100)") }, enabled = !submitted, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(plate, { plate = it.uppercase().take(20) }, label = { Text("Matrícula") }, enabled = !submitted, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = { frontPicker.launch("image/*") }, enabled = !loading && !submitted, modifier = Modifier.fillMaxWidth()) {
                Text(if (frontUri == null) "Seleccionar foto frontal con matrícula" else "Cambiar foto frontal")
            }
            Text(if (frontId.isNotBlank()) "Foto frontal subida correctamente" else "Foto frontal pendiente o seleccionada")
            OutlinedButton(onClick = { steeringPicker.launch("image/*") }, enabled = !loading && !submitted, modifier = Modifier.fillMaxWidth()) {
                Text(if (steeringUri == null) "Seleccionar foto del volante" else "Cambiar foto del volante")
            }
            Text(if (steeringId.isNotBlank()) "Foto del volante subida correctamente" else "Foto del volante pendiente o seleccionada")
            if (message.isNotBlank()) Text(message, color = if (submitted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            Button(
                enabled = !loading && !submitted && frontUri != null && steeringUri != null,
                onClick = {
                    loading = true
                    message = ""
                    scope.launch {
                        try {
                            frontId = DriverRepository.uploadDocument(context, "front", frontUri!!)
                            steeringId = DriverRepository.uploadDocument(context, "steering_wheel", steeringUri!!)
                            message = "Las dos fotografías se subieron de forma privada."
                        } catch (e: Exception) {
                            message = e.message ?: "No se pudieron subir las fotografías."
                        } finally { loading = false }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(when { loading -> "Subiendo..."; submitted -> "Fotografías enviadas"; else -> "Subir las dos fotografías" }) }
            Button(
                enabled = !loading && !submitted && make.length >= 2 && model.isNotBlank() &&
                    (year.toIntOrNull() ?: 0) in 1980..2100 && plate.length >= 3 &&
                    frontId.isNotBlank() && steeringId.isNotBlank(),
                onClick = {
                    loading = true
                    message = ""
                    scope.launch {
                        try {
                            DriverRepository.submitProfile(context, make, model, year.toInt(), plate, frontId, steeringId)
                            submitted = true
                            message = "Perfil enviado. Pendiente de aprobación administrativa."
                        } catch (e: Exception) {
                            message = e.message ?: "No se pudo enviar el perfil."
                        } finally { loading = false }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "Enviando..." else "Enviar perfil para aprobación") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
        }
    }
}
