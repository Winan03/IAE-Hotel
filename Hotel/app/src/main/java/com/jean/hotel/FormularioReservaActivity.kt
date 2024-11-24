package com.jean.hotel

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class FormularioReservaActivity : AppCompatActivity() {

    // Variables para las vistas
    private lateinit var nameEditText: EditText
    private lateinit var lastnameEditText: EditText
    private lateinit var dniEditText: EditText
    private lateinit var startDateText: TextView
    private lateinit var endDateText: TextView
    private lateinit var nextButton: Button
    private lateinit var roomImageView: ImageView

    // Variables para datos
    private var roomPrice: Double = 0.0
    private var roomId: Int = -1
    private lateinit var roomPhotoUrl: String
    private var startDate: String = ""
    private var endDate: String = ""
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.formulario_reserva)

        // 1. Inicializar vistas
        initViews()

        // 2. Cargar datos recibidos del Intent
        loadDataFromIntent()

        // 3. Configurar selección de fechas y horas
        setupDateTimePickers()

        // 4. Configurar botón de continuar
        nextButton.setOnClickListener {
            if (validateInputs()) {
                if (validateDates()) {
                    navigateToNextActivity()
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Inicializa las vistas de la actividad.
     */
    private fun initViews() {
        nameEditText = findViewById(R.id.user_name)
        lastnameEditText = findViewById(R.id.user_lastname)
        dniEditText = findViewById(R.id.user_dni)
        startDateText = findViewById(R.id.start_date_text)
        endDateText = findViewById(R.id.end_date_text)
        nextButton = findViewById(R.id.next_button)
        roomImageView = findViewById(R.id.background_image)
    }

    /**
     * Carga los datos recibidos del Intent y valida su existencia.
     */
    private fun loadDataFromIntent() {
        roomPrice = intent.getDoubleExtra("roomPrice", -1.0)
        roomId = intent.getIntExtra("roomId", -1)
        roomPhotoUrl = intent.getStringExtra("roomPhoto") ?: ""

        if (roomPrice == -1.0 || roomId == -1 || roomPhotoUrl.isEmpty()) {
            Toast.makeText(this, "Error: Datos de la habitación no válidos", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Cargar imagen de la habitación
        Glide.with(this)
            .load(roomPhotoUrl)
            .placeholder(R.drawable.ic_room_placeholder)
            .error(R.drawable.ic_error_image)
            .into(roomImageView)
    }

    /**
     * Configura los `DateTimePickers` para seleccionar las fechas de inicio y fin.
     */
    private fun setupDateTimePickers() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        startDateText.setOnClickListener {
            showDateTimePicker(calendar) { selectedDate ->
                startDate = dateFormat.format(selectedDate.time)
                startDateText.text = startDate
            }
        }

        endDateText.setOnClickListener {
            showDateTimePicker(calendar) { selectedDate ->
                endDate = dateFormat.format(selectedDate.time)
                endDateText.text = endDate
            }
        }
    }

    /**
     * Muestra un selector de fecha y hora.
     */
    private fun showDateTimePicker(calendar: Calendar, onDateTimeSelected: (Calendar) -> Unit) {
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                onDateTimeSelected(calendar)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    /**
     * Calcula el precio final basado en la duración de la reserva.
     */
    private fun calculateFinalPrice(): Double {
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona las fechas correctamente.", Toast.LENGTH_SHORT).show()
            return 0.0
        }

        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val start = sdf.parse(startDate)
            val end = sdf.parse(endDate)

            if (start != null && end != null && start.before(end)) {
                val durationInMillis = end.time - start.time
                val durationInHours = durationInMillis / (1000 * 60 * 60)

                val pricePerHour = roomPrice / 24.0
                return pricePerHour * durationInHours
            } else {
                Toast.makeText(this, "La fecha de inicio debe ser anterior a la fecha de fin.", Toast.LENGTH_SHORT).show()
                return 0.0
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar las fechas seleccionadas.", Toast.LENGTH_SHORT).show()
            return 0.0
        }
    }

    /**
     * Valida que las fechas seleccionadas sean correctas.
     */
    private fun validateDates(): Boolean {
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona las fechas de inicio y fin.", Toast.LENGTH_SHORT).show()
            return false
        }

        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val start = sdf.parse(startDate)
            val end = sdf.parse(endDate)

            if (start != null && end != null && start.before(end)) {
                return true
            } else {
                Toast.makeText(this, "La fecha de inicio debe ser anterior a la fecha de fin.", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al validar las fechas.", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    /**
     * Valida las entradas del usuario.
     */
    private fun validateInputs(): Boolean {
        return nameEditText.text.isNotEmpty() &&
                lastnameEditText.text.isNotEmpty() &&
                dniEditText.text.isNotEmpty() &&
                startDateText.text.isNotEmpty() &&
                endDateText.text.isNotEmpty()
    }

    /**
     * Navega a la siguiente actividad (Método de Pago).
     */
    private fun navigateToNextActivity() {
        val finalPrice = calculateFinalPrice()

        if (finalPrice == 0.0) {
            Toast.makeText(this, "El precio calculado no es válido.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, MetodoPagoActivity::class.java).apply {
            putExtra("userId", dniEditText.text.toString()) // Asume que el DNI actúa como userId
            putExtra("userName", nameEditText.text.toString())
            putExtra("userLastname", lastnameEditText.text.toString())
            putExtra("userDni", dniEditText.text.toString())
            putExtra("reservationDuration", "Personalizado")
            putExtra("finalPrice", finalPrice)
            putExtra("roomId", roomId)
        }

        // Log para depuración
        Log.d("FormularioReservaActivity", "Enviando datos a MetodoPagoActivity: $finalPrice, $roomId")
        startActivity(intent)
    }

}
