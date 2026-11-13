package kz.taxidrive.app.backend

import com.google.gson.annotations.SerializedName

/** Request DTOs mirror the live FastAPI OpenAPI contract. */
data class RegisterRequest(
    val phone: String,
    @SerializedName("full_name") val fullName: String,
    val password: String,
    val role: String // passenger | driver
)

data class LoginRequest(val phone: String, val password: String)

data class PassengerRequestBody(
    @SerializedName("origin_text") val originText: String,
    @SerializedName("destination_text") val destinationText: String,
    @SerializedName("departure_at") val departureAt: String,
    @SerializedName("passenger_count") val passengerCount: Int = 1,
    val comment: String? = null
)

data class DriverOfferBody(
    @SerializedName("request_id") val requestId: Long,
    @SerializedName("price_per_passenger_kzt") val pricePerPassengerKzt: Long,
    @SerializedName("available_seats") val availableSeats: Int = 1,
    val message: String? = null
)

data class OfferMessageBody(val message: String)
