package com.jean.hotel

<<<<<<< HEAD
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
=======
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
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
<<<<<<< HEAD
import com.google.firebase.firestore.FirebaseFirestore

class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleGoogleSignInResult(task)
            } else {
                showAlert(getString(R.string.error), getString(R.string.google_sign_in_error))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
=======

class AuthActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        val forgotPasswordText = findViewById<TextView>(R.id.forgotPasswordText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
<<<<<<< HEAD
        val googleButton = findViewById<ImageView>(R.id.googleButton)

        // Firebase Analytics
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de base de datos")
        analytics.logEvent("InitScreen", bundle)

        // Recuperar contraseña
=======
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integracion de base de datos")
        analytics.logEvent("InitScreen", bundle)

>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
        forgotPasswordText.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
<<<<<<< HEAD
                showAlert(
                    getString(R.string.error),
                    getString(R.string.forgot_password_empty)
                )
            }
        }

        // Google Sign-In
        googleButton.setOnClickListener {
            initiateGoogleSignIn()
        }

=======
                Toast.makeText(this, "Por favor, ingresa tu correo electrónico",
                    Toast.LENGTH_SHORT).show()
            }
        }

>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
        setup()
        session()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
<<<<<<< HEAD
                    showSuccessNotification(getString(R.string.password_reset_email_success, email))
                } else {
                    showCustomAlert(
                        getString(R.string.error),
                        getString(R.string.password_reset_email_failed)
                    )
=======
                    Toast.makeText(this, "Correo de recuperación enviado a $email",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al enviar el correo de recuperación",
                        Toast.LENGTH_SHORT).show()
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
                }
            }
    }

<<<<<<< HEAD
    private fun initiateGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleGoogleSignInResult(task: com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val currentUser = auth.currentUser
                            val email = currentUser?.email
                            val displayName = currentUser?.displayName
                            val photoUrl = currentUser?.photoUrl?.toString()

                            saveGoogleUserToFirestore(email, displayName, photoUrl)

                            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
                            prefs.putString("email", email)
                            prefs.putString("provider", ActivityHome.ProviderType.GOOGLE.name)
                            prefs.apply()

                            showHome(email ?: "", ActivityHome.ProviderType.GOOGLE)
                        } else {
                            showAlert(
                                getString(R.string.error),
                                getString(R.string.google_sign_in_failed)
                            )
                        }
                    }
            }
        } catch (e: ApiException) {
            showAlert(
                getString(R.string.error),
                getString(R.string.google_sign_in_error_message, e.message ?: "Desconocido")
            )
        }
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email != null && provider != null) {
            showHome(email, ActivityHome.ProviderType.valueOf(provider))
=======
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
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
        }
    }

    private fun setup() {
<<<<<<< HEAD
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        signUpButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString().trim()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString().trim()

            if (!isValidEmail(email)) {
                showAlert(
                    getString(R.string.error),
                    getString(R.string.invalid_email_format)
                )
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                showAlert(
                    getString(R.string.error),
                    getString(R.string.invalid_password_format)
                )
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showHome(task.result?.user?.email ?: "", ActivityHome.ProviderType.BASIC)
=======
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
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
                    } else {
                        val errorCode = (task.exception as FirebaseAuthException).errorCode
                        handleAuthError(errorCode)
                    }
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
<<<<<<< HEAD
        val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z]+\\.[a-zA-Z]{2,}$"
=======
        val emailPattern = "^[a-zA-Z0-9]+@[a-zA-Z]+\\.com$"
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
        return Regex(emailPattern).matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$"
        return Regex(passwordPattern).matches(password)
    }

    private fun handleAuthError(errorCode: String) {
        val message = when (errorCode) {
<<<<<<< HEAD
            "ERROR_INVALID_EMAIL" -> getString(R.string.error_invalid_email)
            "ERROR_EMAIL_ALREADY_IN_USE" -> getString(R.string.error_email_in_use)
            "ERROR_WRONG_PASSWORD" -> getString(R.string.error_wrong_password)
            "ERROR_USER_NOT_FOUND" -> getString(R.string.error_user_not_found)
            else -> getString(R.string.error_unknown)
        }
        showAlert(getString(R.string.error_auth), message)
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
=======
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
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
<<<<<<< HEAD
        dialog.findViewById<TextView>(android.R.id.message)?.textSize = 18f
    }

    private fun showHome(email: String, provider: ActivityHome.ProviderType) {
        val homeIntent = Intent(this, ActivityHome::class.java).apply {
=======
    }

    private fun showHome(email: String, provider: Activity_home.ProviderType) {
        val homeIntent = Intent(this, Activity_home::class.java).apply {
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
<<<<<<< HEAD
    }

    private fun showSuccessNotification(message: String) {
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(R.layout.success_notification_layout, null)

        val successMessage = view.findViewById<TextView>(R.id.successMessage)
        successMessage.text = message

        val toast = Toast(this)
        toast.duration = Toast.LENGTH_LONG
        toast.view = view
        toast.show()
    }

    private fun showCustomAlert(title: String, message: String, iconRes: Int = R.drawable.ic_warning) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
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

    private fun saveGoogleUserToFirestore(email: String?, displayName: String?, photoUrl: String?) {
        if (email == null) return

        val userCollection = FirebaseFirestore.getInstance().collection("Users")
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        val username = email.substringBefore("@")

        userCollection.whereEqualTo("correo", email).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val userData = hashMapOf(
                        "correo" to email,
                        "nombreCompleto" to displayName,
                        "imagenPerfil" to photoUrl,
                        "userID" to userID,
                        "usuario" to username
                    )
                    userCollection.add(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Usuario registrado en Firestore", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, getString(R.string.error_creating_user), Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.error_checking_user), Toast.LENGTH_SHORT).show()
            }
    }

}

=======

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
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
