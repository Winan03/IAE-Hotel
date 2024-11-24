package com.jean.hotel

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RoomManager(private val firestore: FirebaseFirestore) {

    fun checkAndReleaseExpiredRooms(callback: (Boolean, String?) -> Unit) {
        val now = Date()
        val roomsRef = firestore.collection("rooms")

        roomsRef.whereEqualTo("reserved", true)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val reservedUntil = document.getTimestamp("reservedUntil")?.toDate()
                    if (reservedUntil != null && reservedUntil.before(now)) {
                        // Liberar habitación si la reserva ha expirado
                        roomsRef.document(document.id).update(
                            mapOf(
                                "reserved" to false,
                                "reservedBy" to null,
                                "reservedUntil" to null
                            )
                        ).addOnSuccessListener {
                            callback(true, null) // Habitación liberada
                        }.addOnFailureListener {
                            callback(false, "Error al liberar la habitación ${document.id}")
                        }
                    }
                }
            }
            .addOnFailureListener {
                callback(false, "Error al consultar las habitaciones.")
            }
    }
}
