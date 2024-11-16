package com.jean.hotel


import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


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
        Glide.with(photoRoom.context)
            .load(roomModel.photo)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
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