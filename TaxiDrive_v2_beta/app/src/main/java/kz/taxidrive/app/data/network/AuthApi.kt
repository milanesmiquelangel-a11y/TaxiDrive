package kz.taxidrive.app.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class AuthCredentials(
    val phone: String,
    val password: String
)

data class RegisterPayload(
    val phone: String,
    val full_name: String,
    val password: String,
    val role: String
)

data class AuthUserDto(
    val id: Long,
    val phone: String,
    val full_name: String,
    val role: String
)

data class AuthResponseDto(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val user: AuthUserDto,
    val driver_status: String? = null
)

data class DriverSessionDto(
    val approval_status: String? = null,
    val approved_at: String? = null,
    val free_trial_ends_at: String? = null
)

data class CurrentUserDto(
    val id: Long,
    val phone: String,
    val full_name: String,
    val role: String,
    val driver: DriverSessionDto? = null
)

interface AuthApi {
    @POST("auth/login")
    fun login(@Body body: AuthCredentials): Call<AuthResponseDto>

    @POST("auth/register")
    fun register(@Body body: RegisterPayload): Call<AuthResponseDto>

    @GET("auth/me")
    fun me(): Call<CurrentUserDto>
}
