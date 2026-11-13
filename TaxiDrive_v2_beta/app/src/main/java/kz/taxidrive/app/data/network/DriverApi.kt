package kz.taxidrive.app.data.network

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

data class DriverDocumentResponse(
    val document_type: String,
    val uploaded: Boolean,
    val document_id: String
)

data class DriverProfilePayload(
    val vehicle_make: String,
    val vehicle_model: String,
    val vehicle_year: Int,
    val license_plate: String,
    val front_photo_url: String,
    val steering_wheel_photo_url: String
)

data class DriverProfileDto(
    val vehicle_make: String? = null,
    val vehicle_model: String? = null,
    val vehicle_year: Int? = null,
    val license_plate: String? = null,
    val front_photo_url: String? = null,
    val steering_wheel_photo_url: String? = null,
    val approval_status: String? = null,
    val rejection_reason: String? = null
)

data class DriverProfileSubmitResponse(
    val status: String,
    val message: String
)

interface DriverApi {
    @GET("drivers/me")
    fun getProfile(): Call<DriverProfileDto>

    @PUT("drivers/me")
    fun submitProfile(@Body body: DriverProfilePayload): Call<DriverProfileSubmitResponse>

    @Multipart
    @POST("drivers/me/documents/{document_type}")
    fun uploadDocument(
        @retrofit2.http.Path("document_type") documentType: String,
        @Part file: MultipartBody.Part
    ): Call<DriverDocumentResponse>
}
