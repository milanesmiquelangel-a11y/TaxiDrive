package kz.taxidrive.app.repository

import android.content.Context
import kz.taxidrive.app.data.network.ApiClient
import kz.taxidrive.app.data.network.CreateRideRequestPayload
import kz.taxidrive.app.data.network.RequestApi
import kz.taxidrive.app.data.network.RideRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

object RequestRepository {
    suspend fun create(
        context: Context,
        origin: String,
        destination: String,
        departureAt: String,
        passengerCount: Int,
        comment: String?
    ): RideRequestDto = withContext(Dispatchers.IO) {
        val api = ApiClient(context.applicationContext).create(RequestApi::class.java)
        api.createRequest(
            CreateRideRequestPayload(
                origin_text = origin.trim(),
                destination_text = destination.trim(),
                departure_at = departureAt,
                passenger_count = passengerCount,
                comment = comment?.trim()?.takeIf { it.isNotEmpty() }
            )
        ).execute().requireBody()
    }

    suspend fun list(context: Context): List<RideRequestDto> = withContext(Dispatchers.IO) {
        ApiClient(context.applicationContext).create(RequestApi::class.java)
            .getRequests().execute().requireBody().items
    }

    private fun <T> Response<T>.requireBody(): T {
        if (isSuccessful) return body() ?: error("El servidor devolvió una respuesta vacía")
        val raw = errorBody()?.string().orEmpty()
        val detail = runCatching { JSONObject(raw).optString("detail") }.getOrNull()
        val message = detail?.takeIf { it.isNotBlank() }
            ?: "No se pudo completar la operación (" + code() + ")."
        throw IllegalStateException(message)
    }
}
