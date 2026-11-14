package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.repository.DriverApiRepository
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(volver: () -> Unit, driverUid: String = "") {
    val context = LocalContext.current
    var activation by remember { mutableStateOf<JSONObject?>(null) }
    var requests by remember { mutableStateOf<JSONArray?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    val prices = remember { mutableStateMapOf<Int, String>() }
    val comments = remember { mutableStateMapOf<Int, String>() }
    val sending = remember { mutableStateSetOf<Int>() }
    val submitted = remember { mutableStateSetOf<Int>() }

    fun loadDriverData(isRefresh: Boolean = false) {
        if (isRefresh) refreshing = true else loading = true
        mensaje = ""
        DriverApiRepository.getActivationStatus(context, { status ->
            activation = status
            if (status.optBoolean("can_publish_offers")) {
                DriverApiRepository.getPassengerRequests(context, { response ->
                    requests = response.optJSONArray("items") ?: JSONArray()
                    loading = false
                    refreshing = false
                }, { error ->
                    mensaje = error
                    requests = JSONArray()
                    loading = false
                    refreshing = false
                })
            } else {
                requests = JSONArray()
                loading = false
                refreshing = false
            }
        }, { error ->
            mensaje = error
            loading = false
            refreshing = false
        })
    }

    LaunchedEffect(Unit) { loadDriverData() }

    val approved = activation?.optBoolean("approved") == true
    val canPublish = activation?.optBoolean("can_publish_offers") == true

    Scaffold(
        topBar = { TopAppBar(title = { Text("TaxiDrive.kz — Conductor") }) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Button(onClick = volver, modifier = Modifier.fillMaxWidth()) { Text("← Volver") }
            Spacer(Modifier.height(20.dp))

            when {
                loading -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Comprobando aprobación y activación...")
                }
                activation == null -> {
                    Text(mensaje.ifBlank { "No se pudo consultar el estado del conductor." })
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { loadDriverData() }) { Text("Reintentar") }
                }
                !approved -> {
                    Text("Tu perfil de conductor está pendiente de aprobación.")
                    Text("Cuando el administrador lo apruebe, comenzará tu mes de prueba gratuito.")
                }
                !canPublish -> {
                    Text("Tu mes gratuito ha terminado o no tienes una activación vigente.")
                    Spacer(Modifier.height(8.dp))
                    Text("Activación: 500 ₸ por 24 horas.")
                    Spacer(Modifier.height(8.dp))
                    Text("El cobro Kaspi todavía no está habilitado. No se ha realizado ningún cobro ni activación.")
                }
                else -> {
                    Text("Solicitudes de pasajeros", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Propón tu precio por pasajero para la solicitud que quieras aceptar.")
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { loadDriverData(isRefresh = true) },
                        enabled = !refreshing,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (refreshing) "Actualizando..." else "Actualizar solicitudes") }

                    if (mensaje.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(mensaje, color = MaterialTheme.colorScheme.error)
                    }

                    val list = requests ?: JSONArray()
                    var visibleCount = 0
                    for (index in 0 until list.length()) {
                        val item = list.optJSONObject(index) ?: continue
                        val requestId = item.optInt("id")
                        if (requestId <= 0 || requestId in submitted) continue
                        visibleCount++
                        Spacer(Modifier.height(12.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    item.optString("origin_text") + " → " + item.optString("destination_text"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(6.dp))
                                Text("Salida: " + item.optString("departure_at"))
                                Text("Pasajeros: " + item.optInt("passenger_count", 1))
                                val passengerComment = item.optString("comment")
                                if (passengerComment.isNotBlank() && passengerComment != "null") {
                                    Text("Comentario: " + passengerComment)
                                }
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = prices[requestId] ?: "",
                                    onValueChange = { value ->
                                        prices[requestId] = value.filter(Char::isDigit).take(8)
                                    },
                                    label = { Text("Tu precio por pasajero (₸)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = comments[requestId] ?: "",
                                    onValueChange = { comments[requestId] = it.take(1000) },
                                    label = { Text("Mensaje (opcional)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val price = prices[requestId]?.toIntOrNull()
                                        if (price == null || price <= 0) {
                                            mensaje = "Indica un precio válido por pasajero."
                                        } else {
                                            sending.add(requestId)
                                            mensaje = ""
                                            DriverApiRepository.createOffer(
                                                context = context,
                                                requestId = requestId,
                                                pricePerPassengerKzt = price,
                                                message = comments[requestId].orEmpty(),
                                                onSuccess = {
                                                    sending.remove(requestId)
                                                    submitted.add(requestId)
                                                    mensaje = "Tu oferta se envió correctamente."
                                                },
                                                onFailure = { error ->
                                                    sending.remove(requestId)
                                                    mensaje = error
                                                }
                                            )
                                        }
                                    },
                                    enabled = requestId !in sending,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (requestId in sending) "Enviando..." else "Enviar oferta")
                                }
                            }
                        }
                    }
                    if (visibleCount == 0) {
                        Spacer(Modifier.height(16.dp))
                        Text("No hay solicitudes de pasajeros disponibles en este momento.")
                    }
                }
            }
        }
    }
}
