package com.jean.hotel

import java.util.Date

data class roomsResponse(
    var suite: Suite = Suite(),
    var rooms: List<Room> = emptyList()
)

data class Suite(
    var descriptionLabel: String = "",
    var price: Int = 0,
    var photo: String = "",
    var title: String = "",
    var reserved: Boolean = false,
    var id: Int = 0
)

data class Room(
    var id: Int = 0,
    var photo: String = "",
    var price: Double = 0.0,
    var reserved: Boolean = false,
    var reservedBy: String? = null,
    var reservedUntil: com.google.firebase.Timestamp? = null
)


data class infoRoomReserved(
    var id: Int = 0,
    var price: Int = 0,
    var characteristics: List<String> = emptyList(),
    var photo: String = "",
    var reserved: Boolean = false
)
