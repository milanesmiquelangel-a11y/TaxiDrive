package kz.taxidrive.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kz.taxidrive.app.model.User
import kz.taxidrive.app.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(volver: () -> Unit) {
    var drivers by remember { mutableStateOf<List<User>>(emptyList()) }
    var mensaje by remember { mutableStateOf("") }
    var autorizado by remember { mutableStateOf<Boolean?>(null) }
    var comprobando by remember { mutableStateOf(true) }

    fun cargar() {
        UserRepository.getDrivers(
            { drivers = it },
            { mensaje = it.message ?: "Error al cargar conductores" }
        )
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            autorizado = false
            comprobando = false
        } else {
            FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
                .addOnSuccessListener { usuario ->
                    val tipoAdmin = usuario.getString("tipo") == "ADMIN"
                    FirebaseFirestore.getInstance().collection("administradores").document(uid).get()
                        .addOnSuccessListener { adminDoc ->
                            autorizado = tipoAdmin && adminDoc.exists()
                            comprobando = false
                            if (autorizado == true) cargar()
                        }
                        .addOnFailureListener {
                            autorizado = false
                            comprobando = false
                            mensaje = "No se pudo verificar el permiso de administrador."
                        }
                }
                .addOnFailureListener {
                    autorizado = false
                    comprobando = false
                    mensaje = "No se pudo verificar la identidad del administrador."
                }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Panel de Administración") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Button(onClick = volver, modifier = Modifier.fillMaxWidth()) { Text("← Volver") }
            Spacer(Modifier.height(20.dp))

            when {
                comprobando -> Text("Verificando permisos…")
                autorizado != true -> Text("Acceso denegado. Se requiere una sesión autenticada y autorización de administrador.")
                else -> {
                    Text("Conductores", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(12.dp))
                    if (drivers.isEmpty()) Text("No hay conductores registrados.")
                    drivers.forEach { driver ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(Modifier.padding(14.dp)) {
                                Text("${driver.nombre} ${driver.apellidos}")
                                Text("Teléfono: ${driver.telefono}")
                                Text("Aprobado: ${if (driver.aprobado) "Sí" else "No"}")
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
                }
            }
            if (mensaje.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(mensaje)
            }
        }
    }
}
