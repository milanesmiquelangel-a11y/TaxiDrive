package kz.taxidrive.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import kz.taxidrive.app.model.User

object UserRepository {
    private val db = FirebaseFirestore.getInstance()

    fun registerUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("usuarios").document(user.uid).set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUser(uid: String, onResult: (User?) -> Unit) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { onResult(it.toObject(User::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun requestActivation(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("usuarios").document(uid)
            .update(
                mapOf(
                    "activationPending" to true,
                    "activationAmount" to 500L
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun approveDriver(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val trialUntil = System.currentTimeMillis() + 30L * 24L * 60L * 60L * 1000L
        db.collection("usuarios").document(uid)
            .update(
                mapOf(
                    "aprobado" to true,
                    "trialUntil" to trialUntil
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun confirmActivation(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val activationUntil = System.currentTimeMillis() + 24L * 60L * 60L * 1000L
        db.collection("usuarios").document(uid)
            .update(
                mapOf(
                    "activationPending" to false,
                    "activationUntil" to activationUntil,
                    "activationAmount" to 500L
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getDrivers(onResult: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("usuarios").whereEqualTo("tipo", "conductor").get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.documents.mapNotNull { it.toObject(User::class.java) })
            }
            .addOnFailureListener { onFailure(it) }
    }
}