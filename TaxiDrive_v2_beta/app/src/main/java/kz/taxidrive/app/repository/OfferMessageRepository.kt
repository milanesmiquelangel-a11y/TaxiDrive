package kz.taxidrive.app.repository
import android.content.Context
import kz.taxidrive.app.data.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
object OfferMessageRepository {
 suspend fun list(context: Context, offerId: Long): List<OfferMessageDto> = withContext(Dispatchers.IO) {
  ApiClient(context.applicationContext).create(OfferMessageApi::class.java).list(offerId).execute().requireBody().items
 }
 suspend fun send(context: Context, offerId: Long, message: String): OfferMessageDto = withContext(Dispatchers.IO) {
  ApiClient(context.applicationContext).create(OfferMessageApi::class.java).send(offerId, SendOfferMessagePayload(message.trim())).execute().requireBody()
 }
 private fun <T> Response<T>.requireBody(): T {
  if (isSuccessful) return body() ?: error("El servidor devolvió una respuesta vacía")
  val raw=errorBody()?.string().orEmpty()
  val detail=runCatching { JSONObject(raw).optString("detail") }.getOrNull()
  throw IllegalStateException(detail?.takeIf { it.isNotBlank() } ?: "Error del servidor (${code()}).")
 }
}