package kz.taxidrive.app.backend

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit contract for the staged backend migration. No Firebase repository is replaced here. */
interface TaxiDriveApi {
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): Response<JsonElement>
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): Response<JsonObject>
    @GET("auth/me") suspend fun me(@Header("Authorization") bearer: String): Response<JsonObject>

    @GET("requests") suspend fun requests(@Header("Authorization") bearer: String): Response<JsonObject>
    @POST("requests") suspend fun createRequest(
        @Header("Authorization") bearer: String,
        @Body body: PassengerRequestBody
    ): Response<JsonObject>
    @GET("requests/{requestId}") suspend fun request(
        @Header("Authorization") bearer: String,
        @Path("requestId") requestId: Long
    ): Response<JsonObject>
    @GET("requests/{requestId}/offers") suspend fun offers(
        @Header("Authorization") bearer: String,
        @Path("requestId") requestId: Long
    ): Response<JsonObject>
    @POST("offers") suspend fun createOffer(
        @Header("Authorization") bearer: String,
        @Body body: DriverOfferBody
    ): Response<JsonObject>
    @POST("offers/{offerId}/accept") suspend fun acceptOffer(
        @Header("Authorization") bearer: String,
        @Path("offerId") offerId: Long
    ): Response<JsonObject>
    @GET("offers/{offerId}/messages") suspend fun messages(
        @Header("Authorization") bearer: String,
        @Path("offerId") offerId: Long
    ): Response<JsonObject>
    @POST("offers/{offerId}/messages") suspend fun sendMessage(
        @Header("Authorization") bearer: String,
        @Path("offerId") offerId: Long,
        @Body body: OfferMessageBody
    ): Response<JsonObject>
    @GET("notifications") suspend fun notifications(@Header("Authorization") bearer: String): Response<JsonObject>
    @POST("notifications/{notificationId}/read") suspend fun markNotificationRead(
        @Header("Authorization") bearer: String,
        @Path("notificationId") notificationId: Long
    ): Response<JsonObject>
    @GET("drivers/me/activation") suspend fun driverActivation(@Header("Authorization") bearer: String): Response<JsonObject>
    @GET("health") suspend fun health(): Response<JsonObject>
}
