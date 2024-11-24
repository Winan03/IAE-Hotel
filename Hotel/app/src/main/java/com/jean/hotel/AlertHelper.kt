package com.jean.hotel

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class AlertHelper(private val context: Context) {

    // Alerta genérica
    fun showCustomAlert(title: String, message: String, iconRes: Int = R.drawable.ic_warning) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.alert_custom_layout, null)

        val alertTitle = view.findViewById<TextView>(R.id.alertTitle)
        val alertMessage = view.findViewById<TextView>(R.id.alertMessage)
        val alertIcon = view.findViewById<ImageView>(R.id.alertIcon)
        val alertButton = view.findViewById<Button>(R.id.alertButton)

        alertTitle.text = title
        alertMessage.text = message
        alertIcon.setImageResource(iconRes)

        builder.setView(view)
        val dialog = builder.create()

        alertButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Notificación de éxito
    fun showSuccessNotification(message: String) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.success_notification_layout, null)

        val successMessage = view.findViewById<TextView>(R.id.successMessage)
        successMessage.text = message

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = view
        toast.show()
    }

    // Manejo de errores específico
    fun showErrorAlert(errorCode: String?, defaultMessage: String = "Ha ocurrido un error inesperado.") {
        val errorMessage = when (errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "El correo electrónico ya está en uso."
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil."
            "ERROR_INVALID_EMAIL" -> "El formato del correo electrónico no es válido."
            "ERROR_OPERATION_NOT_ALLOWED" -> "El registro con correo electrónico no está habilitado."
            else -> defaultMessage
        }
        showCustomAlert("Error", errorMessage)
    }

    // Alerta informativa genérica
    fun showInfoAlert(title: String, message: String) {
        showCustomAlert(title, message, R.drawable.ic_info)
    }
}

