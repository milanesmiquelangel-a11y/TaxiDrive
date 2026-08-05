package kz.taxidrive.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import kz.taxidrive.app.model.TripPost

object TripRepository {

    private val db = FirebaseFirestore.getInstance()
    private val publications = mutableListOf<TripPost>()

    fun add(post: TripPost) {
        db.collection("viajes")
            .add(post)
            .addOnSuccessListener {
                publications.add(post)
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun getAll(): List<TripPost> {
        return publications
    }

    fun clear() {
        publications.clear()
    }

    fun loadTrips(onResult: (List<TripPost>) -> Unit) {
        db.collection("viajes")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(TripPost::class.java)
                publications.clear()
                publications.addAll(lista)
                onResult(lista)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}