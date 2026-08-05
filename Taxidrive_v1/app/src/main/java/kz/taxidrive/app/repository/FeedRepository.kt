package kz.taxidrive.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import kz.taxidrive.app.model.Offer
import kz.taxidrive.app.model.Request

object FeedRepository {

    private val db = FirebaseFirestore.getInstance()

    fun loadOffers(
        onResult: (List<Offer>) -> Unit
    ) {

        db.collection("ofertas")
            .get()
            .addOnSuccessListener { result ->

                val lista = result.toObjects(Offer::class.java)

                onResult(lista)

            }
            .addOnFailureListener {

                onResult(emptyList())

            }

    }

    fun loadRequests(
        onResult: (List<Request>) -> Unit
    ) {

        db.collection("solicitudes")
            .get()
            .addOnSuccessListener { result ->

                val lista = result.toObjects(Request::class.java)

                onResult(lista)

            }
            .addOnFailureListener {

                onResult(emptyList())

            }

    }

    fun searchOffers(
        origin: String,
        destination: String,
        date: String,
        onResult: (List<Offer>) -> Unit
    ) {

        db.collection("ofertas")
            .get()
            .addOnSuccessListener { result ->

                var lista = result.toObjects(Offer::class.java)

                if (origin.isNotBlank()) {
                    lista = lista.filter {
                        it.origin.contains(origin, ignoreCase = true)
                    }
                }

                if (destination.isNotBlank()) {
                    lista = lista.filter {
                        it.destination.contains(destination, ignoreCase = true)
                    }
                }

                if (date.isNotBlank()) {
                    lista = lista.filter {
                        it.date == date
                    }
                }

                onResult(lista)

            }
            .addOnFailureListener {

                onResult(emptyList())

            }

    }

}