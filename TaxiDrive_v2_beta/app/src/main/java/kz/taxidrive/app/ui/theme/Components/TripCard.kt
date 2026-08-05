package kz.taxidrive.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TripCard(

    nombre: String,

    tipo: String,

    origen: String,

    destino: String,

    fecha: String,

    hora: String,

    precio: String,

    comentario: String,

    onCall: () -> Unit,

    onWhatsApp: () -> Unit

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),

        elevation = CardDefaults.cardElevation(6.dp)

    ) {

        Column(

            modifier = Modifier.padding(16.dp)

        ) {

            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(tipo)

            Spacer(modifier = Modifier.height(12.dp))

            Text("📍 $origen")

            Text("⬇")

            Text("📍 $destino")

            Spacer(modifier = Modifier.height(12.dp))

            Text("📅 $fecha")

            Text("🕒 $hora")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "💰 $precio ₸",
                fontWeight = FontWeight.Bold
            )

            if (comentario.isNotBlank()) {

                Spacer(modifier = Modifier.height(10.dp))

                Text(comentario)

            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {

                Button(
                    onClick = onCall,
                    modifier = Modifier.weight(1f)
                ) {

                    Text("📞 Llamar")

                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onWhatsApp,
                    modifier = Modifier.weight(1f)
                ) {

                    Text("💬 WhatsApp")

                }

            }

        }

    }

}