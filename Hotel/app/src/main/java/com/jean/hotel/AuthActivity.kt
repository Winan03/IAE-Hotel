package com.jean.hotel

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        val forgotPasswordText = findViewById<TextView>(R.id.forgotPasswordText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integracion de base de datos")
        analytics.logEvent("InitScreen", bundle)

        forgotPasswordText.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this, "Por favor, ingresa tu correo electrónico",
                    Toast.LENGTH_SHORT).show()
            }
        }

        setup()
        session()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Correo de recuperación enviado a $email",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al enviar el correo de recuperación",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        val authLayout = findViewById<LinearLayout>(R.id.authLayout)
        super.onStart()
        authLayout.visibility = View.VISIBLE
    }

    private fun session() {
        val authLayout = findViewById<LinearLayout>(R.id.authLayout)
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)
        if (email != null && provider != null) {
            authLayout.visibility = View.INVISIBLE
            showHome(email, Activity_home.ProviderType.valueOf(provider))
        }
    }

    private fun setup() {
        title = "Autenticación"

        val loginButton = findViewById<Button>(R.id.loginButton)
        val googleButton = findViewById<Button>(R.id.googleButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validaciones personalizadas
            if (!isValidEmail(email)) {
                showAlert("Formato de correo no válido. Ejemplo: letra123@dominio.com")
                return@setOnClickListener
            }
            if (!isValidPassword(password)) {
                showAlert("La contraseña debe contener al menos una letra y un número.")
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showHome(task.result?.user?.email ?: "", Activity_home.ProviderType.BASIC)
                    } else {
                        val errorCode = (task.exception as FirebaseAuthException).errorCode
                        handleAuthError(errorCode)
                    }
                }
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validaciones personalizadas
            if (!isValidEmail(email)) {
                showAlert("Formato de correo no válido. Ejemplo: letra123@dominio.com")
                return@setOnClickListener
            }
            if (!isValidPassword(password)) {
                showAlert("La contraseña debe contener al menos una letra y un número.")
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showHome(task.result?.user?.email ?: "", Activity_home.ProviderType.BASIC)
                    } else {
                        val errorCode = (task.exception as FirebaseAuthException).errorCode
                        handleAuthError(errorCode)
                    }
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[a-zA-Z0-9]+@[a-zA-Z]+\\.com$"
        return Regex(emailPattern).matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$"
        return Regex(passwordPattern).matches(password)
    }

    private fun handleAuthError(errorCode: String) {
        val message = when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "El formato del correo electrónico es inválido."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "El correo electrónico ya está en uso. Usa uno diferente."
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil. Usa al menos 6 caracteres."
            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta. Inténtalo nuevamente."
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo electrónico."
            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada."
            "ERROR_TOO_MANY_REQUESTS" -> "Se han realizado demasiados intentos. Intenta más tarde."
            "ERROR_OPERATION_NOT_ALLOWED" -> "El registro con correo electrónico y contraseña no está habilitado."
            else -> "Se produjo un error desconocido. Inténtalo nuevamente."
        }
        showAlert(message)
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: Activity_home.ProviderType) {
        val homeIntent = Intent(this, Activity_home::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            showHome(account.email ?: "", Activity_home.ProviderType.GOOGLE)
                        } else {
                            showAlert("Error al autenticar con Google.")
                        }
                    }
                }
            } catch (e: ApiException) {
                showAlert("Error al autenticar con Google: ${e.message}")
            }
        }
    }
}
