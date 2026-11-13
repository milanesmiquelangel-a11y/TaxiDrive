package kz.taxidrive.app.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class CreateRideRequestPayload(
    val origin_text: String,
    val destination_text: String,
    val departure_at: String,
    val passenger_count: Int,
    val comment: String? = null
)

data class RideRequestDto(
    val id: Long,
    val origin_text: String,
    val destination_text: String,
    val departure_at: String,
    val passenger_count: Int,
    val comment: String? = null,
    val status: String? = null,
    val created_at: String? = null
)

data class RideRequestListDto(
    val items: List<RideRequestDto> = emptyList(),
    val count: Int = 0
)

interface RequestApi {
    @POST("requests")
    fun createRequest(@Body body: CreateRideRequestPayload): Call<RideRequestDto>

    @GET("requests")
    fun getRequests(): Call<RideRequestListDto>
}
