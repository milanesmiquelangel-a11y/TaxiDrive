package kz.taxidrive.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import kz.taxidrive.app.model.User

object UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun registerUser(
        user: User,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {

        db.collection("usuarios")
            .document(user.uid)
            .set(user)
            .addOnSuccessListener {

                onSuccess()

            }
            .addOnFailureListener {

                onFailure()

            }

    }

    fun getUser(

        uid: String,

        onResult: (User?) -> Unit

    ) {

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener {

                onResult(it.toObject(User::class.java))

            }
            .addOnFailureListener {

                onResult(null)

            }

    }

}