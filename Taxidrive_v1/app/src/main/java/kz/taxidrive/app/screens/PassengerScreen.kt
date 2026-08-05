package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    var precio by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text("Publicar solicitud")

                }

            )

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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = destino,
                onValueChange = { destino = it },
                label = { Text("Destino") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = hora,
                onValueChange = { hora = it },
                label = { Text("Hora") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio ofrecido (₸)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentarios (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (mensaje.isNotEmpty()) {
                Text(mensaje)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {

                    if (
                        origen.isBlank() ||
                        destino.isBlank() ||
                        fecha.isBlank() ||
                        hora.isBlank() ||
                        precio.isBlank()
                    ) {

                        mensaje = "Complete todos los campos obligatorios"

                    } else {

                        val viaje = TripPost(

                            type = "pasajero",

                            origin = origen,

                            destination = destino,

                            date = fecha,

                            time = hora,

                            price = precio,

                            comment = comentario,

                            phone = "",

                            userId = "",

                            status = "activo"

                        )

                        RequestRepository.add(viaje)

                        mensaje = "Solicitud publicada"

                        origen = ""
                        destino = ""
                        fecha = ""
                        hora = ""
                        precio = ""
                        comentario = ""

                    }

                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Publicar solicitud")

            }

        }

    }

}