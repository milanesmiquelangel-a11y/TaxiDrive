package kz.taxidrive.app.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferChatScreen(offerId: Int, volver: () -> Unit) {
    val context = LocalContext.current
    var messages by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun loadMessages() {
        loading = true
        error = ""
        PassengerApiRepository.getOfferMessages(
            context = context,
            offerId = offerId,
            onSuccess = { items ->
                messages = (0 until items.length()).mapNotNull { items.optJSONObject(it) }
                loading = false
            },
            onFailure = { problem ->
                error = problem
                loading = false
            }
        )
    }

    LaunchedEffect(offerId) { loadMessages() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat del viaje") },
                navigationIcon = {
                    TextButton(onClick = volver) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            Text("Conversación de la oferta #$offerId", style = MaterialTheme.typography.titleMedium)
            Text(
                "Este chat solo está disponible para el pasajero y el conductor de la oferta aceptada.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { loadMessages() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (loading) "Actualizando..." else "Actualizar mensajes") }

            if (error.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (loading && messages.isEmpty()) {
                    CircularProgressIndicator()
                } else if (messages.isEmpty() && error.isBlank()) {
                    Text("Todavía no hay mensajes. Puedes iniciar la conversación.")
                }
                messages.forEach { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text("Usuario #${item.optInt("sender_user_id")}", style = MaterialTheme.typography.labelSmall)
                            Text(item.optString("message"))
                            Text(item.optString("created_at"), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(2000) },
                label = { Text("Escribe un mensaje") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !sending,
                maxLines = 4
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty() && !sending) {
                        sending = true
                        error = ""
                        PassengerApiRepository.sendOfferMessage(
                            context = context,
                            offerId = offerId,
                            message = text,
                            onSuccess = {
                                draft = ""
                                sending = false
                                loadMessages()
                            },
                            onFailure = { problem ->
                                sending = false
                                error = problem
                            }
                        )
                    }
                },
                enabled = draft.isNotBlank() && !sending,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (sending) "Enviando..." else "Enviar mensaje") }
        }
    }
}
