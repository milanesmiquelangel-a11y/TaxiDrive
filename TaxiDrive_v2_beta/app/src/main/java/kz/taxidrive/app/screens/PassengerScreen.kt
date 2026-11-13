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
import kz.taxidrive.app.repository.RequestRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerScreen(volver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var time by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var passengerCount by remember { mutableIntStateOf(1) }
    var comment by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var requests by remember { mutableStateOf(emptyList<kz.taxidrive.app.data.network.RideRequestDto>()) }
    var loadingList by remember { mutableStateOf(false) }

    fun loadRequests() {
        scope.launch {
            loadingList = true
            try {
                requests = RequestRepository.list(context)
            } catch (e: Exception) {
                message = e.message ?: "No se pudieron cargar tus solicitudes."
                isError = true
            } finally {
                loadingList = false
            }
        }
    }

    LaunchedEffect(Unit) { loadRequests() }

    Scaffold(topBar = { TopAppBar(title = { Text("Solicitar un viaje") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedButton(onClick = volver, modifier = Modifier.fillMaxWidth()) {
                Text("← Volver")
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = origin, onValueChange = { origin = it },
                label = { Text("Lugar de salida") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = destination, onValueChange = { destination = it },
                label = { Text("Destino") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    val now = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day -> date = LocalDate.of(year, month + 1, day) },
                        date?.year ?: now.get(Calendar.YEAR),
                        date?.monthValue?.minus(1) ?: now.get(Calendar.MONTH),
                        date?.dayOfMonth ?: now.get(Calendar.DAY_OF_MONTH)
                    ).apply { datePicker.minDate = System.currentTimeMillis() }.show()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Seleccionar fecha") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    val now = Calendar.getInstance()
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> time = hour to minute },
                        time?.first ?: now.get(Calendar.HOUR_OF_DAY),
                        time?.second ?: now.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(time?.let { "%02d:%02d".format(it.first, it.second) } ?: "Seleccionar hora") }
            Spacer(Modifier.height(12.dp))
            Text("Cantidad de pasajeros", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedButton(onClick = { if (passengerCount > 1) passengerCount-- }) { Text("−") }
                Text("  $passengerCount  ", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = { if (passengerCount < 20) passengerCount++ }) { Text("+") }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = comment, onValueChange = { comment = it },
                label = { Text("Comentario (opcional)") },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )
            Spacer(Modifier.height(16.dp))
            if (message.isNotBlank()) {
                Text(message, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }
            Button(
                enabled = !loading && origin.isNotBlank() && destination.isNotBlank() && date != null && time != null,
                onClick = {
                    val chosenDate = date!!
                    val chosenTime = time!!
                    val localDateTime = LocalDateTime.of(chosenDate.year, chosenDate.monthValue, chosenDate.dayOfMonth, chosenTime.first, chosenTime.second)
                    val departure = localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime()
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    scope.launch {
                        loading = true
                        message = ""
                        try {
                            RequestRepository.create(context, origin, destination, departure, passengerCount, comment)
                            message = "Solicitud publicada correctamente."
                            isError = false
                            origin = ""
                            destination = ""
                            date = null
                            time = null
                            passengerCount = 1
                            comment = ""
                            loadRequests()
                        } catch (e: Exception) {
                            message = e.message ?: "No se pudo publicar la solicitud."
                            isError = true
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "Publicando..." else "Publicar solicitud") }

            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Mis solicitudes", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { loadRequests() }, enabled = !loadingList) { Text("Actualizar") }
            }
            if (loadingList) CircularProgressIndicator()
            if (requests.isEmpty() && !loadingList) Text("Todavía no tienes solicitudes.")
            requests.forEach { request ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("${request.origin_text} → ${request.destination_text}", style = MaterialTheme.typography.titleMedium)
                        Text("Pasajeros: ${request.passenger_count}")
                        Text("Salida: ${request.departure_at}")
                        request.status?.let { Text("Estado: $it") }
                        request.comment?.takeIf { it.isNotBlank() }?.let { Text(it) }
                    }
                }
            }
        }
    }
}
