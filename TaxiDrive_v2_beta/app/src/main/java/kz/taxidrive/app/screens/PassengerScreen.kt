package kz.taxidrive.app.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.repository.PassengerApiRepository
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerScreen(volver: () -> Unit) {
    val context = LocalContext.current
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf<Calendar?>(null) }
    var horaSeleccionada by remember { mutableStateOf<Calendar?>(null) }
    var pasajeros by remember { mutableIntStateOf(1) }
    var comentario by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var requestId by remember { mutableIntStateOf(0) }
    var ofertas by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var cargandoOfertas by remember { mutableStateOf(false) }
    var errorOfertas by remember { mutableStateOf("") }
    var acceptingOfferId by remember { mutableIntStateOf(0) }
    var mensajeAceptacion by remember { mutableStateOf("") }

    val cargarOfertas: (Int) -> Unit = { id ->
        if (!cargandoOfertas && id > 0) {
            cargandoOfertas = true
            errorOfertas = ""
            PassengerApiRepository.getRequestOffers(
                context = context,
                requestId = id,
                onSuccess = { items ->
                    ofertas = (0 until items.length()).mapNotNull { index ->
                        items.optJSONObject(index)
                    }
                    cargandoOfertas = false
                },
                onFailure = { error ->
                    errorOfertas = error
                    cargandoOfertas = false
                }
            )
        }
    }

    val dateLabel = fechaSeleccionada?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.time)
    } ?: "Seleccionar fecha"
    val timeLabel = horaSeleccionada?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it.time)
    } ?: "Seleccionar hora"

    Scaffold(
        topBar = { TopAppBar(title = { Text("Publicar solicitud") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Button(onClick = volver, modifier = Modifier.fillMaxWidth()) {
                Text("← Volver")
            }
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = origen,
                onValueChange = { origen = it },
                label = { Text("Origen") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = destino,
                onValueChange = { destino = it },
                label = { Text("Destino") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val now = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            fechaSeleccionada = Calendar.getInstance().apply {
                                set(year, month, day, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                        datePicker.minDate = System.currentTimeMillis()
                    }.show()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(dateLabel) }
            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val now = Calendar.getInstance()
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            horaSeleccionada = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(timeLabel) }

            Spacer(Modifier.height(16.dp))
            Text("Cantidad de pasajeros", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { if (pasajeros > 1) pasajeros-- },
                    enabled = pasajeros > 1
                ) { Text("−") }
                Text(
                    text = pasajeros.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )
                OutlinedButton(
                    onClick = { if (pasajeros < 20) pasajeros++ },
                    enabled = pasajeros < 20
                ) { Text("+") }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it.take(2000) },
                label = { Text("Comentarios (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            if (mensaje.isNotBlank()) {
                Text(
                    mensaje,
                    color = if (mensaje.startsWith("Solicitud publicada")) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    val date = fechaSeleccionada
                    val time = horaSeleccionada
                    if (origen.isBlank() || destino.isBlank() || date == null || time == null) {
                        mensaje = "Complete el origen, destino, fecha y hora."
                    } else {
                        val departure = (date.clone() as Calendar).apply {
                            set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, time.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        if (departure.timeInMillis <= System.currentTimeMillis()) {
                            mensaje = "La fecha y hora de salida deben ser futuras."
                        } else {
                            val departureAt = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ssXXX",
                                Locale.US
                            ).format(departure.time)
                            enviando = true
                            mensaje = ""
                            PassengerApiRepository.createRequest(
                                context = context,
                                origin = origen,
                                destination = destino,
                                departureAt = departureAt,
                                passengerCount = pasajeros,
                                comment = comentario,
                                onSuccess = { response ->
                                    enviando = false
                                    requestId = response.optInt("id", 0)
                                    ofertas = emptyList()
                                    mensaje = "Solicitud publicada correctamente (#$requestId)."
                                    origen = ""
                                    destino = ""
                                    fechaSeleccionada = null
                                    horaSeleccionada = null
                                    pasajeros = 1
                                    comentario = ""
                                    if (requestId > 0) {
                                        cargarOfertas(requestId)
                                    } else {
                                        errorOfertas = "La solicitud se publicó, pero el servidor no devolvió su identificador."
                                    }
                                },
                                onFailure = { error ->
                                    enviando = false
                                    mensaje = error
                                }
                            )
                        }
                    }
                },
                enabled = !enviando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (enviando) "Publicando..." else "Publicar solicitud")
            }

            if (requestId > 0) {
                Spacer(Modifier.height(28.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text("Ofertas recibidas", style = MaterialTheme.typography.titleLarge)
                Text("Solicitud #$requestId", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { cargarOfertas(requestId) },
                    enabled = !cargandoOfertas,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (cargandoOfertas) "Consultando..." else "Actualizar ofertas")
                }

                if (errorOfertas.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorOfertas, color = MaterialTheme.colorScheme.error)
                } else if (cargandoOfertas) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else if (ofertas.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Todavía no hay ofertas. Puedes actualizar esta sección más tarde.")
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text("Total: ${ofertas.size}", style = MaterialTheme.typography.bodyMedium)
                if (mensajeAceptacion.isNotBlank()) {
                    Text(
                        mensajeAceptacion,
                        color = if (mensajeAceptacion == "Oferta aceptada correctamente.") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
                    ofertas.forEach { oferta ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    oferta.optString("driver_name", "Conductor"),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${oferta.optInt("price_per_passenger_kzt")} ₸ por pasajero",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val offerMessage = oferta.optString("message")
                                if (offerMessage.isNotBlank() && offerMessage != "null") {
                                    Text(offerMessage)
                                }
                                val status = when (oferta.optString("status")) {
                                    "accepted" -> "Aceptada"
                                    "rejected" -> "No seleccionada"
                                    else -> "Pendiente"
                                }
                                Text("Estado: $status", style = MaterialTheme.typography.bodySmall)

                                if (oferta.optString("status") == "pending") {
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val offerId = oferta.optInt("id", 0)
                                            if (offerId > 0 && acceptingOfferId == 0) {
                                                acceptingOfferId = offerId
                                                mensajeAceptacion = ""
                                                PassengerApiRepository.acceptOffer(
                                                    context = context,
                                                    offerId = offerId,
                                                    onSuccess = {
                                                        acceptingOfferId = 0
                                                        mensajeAceptacion = "Oferta aceptada correctamente."
                                                        cargarOfertas(requestId)
                                                    },
                                                    onFailure = { error ->
                                                        acceptingOfferId = 0
                                                        mensajeAceptacion = error
                                                    }
                                                )
                                            }
                                        },
                                        enabled = acceptingOfferId == 0,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            if (acceptingOfferId == oferta.optInt("id")) {
                                                "Aceptando..."
                                            } else {
                                                "Aceptar oferta"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
