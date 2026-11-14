package kz.taxidrive.app.auth

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object AuthRepository {
    private const val BASE_URL = "https://cloud-001.tail3baa0f.ts.net"
    private const val PREFS = "taxidrive_auth"
    private const val TOKEN_KEY = "access_token"

    fun login(
        context: Context,
        phone: String,
        password: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        post(context, "/auth/login", JSONObject()
            .put("phone", phone.trim())
            .put("password", password), onSuccess, onError)
    }

    fun register(
        context: Context,
        phone: String,
        fullName: String,
        password: String,
        role: String,
        privacyAccepted: Boolean,
        driverDeclarationAccepted: Boolean,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        post(context, "/auth/register", JSONObject()
            .put("phone", phone.trim())
            .put("full_name", fullName.trim())
            .put("password", password)
            .put("role", role)
            .put("privacy_accepted", privacyAccepted)
            .put("driver_declaration_accepted", driverDeclarationAccepted),
            onSuccess, onError)
    }

    fun accessToken(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(TOKEN_KEY, null)

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(TOKEN_KEY).apply()
    }

    fun getMe(
        context: Context,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                val token = accessToken(context)
                    ?: throw IllegalStateException("No hay una sesión activa")
                connection = (URL(BASE_URL + "/auth/me").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Authorization", "Bearer $token")
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
                val response = if (responseText.isNotBlank()) JSONObject(responseText) else JSONObject()
                if (code in 200..299) {
                    android.os.Handler(context.mainLooper).post { onSuccess(response) }
                } else {
                    if (code == 401 || code == 403) clearSession(context)
                    android.os.Handler(context.mainLooper).post {
                        onError("No se pudo verificar la sesión (HTTP $code). Vuelva a iniciar sesión.")
                    }
                }
            } catch (e: Exception) {
                android.os.Handler(context.mainLooper).post {
                    onError("No se pudo verificar la sesión. Compruebe su conexión e inténtelo de nuevo.")
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }


    fun uploadDriverDocument(
        context: Context,
        documentType: String,
        uri: android.net.Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                val token = accessToken(context) ?: throw IllegalStateException("No hay una sesión activa")
                val mime = context.contentResolver.getType(uri)?.lowercase()
                    ?: throw IllegalArgumentException("No se pudo identificar el formato de la imagen")
                if (mime !in setOf("image/jpeg", "image/png", "image/webp")) {
                    throw IllegalArgumentException("Use una imagen JPG, PNG o WEBP")
                }
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalArgumentException("No se pudo leer la imagen")
                if (bytes.isEmpty() || bytes.size > 5 * 1024 * 1024) {
                    throw IllegalArgumentException("La imagen debe pesar como máximo 5 MB")
                }
                val extension = when (mime) {
                    "image/jpeg" -> "jpg"
                    "image/png" -> "png"
                    else -> "webp"
                }
                val boundary = "TaxiDriveBoundary" + System.currentTimeMillis()
                connection = (URL(BASE_URL + "/drivers/me/documents/" + documentType).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 20000
                    readTimeout = 20000
                    doOutput = true
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                    setRequestProperty("Accept", "application/json")
                }
                connection.outputStream.use { output ->
                    output.write("--$boundary\\r\\n".toByteArray(StandardCharsets.UTF_8))
                    output.write("Content-Disposition: form-data; name=\\"file\\"; filename=\\"document.$extension\\"\\r\\n".toByteArray(StandardCharsets.UTF_8))
                    output.write("Content-Type: $mime\\r\\n\\r\\n".toByteArray(StandardCharsets.UTF_8))
                    output.write(bytes)
                    output.write("\\r\\n--$boundary--\\r\\n".toByteArray(StandardCharsets.UTF_8))
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
                val response = if (responseText.isNotBlank()) JSONObject(responseText) else JSONObject()
                if (code in 200..299) {
                    val id = response.optString("document_id")
                    if (id.isBlank()) throw IllegalStateException("El servidor no confirmó el documento")
                    android.os.Handler(context.mainLooper).post { onSuccess(id) }
                } else {
                    val detail = response.optString("detail", "No se pudo subir la imagen (HTTP $code)")
                    android.os.Handler(context.mainLooper).post { onError(detail) }
                }
            } catch (e: Exception) {
                android.os.Handler(context.mainLooper).post {
                    onError(e.message ?: "No se pudo subir la imagen. Inténtelo de nuevo.")
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    fun submitDriverProfile(
        context: Context,
        vehicleMake: String,
        vehicleModel: String,
        vehicleYear: Int,
        licensePlate: String,
        frontPhotoId: String,
        steeringWheelPhotoId: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val body = JSONObject()
            .put("vehicle_make", vehicleMake.trim())
            .put("vehicle_model", vehicleModel.trim())
            .put("vehicle_year", vehicleYear)
            .put("license_plate", licensePlate.trim())
            .put("front_photo_url", frontPhotoId)
            .put("steering_wheel_photo_url", steeringWheelPhotoId)
        put(context, "/drivers/me", body, onSuccess, onError)
    }

    private fun put(
        context: Context,
        path: String,
        body: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                val token = accessToken(context) ?: throw IllegalStateException("No hay una sesión activa")
                connection = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    connectTimeout = 15000
                    readTimeout = 15000
                    doOutput = true
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }
                connection.outputStream.use {
                    it.write(body.toString().toByteArray(StandardCharsets.UTF_8))
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
                val response = if (responseText.isNotBlank()) JSONObject(responseText) else JSONObject()
                if (code in 200..299) {
                    android.os.Handler(context.mainLooper).post { onSuccess(response) }
                } else {
                    val detail = response.optString("detail", "No se pudo guardar el perfil (HTTP $code)")
                    android.os.Handler(context.mainLooper).post { onError(detail) }
                }
            } catch (e: Exception) {
                android.os.Handler(context.mainLooper).post {
                    onError(e.message ?: "No se pudo conectar con TaxiDrive.")
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }

    private fun post(
        context: Context,
        path: String,
        body: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 15000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }
                connection.outputStream.use {
                    it.write(body.toString().toByteArray(StandardCharsets.UTF_8))
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
                val response = if (responseText.isNotBlank()) JSONObject(responseText) else JSONObject()
                if (code in 200..299) {
                    val token = response.optString("access_token")
                    if (token.isNotBlank()) {
                        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                            .edit().putString(TOKEN_KEY, token).apply()
                    }
                    android.os.Handler(context.mainLooper).post { onSuccess(response) }
                } else {
                    val detail = response.opt("detail")
                    val message = when (detail) {
                        is String -> detail
                        is org.json.JSONArray -> detail.toString()
                        else -> "No se pudo completar la operación (HTTP $code)"
                    }
                    android.os.Handler(context.mainLooper).post { onError(message) }
                }
            } catch (e: Exception) {
                android.os.Handler(context.mainLooper).post {
                    onError("No se pudo conectar con TaxiDrive. Compruebe su conexión e inténtelo de nuevo.")
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }
}
