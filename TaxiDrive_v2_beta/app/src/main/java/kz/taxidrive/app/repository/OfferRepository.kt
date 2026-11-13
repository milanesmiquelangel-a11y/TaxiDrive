package kz.taxidrive.app.repository

import android.content.Context
import kz.taxidrive.app.data.network.AcceptOfferDto
import kz.taxidrive.app.data.network.ApiClient
import kz.taxidrive.app.data.network.CreateOfferPayload
import kz.taxidrive.app.data.network.DriverOfferDto
import kz.taxidrive.app.data.network.OfferApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

object OfferRepository {
    suspend fun create(context: Context, requestId: Long, priceKzt: Int, message: String?): DriverOfferDto =
        withContext(Dispatchers.IO) {
            ApiClient(context.applicationContext).create(OfferApi::class.java)
                .createOffer(CreateOfferPayload(requestId, priceKzt, message?.trim()?.takeIf { it.isNotEmpty() }))
                .execute().requireBody()
        }

    suspend fun listForRequest(context: Context, requestId: Long): List<DriverOfferDto> =
        withContext(Dispatchers.IO) {
            ApiClient(context.applicationContext).create(OfferApi::class.java)
                .getOffers(requestId).execute().requireBody().items
        }

    suspend fun listMyOffers(context: Context): List<DriverOfferDto> =
        withContext(Dispatchers.IO) {
            ApiClient(context.applicationContext).create(OfferApi::class.java)
                .getMyOffers().execute().requireBody().items
        }

    suspend fun accept(context: Context, offerId: Long): AcceptOfferDto =
        withContext(Dispatchers.IO) {
            ApiClient(context.applicationContext).create(OfferApi::class.java)
                .acceptOffer(offerId).execute().requireBody()
        }

    private fun <T> Response<T>.requireBody(): T {
        if (isSuccessful) return body() ?: error("El servidor devolvió una respuesta vacía")
        val raw = errorBody()?.string().orEmpty()
        val detail = runCatching { JSONObject(raw).optString("detail") }.getOrNull()
        throw IllegalStateException(detail?.takeIf { it.isNotBlank() } ?: "Error del servidor (" + code() + ").")
    }
}
