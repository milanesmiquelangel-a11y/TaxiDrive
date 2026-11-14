package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(volver: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Panel de Administración") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = volver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← Volver")
            }

            Text(
                text = "Administración temporalmente protegida",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Las operaciones administrativas están deshabilitadas mientras se conectan " +
                    "a los endpoints seguros del servidor."
            )

            Text(
                text = "La aprobación de conductores debe registrarse en el servidor. " +
                    "Las activaciones de 500 ₸ solo podrán confirmarse después de verificar " +
                    "un pago real. Esta pantalla no consulta ni modifica Firebase."
            )

            Text(
                text = "No se ha realizado ningún pago, aprobación ni activación desde esta pantalla.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
