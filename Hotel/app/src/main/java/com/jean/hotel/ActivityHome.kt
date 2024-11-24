package com.jean.hotel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class ActivityHome : AppCompatActivity(), NavigationRoom {
    private lateinit var rvRooms: RecyclerView
    private lateinit var adapterRoom: RoomAdapter
    private var listRooms = mutableListOf<Room>()
    private val firestore = FirebaseFirestore.getInstance() // Instancia de Firestore

enum class ProviderType{
    BASIC,
    GOOGLE
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Inicializar Firebase
        setContentView(R.layout.activity_main)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        val displayName = bundle?.getString("displayName")
        val photoUrl = bundle?.getString("photoUrl")

        setup(email ?: "", provider ?: "", displayName, photoUrl)
        initRoomRecyclerView()
        loadRoomDataFromFirestore() // Carga datos desde Firestore
    }

    private fun setup(email: String, provider: String, displayName: String?, photoUrl: String?) {
        val usuarioTextView = findViewById<TextView>(R.id.usuarioextView)
        val providerTextView = findViewById<TextView>(R.id.providertextView)
        val profileImageView = findViewById<ImageView>(R.id.profileImageView)
        val loadingIndicator = findViewById<ProgressBar>(R.id.loadingIndicator) // ProgressBar

        providerTextView.text = provider
        loadingIndicator.visibility = View.VISIBLE // Mostrar indicador de carga

        // Recuperar datos del usuario desde SharedPreferences
        val sharedPrefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val savedName = sharedPrefs.getString("userName", null)
        val savedPhoto = sharedPrefs.getString("userPhoto", null)

        if (!savedName.isNullOrEmpty() && !savedPhoto.isNullOrEmpty()) {
            // Usar datos guardados localmente
            usuarioTextView.text = savedName
            Glide.with(this)
                .load(savedPhoto)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(profileImageView)
            loadingIndicator.visibility = View.GONE // Ocultar indicador de carga
        } else {
            // Consultar Firestore si no hay datos locales
            val userDoc = firestore.collection("Users")
                .whereEqualTo("correo", email)
                .limit(1)

            userDoc.get().addOnSuccessListener { documents ->
                loadingIndicator.visibility = View.GONE // Ocultar indicador de carga

                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val nombreUsuario = document.getString("nombreCompleto") ?: "Usuario"
                    val imageUrl = document.getString("imagenPerfil") ?: ""

                    usuarioTextView.text = nombreUsuario
                    if (imageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_avatar_placeholder)
                            .circleCrop()
                            .into(profileImageView)
                    }

                    // Guardar datos en SharedPreferences
                    val editor = sharedPrefs.edit()
                    editor.putString("userName", nombreUsuario)
                    editor.putString("userPhoto", imageUrl)
                    editor.apply()
                } else {
                    // Guardar usuario si no existe
                    saveNewGoogleUser(email, displayName, photoUrl) { updatedUser ->
                        val nombreUsuario = updatedUser?.get("nombreCompleto") as? String ?: "Usuario"
                        val imageUrl = updatedUser?.get("imagenPerfil") as? String ?: ""

                        usuarioTextView.text = nombreUsuario
                        if (imageUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_avatar_placeholder)
                                .circleCrop()
                                .into(profileImageView)
                        }

                        // Guardar datos en SharedPreferences
                        val editor = sharedPrefs.edit()
                        editor.putString("userName", nombreUsuario)
                        editor.putString("userPhoto", imageUrl)
                        editor.apply()
                    }
                }
            }.addOnFailureListener {
                loadingIndicator.visibility = View.GONE
                usuarioTextView.text = getString(R.string.error_loading_user_data)
                Toast.makeText(this, getString(R.string.error_loading_user_data), Toast.LENGTH_SHORT).show()
            }
        }

        // Botón de cerrar sesión
        val logOutButton = findViewById<Button>(R.id.logOutButton)
        logOutButton.setOnClickListener {
            val editor = sharedPrefs.edit()
            editor.clear() // Limpiar datos de SharedPreferences
            editor.apply()

            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    private fun saveNewGoogleUser(
        email: String?,
        displayName: String?,
        photoUrl: String?,
        callback: (Map<String, Any>?) -> Unit
    ) {
        if (email != null) {
            val userData = hashMapOf(
                "correo" to email,
                "nombreCompleto" to (displayName ?: "Nuevo Usuario"),
                "imagenPerfil" to (photoUrl ?: "")
            )

            firestore.collection("Users")
                .whereEqualTo("correo", email)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        firestore.collection("Users").add(userData)
                            .addOnSuccessListener { documentRef ->
                                documentRef.get().addOnSuccessListener { document ->
                                    callback(document.data)
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, getString(R.string.error_creating_user), Toast.LENGTH_SHORT).show()
                                callback(null)
                            }
                    } else {
                        callback(documents.documents[0].data)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.error_checking_user), Toast.LENGTH_SHORT).show()
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

    private fun initRoomRecyclerView() {
        rvRooms = findViewById(R.id.rvRooms)

        adapterRoom = RoomAdapter(listRooms) { room ->
            navigateToFormulario(room)
        }

        rvRooms.layoutManager = GridLayoutManager(this, 2) // Dos columnas
        rvRooms.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.set(16, 16, 16, 16) // Espaciado uniforme
            }
        })
        rvRooms.adapter = adapterRoom
    }

    private fun loadRoomDataFromFirestore() {
        firestore.collection("rooms")
            .get()
            .addOnSuccessListener { documents ->
                listRooms.clear() // Limpiar la lista antes de agregar nuevos datos
                for (document in documents) {
                    val room = document.toObject(Room::class.java)
                    room.id = document.id.toIntOrNull() ?: 0
                    listRooms.add(room)
                }
                adapterRoom.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar habitaciones desde Firestore", Toast.LENGTH_SHORT).show()
            }
    }


    private fun navigateToFormulario(room: Room) {
        firestore.collection("rooms").document(room.id.toString())
            .get()
            .addOnSuccessListener { document ->
                val isReserved = document.getBoolean("reserved") ?: false
                val reservedUntil = document.getTimestamp("reservedUntil")

                if (isReserved && reservedUntil != null && reservedUntil.toDate().after(Date())) {
                    val formattedDate = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", reservedUntil.toDate())
                    showCustomAlert(
                        getString(R.string.room_reserved),
                        "${getString(R.string.reserved_until)}: $formattedDate"
                    )
                } else {
                    val intent = Intent(this, FormularioReservaActivity::class.java)
                    intent.putExtra("roomId", room.id)
                    intent.putExtra("roomPrice", room.price)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                showCustomAlert(getString(R.string.error), getString(R.string.error_checking_room))
            }
    }

    override fun getRoomInfo() {
        val intent = Intent(this, RoomActivity::class.java)
        startActivity(intent)
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

    override fun onResume() {
        super.onResume()

        val roomManager = RoomManager(FirebaseFirestore.getInstance())
        roomManager.checkAndReleaseExpiredRooms { success, message ->
            if (success) {
                Toast.makeText(this, "Habitaciones liberadas automáticamente.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, message ?: "Error al procesar habitaciones.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}







