package kz.taxidrive.app.repository

import android.content.Context
import android.net.Uri
import kz.taxidrive.app.data.network.ApiClient
import kz.taxidrive.app.data.network.DriverApi
import kz.taxidrive.app.data.network.DriverProfileDto
import kz.taxidrive.app.data.network.DriverProfilePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response

object DriverRepository {
    suspend fun getProfile(context: Context): DriverProfileDto =
        withContext(Dispatchers.IO) {
            ApiClient(context.applicationContext).create(DriverApi::class.java)
                .getProfile().execute().requireBody()
        }

    suspend fun uploadDocument(
        context: Context,
        documentType: String,
        uri: Uri
    ): String = withContext(Dispatchers.IO) {
        require(documentType == "front" || documentType == "steering_wheel")
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: error("No se pudo identificar el formato de la imagen")
        require(mime in listOf("image/jpeg", "image/png", "image/webp")) {
            "Usa una imagen JPG, PNG o WEBP"
        }
        val bytes = resolver.openInputStream(uri)?.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                require(total <= 5 * 1024 * 1024) { "La imagen supera el límite de 5 MB" }
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        } ?: error("No se pudo leer la imagen")
        require(bytes.isNotEmpty()) { "La imagen está vacía" }
        val body = bytes.toRequestBody(mime.toMediaType())
        val part = MultipartBody.Part.createFormData("file", "driver_document", body)
        ApiClient(context.applicationContext).create(DriverApi::class.java)
            .uploadDocument(documentType, part).execute().requireBody().document_id
    }

    suspend fun submitProfile(
        context: Context,
        make: String,
        model: String,
        year: Int,
        plate: String,
        frontId: String,
        steeringId: String
    ) = withContext(Dispatchers.IO) {
        ApiClient(context.applicationContext).create(DriverApi::class.java)
            .submitProfile(
                DriverProfilePayload(make.trim(), model.trim(), year, plate.trim(), frontId, steeringId)
            ).execute().requireBody()
    }

    private fun <T> Response<T>.requireBody(): T {
        if (isSuccessful) return body() ?: error("El servidor devolvió una respuesta vacía")
        val raw = errorBody()?.string().orEmpty()
        val detail = runCatching { JSONObject(raw).optString("detail") }.getOrNull()
        throw IllegalStateException(detail?.takeIf { it.isNotBlank() } ?: "Error del servidor (${code()})")
    }
}
