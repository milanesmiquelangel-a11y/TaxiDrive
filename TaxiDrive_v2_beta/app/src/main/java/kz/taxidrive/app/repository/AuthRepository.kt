package kz.taxidrive.app.repository

import android.content.Context
import kz.taxidrive.app.data.network.ApiClient
import kz.taxidrive.app.data.network.AuthApi
import kz.taxidrive.app.data.network.AuthCredentials
import kz.taxidrive.app.data.network.AuthUserDto
import kz.taxidrive.app.data.network.CurrentUserDto
import kz.taxidrive.app.data.network.RegisterPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

data class AuthSession(
    val id: Long,
    val phone: String,
    val fullName: String,
    val role: String,
    val driverApprovalStatus: String? = null,
    val freeTrialEndsAt: String? = null
)

object AuthRepository {
    suspend fun login(context: Context, phone: String, password: String): AuthSession =
        withContext(Dispatchers.IO) {
            val client = ApiClient(context.applicationContext)
            val response = client.create(AuthApi::class.java)
                .login(AuthCredentials(phone = phone.trim(), password = password)).execute()
            val body = response.requireBody()
            client.saveSessionToken(body.access_token)
            body.user.toSession(body.driver_status)
        }

    suspend fun register(
        context: Context,
        phone: String,
        fullName: String,
        password: String,
        role: String
    ): AuthSession = withContext(Dispatchers.IO) {
        val client = ApiClient(context.applicationContext)
        val response = client.create(AuthApi::class.java).register(
            RegisterPayload(phone.trim(), fullName.trim(), password, role)
        ).execute()
        val body = response.requireBody()
        client.saveSessionToken(body.access_token)
        body.user.toSession(body.driver_status)
    }

    suspend fun restoreSession(context: Context): AuthSession? = withContext(Dispatchers.IO) {
        val client = ApiClient(context.applicationContext)
        if (client.readSessionToken().isNullOrBlank()) return@withContext null
        val response = client.create(AuthApi::class.java).me().execute()
        if (response.code() == 401 || response.code() == 403) {
            client.clearSession()
            return@withContext null
        }
        response.requireBody().toSession()
    }

    fun logout(context: Context) {
        ApiClient(context.applicationContext).clearSession()
    }

    private fun AuthUserDto.toSession(driverStatus: String? = null) =
        AuthSession(id, phone, full_name, role, driverStatus)

    private fun CurrentUserDto.toSession() =
        AuthSession(id, phone, full_name, role, driver?.approval_status, driver?.free_trial_ends_at)

    private fun <T> Response<T>.requireBody(): T {
        if (isSuccessful) return body() ?: error("El servidor devolvió una respuesta vacía")
        val raw = errorBody()?.string().orEmpty()
        val detail = runCatching { JSONObject(raw).optString("detail") }.getOrNull()
        throw IllegalStateException(
            detail?.takeIf { it.isNotBlank() } ?: when (code()) {
                409 -> "El teléfono ya está registrado."
                422 -> "Comprueba el teléfono y los datos introducidos."
                429 -> "Demasiados intentos. Espera 15 minutos antes de volver a intentarlo."
                else -> "Error del servidor (${code()}). Inténtalo de nuevo."
            }
        )
    }
}
