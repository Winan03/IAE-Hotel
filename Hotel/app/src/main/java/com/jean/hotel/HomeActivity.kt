package com.jean.hotel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class Activity_home : AppCompatActivity(), NavigationRoom {
    private lateinit var rvRooms: RecyclerView
    private lateinit var adapterRoom: RoomAdapter
    private var listRooms = mutableListOf<Room>()
    private lateinit var suiteInfo: Suite

    enum class ProviderType{
        BASIC,
        GOOGLE
    }

    companion object {
        const val RESERVA_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

        initRoomRecyclerView()
        loadRoomData()
    }

    private fun setup(email: String, provider: String) {
        title = "Inicio"
        val emailTextView = findViewById<TextView>(R.id.emailtextView)
        val providerTextView = findViewById<TextView>(R.id.providertextView)
        emailTextView.text = email
        providerTextView.text = provider

        val logOutButton = findViewById<Button>(R.id.logOutButton)
        logOutButton.setOnClickListener {
            val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    private fun initRoomRecyclerView() {
        rvRooms = findViewById(R.id.rvRooms)
        adapterRoom = RoomAdapter(listRooms) { room ->
            val intent = Intent(this, ReservaActivity::class.java)
            intent.putExtra("roomId", room.id)
            startActivityForResult(intent, RESERVA_REQUEST_CODE)
        }
        rvRooms.layoutManager = GridLayoutManager(this, 2,
            LinearLayoutManager.VERTICAL, false)
        rvRooms.adapter = adapterRoom
    }

    private fun loadRoomData() {
        GlobalScope.launch(Dispatchers.IO) {
            val service: Endpoints = Connection.ResponseEngine().create(Endpoints::class.java)
            val response: Response<roomsResponse> = service.getDataRooms()
            runOnUiThread {
                if (response.isSuccessful) {
                    suiteInfo = response.body()!!.suite
                    listRooms.addAll(response.body()!!.rooms)
                    adapterRoom.notifyDataSetChanged()
                    initSuiteCard()
                } else {
                    Toast.makeText(this@Activity_home, "Error al cargar datos",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initSuiteCard() {
        val infoSuite = findViewById<TextView>(R.id.infoSuite)
        val titleSuite = findViewById<TextView>(R.id.titleSuite)
        val priceSuite = findViewById<TextView>(R.id.priceSuite)
        val ivSuite = findViewById<ImageView>(R.id.ivSuite)

        infoSuite.text = suiteInfo.descriptionLabel
        titleSuite.text = suiteInfo.title
        priceSuite.text = "$ ${suiteInfo.price} - Day"

        Glide.with(ivSuite.context)
            .load(suiteInfo.photo)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(ivSuite)

        ivSuite.setOnClickListener {
            Toast.makeText(this, suiteInfo.title, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESERVA_REQUEST_CODE && resultCode == RESULT_OK) {
            val roomId = data?.getIntExtra("roomId", -1)
            if (roomId != null && roomId != -1) {
                // Encuentra el índice de la habitación en la lista
                val index = listRooms.indexOfFirst { it.id == roomId }

                if (index != -1) {
                    // Crea una copia de la habitación con 'reserved = true' y reemplázala en la lista
                    listRooms[index] = listRooms[index].copy(reserved = true)
                    adapterRoom.notifyDataSetChanged()
                }

                // Envía la actualización al servidor
                GlobalScope.launch(Dispatchers.IO) {
                    val service: EndpointsStatusRoom = Connection.ResponseEngine().
                    create(EndpointsStatusRoom::class.java)
                    val response = service.updateRoomStatus(roomId, RoomStatusUpdate(reserved = true))

                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@Activity_home, "Reserva actualizada en el servidor",
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@Activity_home,
                                "Error al actualizar la reserva en el servidor", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }


    override fun getRoomInfo() {
        val intent = Intent(this, RoomActivity::class.java)
        startActivity(intent)
    }
}





