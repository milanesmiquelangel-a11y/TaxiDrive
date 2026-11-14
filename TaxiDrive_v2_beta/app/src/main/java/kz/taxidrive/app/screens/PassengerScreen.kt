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
                Text(mensaje, color = if (mensaje.startsWith("Solicitud publicada")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
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
                                    mensaje = "Solicitud publicada correctamente (#" + response.optInt("id") + ")."
                                    origen = ""
                                    destino = ""
                                    fechaSeleccionada = null
                                    horaSeleccionada = null
                                    pasajeros = 1
                                    comentario = ""
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
        }
    }
}
