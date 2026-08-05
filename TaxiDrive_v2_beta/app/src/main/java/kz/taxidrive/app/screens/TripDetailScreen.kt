package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(

    nombre: String,

    telefono: String,

    origen: String,

    destino: String,

    fecha: String,

    hora: String,

    precio: String,

    comentario: String,

    volver: () -> Unit

) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text("Detalle del viaje")

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)

        ) {

            Button(

                onClick = volver,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("← Volver")

            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Origen: $origen")

            Text("Destino: $destino")

            Text("Fecha: $fecha")

            Text("Hora: $hora")

            Text("Precio: $precio ₸")

            Spacer(modifier = Modifier.height(20.dp))

            Text("Comentario")

            Text(comentario)

            Spacer(modifier = Modifier.height(30.dp))

            Button(

                onClick = {

                    // Abrir llamada

                },

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("📞 Llamar")

            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(

                onClick = {

                    // Abrir WhatsApp

                },

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("💬 WhatsApp")

            }

        }

    }

}