package com.jean.hotel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ResumenActivity : AppCompatActivity() {

    private lateinit var resumenUsuario: TextView
    private lateinit var resumenDni: TextView
    private lateinit var resumenPrecio: TextView
    private lateinit var resumenDuracion: TextView
    private lateinit var resumenFechas: TextView
    private lateinit var confirmarButton: Button

    private var userName: String = ""
    private var userLastname: String = ""
    private var userDni: String = ""
    private var reservationDuration: String = ""
    private var finalPrice: Double = 0.0
    private var roomId: Int = 0 // ID de la habitación
    private val firestore = FirebaseFirestore.getInstance() // Instancia de Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen)

        // Inicializar vistas
        resumenUsuario = findViewById(R.id.resumen_usuario)
        resumenDni = findViewById(R.id.resumen_dni)
        resumenPrecio = findViewById(R.id.resumen_precio)
        resumenDuracion = findViewById(R.id.resumen_duracion)
        resumenFechas = findViewById(R.id.resumen_fechas)
        confirmarButton = findViewById(R.id.btn_confirmar_reserva)

        // Recibir datos del Intent
        intent.extras?.let {
            userName = it.getString("userName", "")
            userLastname = it.getString("userLastname", "")
            userDni = it.getString("userDni", "")
            reservationDuration = it.getString("reservationDuration", "")
            finalPrice = it.getDouble("finalPrice", 0.0)
            roomId = it.getInt("roomId", 0)
        }

        // Validar datos recibidos
        if (finalPrice == 0.0 || roomId == 0) {
            Toast.makeText(this, getString(R.string.error_invalid_reservation), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Calcular fechas de inicio y fin
        val startDate = Date()
        val endDate = calculateEndDate(startDate, reservationDuration)

        // Mostrar datos en la interfaz
        resumenUsuario.text = getString(R.string.reservation_user, userName, userLastname)
        resumenDni.text = getString(R.string.reservation_dni, userDni)
        resumenPrecio.text = getString(R.string.reservation_price, finalPrice)
        resumenDuracion.text = getString(R.string.reservation_duration, reservationDuration)
        resumenFechas.text = getString(
            R.string.reservation_dates,
            android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", startDate),
            android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", endDate)
        )

        confirmarButton.setOnClickListener {
            confirmReservation(endDate)
        }
    }

    // Confirmar reserva y actualizar Firestore
    private fun confirmReservation(endDate: Date) {
        firestore.collection("rooms")
            .document(roomId.toString())
            .update(
                "reserved", true,
                "reservedBy", "$userName $userLastname",
                "reservedUntil", Timestamp(endDate)
            )
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.reservation_confirmed), Toast.LENGTH_SHORT).show()

                // Navegar a la actividad principal
                val intent = Intent(this, ActivityHome::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.error_confirming_reservation), Toast.LENGTH_SHORT).show()
            }
    }

    // Calcular la fecha de fin según la duración
    private fun calculateEndDate(startDate: Date, duration: String): Date {
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        when (duration) {
            "6h" -> calendar.add(Calendar.HOUR_OF_DAY, 6)
            "12h" -> calendar.add(Calendar.HOUR_OF_DAY, 12)
            "1day" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.time
    }
}
