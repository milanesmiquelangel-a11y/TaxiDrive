package kz.taxidrive.app.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class CreateOfferPayload(
    val request_id: Long,
    val price_per_passenger_kzt: Int,
    val message: String? = null
)

data class DriverOfferDto(
    val id: Long,
    val request_id: Long,
    val driver_user_id: Long? = null,
    val driver_name: String? = null,
    val price_per_passenger_kzt: Int,
    val message: String? = null,
    val status: String,
    val created_at: String? = null
)

data class OfferListDto(
    val items: List<DriverOfferDto> = emptyList(),
    val count: Int = 0
)

data class AcceptOfferDto(
    val accepted: Boolean,
    val offer_id: Long,
    val request_id: Long
)

interface OfferApi {
    @POST("offers")
    fun createOffer(@Body body: CreateOfferPayload): Call<DriverOfferDto>

    @GET("requests/{request_id}/offers")
    fun getOffers(@Path("request_id") requestId: Long): Call<OfferListDto>

    @POST("offers/{offer_id}/accept")
    fun acceptOffer(@Path("offer_id") offerId: Long): Call<AcceptOfferDto>
}
