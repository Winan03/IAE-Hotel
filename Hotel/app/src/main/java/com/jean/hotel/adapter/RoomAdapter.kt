package com.jean.hotel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RoomAdapter(
    private val roomList: List<Room>,
    private val clickListener: (Room) -> Unit
) : RecyclerView.Adapter<RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val item = roomList[position]
        holder.render(item, holder.itemView.context)
        holder.itemView.setOnClickListener {
            if (!item.reserved) {
                clickListener(item) // Llama al listener para manejar la reserva
            }
        }
    }

    override fun getItemCount(): Int = roomList.size
}
