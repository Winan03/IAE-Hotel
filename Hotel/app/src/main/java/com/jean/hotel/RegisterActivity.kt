package com.jean.hotel

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private lateinit var alertHelper: AlertHelper

    private var selectedImageUri: Uri? = null
    private lateinit var profileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        alertHelper = AlertHelper(this) // Inicializa AlertHelper

        val registerButton = findViewById<Button>(R.id.registerButton)
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val selectImageButton = findViewById<Button>(R.id.selectImageButton)
        profileImageView = findViewById(R.id.profileImageView)

        // TextWatcher para formatear y validar los nombres en tiempo real
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                val filteredText = capitalizeWords(text)
                if (text != filteredText) {
                    nameEditText.removeTextChangedListener(this)
                    nameEditText.setText(filteredText)
                    nameEditText.setSelection(filteredText.length)
                    nameEditText.addTextChangedListener(this)
                }
            }
        })

        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                val filteredText = capitalizeWords(text)
                if (text != filteredText) {
                    usernameEditText.removeTextChangedListener(this)
                    usernameEditText.setText(filteredText)
                    usernameEditText.setSelection(filteredText.length)
                    usernameEditText.addTextChangedListener(this)
                }
            }
        })

        // Seleccionar imagen de la galería
        selectImageButton.setOnClickListener {
            openImagePicker()
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            when {
                name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                    alertHelper.showInfoAlert("Campos incompletos", "Por favor, completa todos los campos.")
                }
                !isValidName(name) -> {
                    alertHelper.showInfoAlert("Nombre inválido", "El nombre solo puede contener letras y espacios.")
                }
                !isValidName(username) -> {
                    alertHelper.showInfoAlert("Usuario inválido", "El nombre de usuario solo puede contener letras y espacios.")
                }
                !isValidEmail(email) -> {
                    alertHelper.showInfoAlert(
                        "Correo no válido",
                        "Por favor, utiliza un correo que termine en '@gmail.com', '@hotmail.com', '@yahoo.com' o '@outlook.com'."
                    )
                }
                !isValidPassword(password) -> {
                    alertHelper.showInfoAlert(
                        "Contraseña inválida",
                        "La contraseña debe contener al menos 6 caracteres, incluyendo letras y números."
                    )
                }
                selectedImageUri == null -> {
                    alertHelper.showInfoAlert("Imagen requerida", "Por favor selecciona una imagen de perfil.")
                }
                else -> {
                    registerUser(name, username, email, password)
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }

    private fun capitalizeWords(input: String): String {
        return input.replace(Regex("[^a-zA-Z\\s]"), "").split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

    private fun isValidName(input: String): Boolean {
        return input.matches(Regex("^[a-zA-Z\\s]+$"))
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[a-zA-Z0-9._%+-]+@(gmail\\.com|hotmail\\.com|yahoo\\.com|outlook\\.com)$"
        return Regex(emailPattern).matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$"
        return Regex(passwordPattern).matches(password)
    }

    private fun handleFirebaseAuthError(exception: Exception?) {
        val errorCode = (exception as? FirebaseAuthException)?.errorCode
        alertHelper.showErrorAlert(errorCode, "Error desconocido: ${exception?.localizedMessage}")
        Log.e("RegisterActivity", "Error de autenticación: ${exception?.localizedMessage}")
    }

    private fun saveImageLocally(userId: String, name: String, username: String, email: String) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
            val file = File(filesDir, "$userId-profile.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()

            saveUserToFirestore(userId, name, username, email, file.absolutePath)
        } catch (e: IOException) {
            Log.e("RegisterActivity", "Error al guardar la imagen", e)
            alertHelper.showCustomAlert("Error", "Error al guardar la imagen localmente.")
        }
    }

    private fun saveUserToFirestore(userId: String, name: String, username: String, email: String, imagePath: String) {
        val user = User(userId, name, username, email, imagePath)
        db.collection("Users").document(userId).set(user)
            .addOnSuccessListener {
                saveUserLocally(user)
                alertHelper.showSuccessNotification("¡Registro exitoso! Bienvenido, $name")
                redirectToMainPage()
            }
            .addOnFailureListener { e ->
                alertHelper.showErrorAlert(null, "Error al guardar datos en Firestore: ${e.localizedMessage}")
                Log.e("RegisterActivity", "Error al guardar en Firestore", e)
            }
    }

    private fun saveUserLocally(user: User) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userId", user.userId)
        editor.putString("name", user.name)
        editor.putString("username", user.username)
        editor.putString("email", user.email)
        editor.putString("imagePath", user.imagePath)
        editor.apply()
        Log.d("RegisterActivity", "Datos del usuario guardados localmente.")
    }

    private fun redirectToMainPage() {
        val intent = Intent(this, ActivityHome::class.java)
        startActivity(intent)
        finish()
    }

    private fun registerUser(name: String, username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: ""
                    saveCredentialsEncrypted(email, password)
                    saveImageLocally(userId, name, username, email)
                } else {
                    handleFirebaseAuthError(task.exception)
                }
            }
    }

    private fun saveCredentialsEncrypted(email: String, password: String) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "SecureUserPrefs",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val editor = sharedPreferences.edit()
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
        showToast("Credenciales guardadas de forma segura")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

