package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.data.network.RideRequestDto
import kz.taxidrive.app.repository.OfferRepository
import kz.taxidrive.app.repository.RequestRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(volver: () -> Unit, abrirChat: (Long) -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var requests by remember { mutableStateOf(emptyList<RideRequestDto>()) }
    var myOffers by remember { mutableStateOf(emptyList<kz.taxidrive.app.data.network.DriverOfferDto>()) }
    var prices by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var messages by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var statusText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var sendingId by remember { mutableStateOf<Long?>(null) }

    fun refresh() {
        scope.launch {
            loading = true
            statusText = ""
            try {
                requests = RequestRepository.list(context)
                myOffers = runCatching { OfferRepository.listMyOffers(context) }.getOrDefault(emptyList())
                error = false
                if (requests.isEmpty()) statusText = "No hay solicitudes disponibles por ahora."
            } catch (e: Exception) {
                statusText = e.message ?: "No se pudieron cargar las solicitudes. Comprueba tu aprobación y activación."
                error = true
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(topBar = { TopAppBar(title = { Text("Solicitudes disponibles") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedButton(onClick = volver, modifier = Modifier.fillMaxWidth()) { Text("← Volver") }
            Spacer(Modifier.height(10.dp))
            Text("Propón tu precio por pasajero. No necesitas indicar capacidad ni plazas.", style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { refresh() }, enabled = !loading) { Text("Actualizar") }
            }
            if (loading) CircularProgressIndicator()
            if (statusText.isNotBlank()) {
                Text(statusText, color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
            if (myOffers.isNotEmpty()) {
                Text("Mis ofertas", style = MaterialTheme.typography.titleLarge)
                myOffers.forEach { offer ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Oferta #${offer.id}", style = MaterialTheme.typography.titleMedium)
                            Text("${offer.price_per_passenger_kzt} ₸ por pasajero · Estado: ${offer.status}")
                            if (offer.status == "accepted") {
                                Button(onClick = { abrirChat(offer.id) }, modifier = Modifier.fillMaxWidth()) { Text("Abrir chat del viaje") }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Nuevas solicitudes", style = MaterialTheme.typography.titleLarge)
            }
            requests.forEach { request ->
                Card(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(request.origin_text + " → " + request.destination_text, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(5.dp))
                        Text("Salida: " + request.departure_at)
                        Text("Pasajeros: " + request.passenger_count)
                        request.comment?.takeIf { it.isNotBlank() }?.let { Text("Comentario: " + it) }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = prices[request.id] ?: "",
                            onValueChange = { prices = prices + (request.id to it.filter(Char::isDigit).take(9)) },
                            label = { Text("Tu precio por pasajero (₸)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = messages[request.id] ?: "",
                            onValueChange = { messages = messages + (request.id to it) },
                            label = { Text("Mensaje opcional") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            enabled = sendingId == null && (prices[request.id]?.toIntOrNull() ?: 0) > 0,
                            onClick = {
                                val price = prices[request.id]?.toIntOrNull() ?: return@Button
                                scope.launch {
                                    sendingId = request.id
                                    try {
                                        OfferRepository.create(context, request.id, price, messages[request.id])
                                        statusText = "Oferta enviada correctamente."
                                        error = false
                                        requests = requests.filterNot { it.id == request.id }
                                    } catch (e: Exception) {
                                        statusText = e.message ?: "No se pudo enviar la oferta."
                                        error = true
                                    } finally {
                                        sendingId = null
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (sendingId == request.id) "Enviando..." else "Enviar oferta") }
                    }
                }
            }
        }
    }
}
