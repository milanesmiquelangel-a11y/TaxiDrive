package kz.taxidrive.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import kz.taxidrive.app.model.TripPost

object RequestRepository {

    private val db = FirebaseFirestore.getInstance()

    fun add(
        request: TripPost,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {

        db.collection("solicitudes")
            .add(request)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }

    }

}