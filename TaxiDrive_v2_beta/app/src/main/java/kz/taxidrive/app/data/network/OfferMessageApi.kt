package kz.taxidrive.app.data.network
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
data class OfferMessageDto(val id: Long, val sender_user_id: Long, val message: String, val created_at: String? = null)
data class OfferMessageListDto(val items: List<OfferMessageDto> = emptyList(), val count: Int = 0)
data class SendOfferMessagePayload(val message: String)
interface OfferMessageApi {
 @GET("offers/{offer_id}/messages") fun list(@Path("offer_id") offerId: Long): Call<OfferMessageListDto>
 @POST("offers/{offer_id}/messages") fun send(@Path("offer_id") offerId: Long, @Body body: SendOfferMessagePayload): Call<OfferMessageDto>
}