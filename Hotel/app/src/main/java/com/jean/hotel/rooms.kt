package com.jean.hotel

data class roomsResponse(
    val suite: Suite,
    val rooms: List<Room>
)

data class Suite (
    val descriptionLabel: String,
    val price: Int,
    val photo: String,
    val title: String,
    val reserved: Boolean,
    val id: Int
)

data class Room(
    val price: Int,
    val photo: String,
    val reserved: Boolean,
    val id: Int
)

data class infoRoomReserved(
    val id: Int,
    val price: Int,
    val characteristics: List<String>,
    val photo: String,
    val reserved: Boolean
)