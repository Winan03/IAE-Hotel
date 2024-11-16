package com.jean.hotel

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface Endpoints {
    @GET("/TempestDemiGod/demo/db")
    suspend fun getDataRooms(): Response<roomsResponse>
}

interface EndpointsRoom {
    @GET("/TempestDemiGod/demo/{id}")
    suspend fun getDataRoomReserved(@Path("id") roomId: Int): Response<infoRoomReserved>
}

interface EndpointsStatusRoom {
    @PATCH("/TempestDemiGod/demo/rooms/{id}")
    suspend fun updateRoomStatus(@Path("id") roomId: Int, @Body statusUpdate: RoomStatusUpdate): Response<Void>
}

interface NavigationRoom {
    fun getRoomInfo()
}
