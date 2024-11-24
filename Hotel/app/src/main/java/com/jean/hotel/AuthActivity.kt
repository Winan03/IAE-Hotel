package com.jean.hotel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        val forgotPasswordText = findViewById<TextView>(R.id.forgotPasswordText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val googleButton = findViewById<ImageView>(R.id.googleButton)

        // Firebase Analytics
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de base de datos")
        analytics.logEvent("InitScreen", bundle)

        // Recuperar contraseña
        forgotPasswordText.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
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

        setup()
        session()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showSuccessNotification(getString(R.string.password_reset_email_success, email))
                } else {
                    showCustomAlert(
                        getString(R.string.error),
                        getString(R.string.password_reset_email_failed)
                    )
                }
            }
    }

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
        }
    }

    private fun setup() {
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
                    } else {
                        val errorCode = (task.exception as FirebaseAuthException).errorCode
                        handleAuthError(errorCode)
                    }
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z]+\\.[a-zA-Z]{2,}$"
        return Regex(emailPattern).matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$"
        return Regex(passwordPattern).matches(password)
    }

    private fun handleAuthError(errorCode: String) {
        val message = when (errorCode) {
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
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
        dialog.findViewById<TextView>(android.R.id.message)?.textSize = 18f
    }

    private fun showHome(email: String, provider: ActivityHome.ProviderType) {
        val homeIntent = Intent(this, ActivityHome::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
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

