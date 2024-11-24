package com.jean.hotel

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RoomAdapter(
    private val rooms: List<Room>,
    private val onRoomClick: (Room) -> Unit // Callback para manejar clics
) : RecyclerView.Adapter<RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.render(room, holder.itemView.context)

        // Llamar a la actividad de formulario o mostrar alerta
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            if (room.reserved) {
                // Mostrar mensaje de alerta si la habitación está reservada
                android.widget.Toast.makeText(
                    context,
                    "Esta habitación no está disponible hasta ${room.reservedUntil}.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                // Navegar a la actividad de reserva si está disponible
                val intent = Intent(context, FormularioReservaActivity::class.java)
                intent.putExtra("roomPrice", room.price)
                intent.putExtra("roomId", room.id)
                intent.putExtra("roomPhoto", room.photo) // URL de la foto
                context.startActivity(intent)
            }
        }
    }


    override fun getItemCount(): Int = rooms.size
}
