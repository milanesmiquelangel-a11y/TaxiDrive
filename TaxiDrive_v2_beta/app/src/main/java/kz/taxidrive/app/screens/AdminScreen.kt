package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kz.taxidrive.app.model.User
import kz.taxidrive.app.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(volver: () -> Unit) {
    var drivers by remember { mutableStateOf<List<User>>(emptyList()) }
    var mensaje by remember { mutableStateOf("") }

    fun cargar() {
        UserRepository.getDrivers({ drivers = it }, { mensaje = it.message ?: "Error al cargar conductores" })
    }
    LaunchedEffect(Unit) { cargar() }

    Scaffold(topBar = { TopAppBar(title = { Text("Panel de Administración") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            Button(volver, modifier = Modifier.fillMaxWidth()) { Text("← Volver") }
            Spacer(Modifier.height(20.dp))
            Text("Conductores", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            if (drivers.isEmpty()) Text("No hay conductores registrados.")
            drivers.forEach { driver ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("\${driver.nombre} \${driver.apellidos}")
                        Text("Teléfono: \${driver.telefono}")
                        Text("Aprobado: \${if (driver.aprobado) "Sí" else "No"}")
                        if (!driver.aprobado) {
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                UserRepository.approveDriver(driver.uid, {
                                    mensaje = "Conductor aprobado. Se inició su mes gratuito."
                                    cargar()
                                }, { mensaje = it.message ?: "Error al aprobar" })
                            }) { Text("Aprobar conductor") }
                        }
                        if (driver.activationPending) {
                            Spacer(Modifier.height(8.dp))
                            Text("Activación pendiente: 500 ₸")
                            Button(onClick = {
                                UserRepository.confirmActivation(driver.uid, {
                                    mensaje = "Activación confirmada por 24 horas."
                                    cargar()
                                }, { mensaje = it.message ?: "Error al confirmar activación" })
                            }) { Text("Confirmar activación (prueba)") }
                        }
                    }
                }
            }
            if (mensaje.isNotEmpty()) { Spacer(Modifier.height(12.dp)); Text(mensaje) }
        }
    }
}