package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    abrirRegistro: () -> Unit,
    loginCorrecto: (String) -> Unit
) {
    var telefono by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Iniciar sesión") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { if (telefono.isNotBlank()) loginCorrecto(telefono.trim()) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Entrar") }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = abrirRegistro, modifier = Modifier.fillMaxWidth()) {
                Text("Crear cuenta")
            }
        }
    }
}