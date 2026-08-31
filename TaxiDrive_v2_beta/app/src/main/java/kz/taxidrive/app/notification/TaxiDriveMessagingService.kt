package kz.taxidrive.app.notification

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TaxiDriveMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        guardarToken(token)
    }

    override fun onMessageReceived(mensaje: RemoteMessage) {
        super.onMessageReceived(mensaje)

        val titulo = mensaje.notification?.title
            ?: mensaje.data["titulo"]
            ?: "TaxiDrive"

        val cuerpo = mensaje.notification?.body
            ?: mensaje.data["mensaje"]
            ?: mensaje.data["body"]
            ?: ""

        if (cuerpo.isNotEmpty()) {
            NotificationHelper.mostrarNotificacion(
                context = this,
                id = System.currentTimeMillis().toInt(),
                titulo = titulo,
                mensaje = cuerpo
            )
        }
    }

    private fun guardarToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .update("fcmToken", token)
            .addOnFailureListener { e ->
                Log.e("FCM", "Error al guardar token", e)
            }
    }
}
