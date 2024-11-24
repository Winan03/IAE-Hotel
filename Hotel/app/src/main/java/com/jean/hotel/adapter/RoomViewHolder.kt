package com.jean.hotel

<<<<<<< HEAD
import android.content.Context
=======

import android.content.Context
import android.content.Intent
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
<<<<<<< HEAD
=======
import android.widget.Toast
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

<<<<<<< HEAD
class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val priceRoom = view.findViewById<TextView>(R.id.priceCardRoom)
    private val photoRoom = view.findViewById<ImageView>(R.id.ivRoom)
    private val reservedRoom = view.findViewById<FrameLayout>(R.id.frReserved)

    fun render(roomModel: Room, context: Context) {
        // Mostrar u ocultar indicador de reserva
        reservedRoom.isVisible = roomModel.reserved

        // Establecer datos de la habitación
        priceRoom.text = "$ ${roomModel.price} - Day"

        // Mostrar información de reserva (opcional)
        if (roomModel.reserved && roomModel.reservedUntil != null) {
            priceRoom.text = "$ ${roomModel.price} - Reservada hasta ${roomModel.reservedUntil}"
        }

        // Cargar imagen usando Glide, con manejo de errores
=======

class RoomViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val priceRoom = view.findViewById<TextView>(R.id.priceCardRoom)
    val photoRoom = view.findViewById<ImageView>(R.id.ivRoom)
    val reservedRoom = view.findViewById<FrameLayout>(R.id.frReserved)

    fun render(roomModel: Room, context: Context) {
        if (roomModel.reserved) {
            reservedRoom.isVisible = true
        } else {
            reservedRoom.isVisible = false
        }

        priceRoom.text = "$ ${roomModel.price} - Day"
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
        Glide.with(photoRoom.context)
            .load(roomModel.photo)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
<<<<<<< HEAD
            .placeholder(R.drawable.ic_room_placeholder) // Imagen de reemplazo
            .error(R.drawable.ic_error_image)       // Imagen de error
            .into(photoRoom)
    }
}
=======
            .into(photoRoom)

        itemView.setOnClickListener {
            if (roomModel.reserved) {
                Toast.makeText(
                    photoRoom.context,
                    "La habitación seleccionada ya está reservada",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Iniciar el proceso de reserva
                val intent = Intent(context, ReservaActivity::class.java)
                intent.putExtra("roomId", roomModel.id)
                context.startActivity(intent)
            }
        }
    }
}
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
