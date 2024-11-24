package com.jean.hotel

<<<<<<< HEAD
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
=======
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
>>>>>>> 66ee247a4c2ba502c1bde67230779dfb463abbfe
