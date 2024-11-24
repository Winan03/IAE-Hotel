package com.jean.hotel

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

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
        Glide.with(photoRoom.context)
            .load(roomModel.photo)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .placeholder(R.drawable.ic_room_placeholder) // Imagen de reemplazo
            .error(R.drawable.ic_error_image)       // Imagen de error
            .into(photoRoom)
    }
}
