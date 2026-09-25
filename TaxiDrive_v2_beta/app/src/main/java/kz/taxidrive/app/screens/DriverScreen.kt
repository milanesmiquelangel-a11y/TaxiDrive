package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.model.TripPost
import kz.taxidrive.app.repository.OfferRepository
import kz.taxidrive.app.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(volver: () -> Unit, driverUid: String = "") {
    var user by remember { mutableStateOf<kz.taxidrive.app.model.User?>(null) }
    var mensaje by remember { mutableStateOf("") }

    LaunchedEffect(driverUid) {
        if (driverUid.isNotBlank()) UserRepository.getUser(driverUid) { user = it }
    }

    val now = System.currentTimeMillis()
    val acceso = user?.let { it.aprobado && (it.trialUntil > now || it.activationUntil > now) } == true

    Scaffold(topBar = { TopAppBar(title = { Text("TaxiDrive.kz — Conductor") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            Button(volver, modifier = Modifier.fillMaxWidth()) { Text("← Volver") }
            Spacer(Modifier.height(20.dp))
            when {
                driverUid.isBlank() -> Text("Inicie sesión con el teléfono del conductor.")
                user == null -> Text("No se encontró el perfil del conductor.")
                !user!!.aprobado -> Text("Tu perfil de conductor está pendiente de aprobación.")
                acceso -> {
                    var origen by remember { mutableStateOf("") }
                    var destino by remember { mutableStateOf("") }
                    var fecha by remember { mutableStateOf("") }
                    var hora by remember { mutableStateOf("") }
                    var precio by remember { mutableStateOf("") }
                    var comentario by remember { mutableStateOf("") }

                    fun field(value: String, label: String, set: (String) -> Unit) {
                        OutlinedTextField(value, set, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                    }
                    Text("Publicar oferta", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(12.dp))
                    field(origen, "Origen") { origen = it }
                    field(destino, "Destino") { destino = it }
                    field(fecha, "Fecha") { fecha = it }
                    field(hora, "Hora de salida") { hora = it }
                    field(precio, "Precio solicitado (₸)") { precio = it }
                    field(comentario, "Comentarios (opcional)") { comentario = it }
                    if (mensaje.isNotEmpty()) Text(mensaje)
                    Button(onClick = {
                        if (origen.isBlank() || destino.isBlank() || fecha.isBlank() || hora.isBlank() || precio.isBlank()) {
                            mensaje = "Complete todos los campos obligatorios"
                        } else {
                            OfferRepository.add(TripPost(
                                type = "conductor", origin = origen, destination = destino, date = fecha, time = hora,
                                price = precio, comment = comentario, phone = user!!.telefono, userId = user!!.uid, status = "activo"
                            ), {
                                mensaje = "Oferta publicada correctamente"
                                origen = ""; destino = ""; fecha = ""; hora = ""; precio = ""; comentario = ""
                            }, { mensaje = it.message ?: "No se pudo publicar la oferta" })
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Publicar oferta") }
                }
                user!!.activationPending -> {
                    Text("Hay una activación de 500 ₸ pendiente de confirmación.")
                    Spacer(Modifier.height(12.dp))
                    Text("La activación será válida durante 24 horas desde la confirmación.")
                }
                else -> {
                    Text("Para publicar ofertas necesitas una activación.")
                    Spacer(Modifier.height(12.dp))
                    Text("Activación: 500 ₸ • Duración: 24 horas")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        UserRepository.requestActivation(user!!.uid, {
                            mensaje = "Solicitud de activación enviada al administrador."
                            UserRepository.getUser(user!!.uid) { user = it }
                        }, { mensaje = it.message ?: "No se pudo solicitar la activación" })
                    }, modifier = Modifier.fillMaxWidth()) { Text("Solicitar activación — 500 ₸") }
                    if (mensaje.isNotEmpty()) { Spacer(Modifier.height(12.dp)); Text(mensaje) }
                }
            }
        }
    }
}