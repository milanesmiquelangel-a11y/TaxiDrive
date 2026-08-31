package kz.taxidrive.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import kz.taxidrive.app.notification.NotificationHelper
import kz.taxidrive.app.screens.AdminScreen
import kz.taxidrive.app.screens.DriverScreen
import kz.taxidrive.app.screens.LoginScreen
import kz.taxidrive.app.screens.PassengerScreen
import kz.taxidrive.app.screens.RegisterScreen
import kz.taxidrive.app.ui.theme.TaxiDriveTheme

class MainActivity : ComponentActivity() {

    private val permisoNotificaciones = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) {
            Log.w("FCM", "Permiso de notificaciones denegado")
        }
    }

    private var listenerSolicitudes: ListenerRegistration? = null
    private val solicitudesVistas = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        NotificationHelper.crearCanal(this)
        solicitarPermisoNotificaciones()
        obtenerTokenFCM()
        escucharSolicitudesParaConductor()

        setContent {
            TaxiDriveTheme {
                TaxiDriveApp()
            }
        }
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun obtenerTokenFCM() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM", "Token de registro: $token")
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .update("fcmToken", token)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerSolicitudes?.remove()
        listenerSolicitudes = null
    }

    // Escucha nuevas solicitudes de pasajeros en tiempo real y notifica al conductor
    // solo cuando la ruta (origen y destino) coincide con una oferta publicada.
    private fun escucharSolicitudesParaConductor() {
        val db = FirebaseFirestore.getInstance()
        listenerSolicitudes = db.collection("solicitudes")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                // Primera carga: registrar solicitudes existentes sin notificar
                if (solicitudesVistas.isEmpty() && snapshots.documents.isNotEmpty()) {
                    for (doc in snapshots.documents) {
                        solicitudesVistas.add(doc.id)
                    }
                    return@addSnapshotListener
                }

                for (cambio in snapshots.documentChanges) {
                    if (cambio.type == DocumentChange.Type.ADDED &&
                        solicitudesVistas.add(cambio.document.id)
                    ) {
                        val origen = cambio.document.getString("origin") ?: ""
                        val destino = cambio.document.getString("destination") ?: ""
                        verificarRutaYNotificar(origen, destino)
                    }
                }
            }
    }

    private fun verificarRutaYNotificar(origen: String, destino: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("ofertas").get().addOnSuccessListener { result ->
            val coincide = result.documents.any { oferta ->
                val o = (oferta.getString("origin") ?: "").trim()
                val d = (oferta.getString("destination") ?: "").trim()
                o.equals(origen.trim(), ignoreCase = true) &&
                    d.equals(destino.trim(), ignoreCase = true)
            }
            if (coincide) {
                NotificationHelper.mostrarNotificacion(
                    context = this,
                    id = System.currentTimeMillis().toInt(),
                    titulo = "Nueva solicitud de viaje",
                    mensaje = if (origen.isNotEmpty() && destino.isNotEmpty())
                        "$origen → $destino" else "Un pasajero publicó una solicitud en tu ruta"
                )
            }
        }
    }
}

@Composable
fun TaxiDriveApp() {

    var pantalla by remember {
        mutableStateOf("login")
    }

    when (pantalla) {

        "login" -> LoginScreen(
            abrirRegistro = {
                pantalla = "registro"
            },
            loginCorrecto = {
                pantalla = "inicio"
            }
        )

        "registro" -> RegisterScreen(
            volverLogin = {
                pantalla = "login"
            }
        )

        "inicio" -> PantallaInicio(

            abrirPasajero = {
                pantalla = "pasajero"
            },

            abrirConductor = {
                pantalla = "conductor"
            },

            abrirAdministrador = {
                pantalla = "admin"
            }

        )

        "pasajero" -> PassengerScreen(
            volver = {
                pantalla = "inicio"
            }
        )

        "conductor" -> DriverScreen(
            volver = {
                pantalla = "inicio"
            }
        )

        "admin" -> AdminScreen(
            volver = {
                pantalla = "inicio"
            }
        )
    }
}

@Composable
fun PantallaInicio(

    abrirPasajero: () -> Unit,

    abrirConductor: () -> Unit,

    abrirAdministrador: () -> Unit

) {

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Center

        ) {

            Text(

                text = "🚖 TaxiDrive.kz",

                fontSize = 34.sp,

                fontWeight = FontWeight.Bold

            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(

                onClick = abrirPasajero,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Soy pasajero")

            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(

                onClick = abrirConductor,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Soy conductor")

            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(

                onClick = abrirAdministrador,

                modifier = Modifier.fillMaxWidth()

            ) {

                Text("Administrador")

            }

            Spacer(modifier = Modifier.height(40.dp))

            Text("Idioma")

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🇪🇸 Español")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🇷🇺 Русский")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🇬🇧 English")
            }

        }

    }

}
