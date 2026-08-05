package kz.taxidrive.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import kz.taxidrive.app.model.TripPost

object OfferRepository {

    private val db = FirebaseFirestore.getInstance()

    fun add(
        offer: TripPost,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {

        db.collection("ofertas")
            .add(offer)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }

    }

}