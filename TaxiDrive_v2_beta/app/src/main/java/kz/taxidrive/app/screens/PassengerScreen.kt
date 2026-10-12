package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kz.taxidrive.app.model.TripPost
import kz.taxidrive.app.repository.RequestRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerScreen(
    volver: () -> Unit
) {
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Publicar solicitud") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                onClick = volver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← Volver")
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = origen,
                onValueChange = { origen = it },
                label = { Text("Origen") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !enviando
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = destino,
                onValueChange = { destino = it },
                label = { Text("Destino") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !enviando
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !enviando
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = hora,
                onValueChange = { hora = it },
                label = { Text("Hora") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !enviando
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentarios (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !enviando
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (mensaje.isNotEmpty()) {
                Text(mensaje)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    if (origen.isBlank() || destino.isBlank() ||
                        fecha.isBlank() || hora.isBlank()
                    ) {
                        mensaje = "Complete origen, destino, fecha y hora."
                        return@Button
                    }

                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid.isNullOrBlank()) {
                        mensaje = "Debe iniciar sesión correctamente antes de publicar."
                        return@Button
                    }

                    enviando = true
                    mensaje = ""

                    val solicitud = TripPost(
                        type = "pasajero",
                        origin = origen.trim(),
                        destination = destino.trim(),
                        date = fecha.trim(),
                        time = hora.trim(),
                        price = "",
                        comment = comentario.trim(),
                        phone = "",
                        userId = uid,
                        status = "activo"
                    )

                    RequestRepository.add(
                        request = solicitud,
                        onSuccess = {
                            mensaje = "Solicitud publicada correctamente."
                            origen = ""
                            destino = ""
                            fecha = ""
                            hora = ""
                            comentario = ""
                            enviando = false
                        },
                        onFailure = { error ->
                            mensaje = "No se pudo publicar la solicitud. Compruebe la conexión e inténtelo de nuevo."
                            enviando = false
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !enviando
            ) {
                Text(if (enviando) "Publicando…" else "Publicar solicitud")
            }
        }
    }
}
