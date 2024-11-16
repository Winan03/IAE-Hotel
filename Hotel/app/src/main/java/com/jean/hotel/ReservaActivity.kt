package com.jean.hotel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReservaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserva)

        val roomId = intent.getIntExtra("roomId", -1)

        val confirmButton = findViewById<Button>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            // Simular una solicitud de reserva
            reservarHabitacion(roomId)
        }
    }

    private fun reservarHabitacion(roomId: Int) {

        Toast.makeText(this, "Habitación $roomId reservada con éxito", Toast.LENGTH_SHORT).show()
        val resultIntent = Intent()
        resultIntent.putExtra("roomId", roomId)
        setResult(RESULT_OK, resultIntent)

        finish()
    }
}