package com.jean.hotel

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MetodoPagoActivity : AppCompatActivity() {

    private lateinit var cardFront: View
    private lateinit var cardBack: View
    private lateinit var flipButton: Button
    private lateinit var reservarButton: Button
    private lateinit var loadingIndicator: ProgressBar

    private lateinit var cardNumberEdit: EditText
    private lateinit var cardNameEdit: EditText
    private lateinit var cardExpiryEdit: EditText
    private lateinit var cardCvvEdit: EditText

    private var isFrontVisible = true
    private val firestore = FirebaseFirestore.getInstance()

    private var userId: String = ""
    private var finalPrice: Double = 0.0
    private var roomId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.metodo_pago)

        // Inicializar vistas
        cardFront = findViewById(R.id.card_front)
        cardBack = findViewById(R.id.card_back)
        flipButton = findViewById(R.id.flip_card_button)
        reservarButton = findViewById(R.id.btn_reservar)
        loadingIndicator = findViewById(R.id.loading_indicator)
        cardNumberEdit = findViewById(R.id.card_number_edit)
        cardNameEdit = findViewById(R.id.card_name_edit)
        cardExpiryEdit = findViewById(R.id.card_expiry_edit)
        cardCvvEdit = findViewById(R.id.card_cvv_edit)

        // Recibir datos
        intent.extras?.let {
            userId = it.getString("userId", "")
            finalPrice = it.getDouble("finalPrice", 0.0)
            roomId = it.getInt("roomId", 0)
        }

        // Configurar el botón de voltear
        flipButton.setOnClickListener { flipCard() }

        // Configurar el botón de reservar
        reservarButton.setOnClickListener {
            val cardNumber = cardNumberEdit.text.toString()
            val cardName = cardNameEdit.text.toString()
            val cardExpiry = cardExpiryEdit.text.toString()
            val cardCvv = cardCvvEdit.text.toString()

            if (validateInputs(cardNumber, cardName, cardExpiry, cardCvv)) {
                saveCardToFirestore(cardNumber, cardName, cardExpiry, cardCvv)
            } else {
                Toast.makeText(this, "Por favor, completa todos los datos correctamente.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun flipCard() {
        val scale = applicationContext.resources.displayMetrics.density
        cardFront.cameraDistance = 8000 * scale
        cardBack.cameraDistance = 8000 * scale

        val flipOutAnimatorSet = AnimatorInflater.loadAnimator(this, R.animator.card_flip_out) as AnimatorSet
        val flipInAnimatorSet = AnimatorInflater.loadAnimator(this, R.animator.card_flip_in) as AnimatorSet

        if (isFrontVisible) {
            flipOutAnimatorSet.setTarget(cardFront)
            flipInAnimatorSet.setTarget(cardBack)
            cardFront.visibility = View.GONE
            cardBack.visibility = View.VISIBLE
        } else {
            flipOutAnimatorSet.setTarget(cardBack)
            flipInAnimatorSet.setTarget(cardFront)
            cardBack.visibility = View.GONE
            cardFront.visibility = View.VISIBLE
        }

        flipOutAnimatorSet.start()
        flipInAnimatorSet.start()
        isFrontVisible = !isFrontVisible
    }

    private fun validateInputs(cardNumber: String, cardName: String, cardExpiry: String, cardCvv: String): Boolean {
        return cardNumber.length == 16 && cardName.isNotEmpty() && cardExpiry.matches(Regex("\\d{2}/\\d{2}")) && cardCvv.length == 3
    }

    private fun saveCardToFirestore(cardNumber: String, cardName: String, cardExpiry: String, cardCvv: String) {
        val cardData = hashMapOf(
            "cardNumber" to cardNumber,
            "cardholderName" to cardName,
            "expiryDate" to cardExpiry,
            "cvv" to cardCvv,
            "timestamp" to Timestamp.now(),
            "userId" to userId
        )

        firestore.collection("cards").add(cardData)
            .addOnSuccessListener {
                Toast.makeText(this, "Tarjeta guardada correctamente.", Toast.LENGTH_SHORT).show()

                // Navegar a ResumenActivity
                val intent = Intent(this, ResumenActivity::class.java).apply {
                    putExtra("userName", intent.getStringExtra("userName"))
                    putExtra("userLastname", intent.getStringExtra("userLastname"))
                    putExtra("userDni", intent.getStringExtra("userDni"))
                    putExtra("reservationDuration", "Personalizado")
                    putExtra("finalPrice", intent.getDoubleExtra("finalPrice", 0.0))
                    putExtra("roomId", intent.getIntExtra("roomId", 0))
                }
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar la tarjeta.", Toast.LENGTH_SHORT).show()
            }
    }
}







