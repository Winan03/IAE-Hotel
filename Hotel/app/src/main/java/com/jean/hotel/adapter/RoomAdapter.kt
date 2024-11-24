package com.jean.hotel

<<<<<<< HEAD
import android.content.Intent
=======
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RoomAdapter(
<<<<<<< HEAD
    private val rooms: List<Room>,
    private val onRoomClick: (Room) -> Unit // Callback para manejar clics
=======
    private val roomList: List<Room>,
    private val clickListener: (Room) -> Unit
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
) : RecyclerView.Adapter<RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
<<<<<<< HEAD
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
=======
        val item = roomList[position]
        holder.render(item, holder.itemView.context)
        holder.itemView.setOnClickListener {
            if (!item.reserved) {
                clickListener(item) // Llama al listener para manejar la reserva
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
            }
        }
    }

<<<<<<< HEAD

    override fun getItemCount(): Int = rooms.size
=======
    override fun getItemCount(): Int = roomList.size
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
}
