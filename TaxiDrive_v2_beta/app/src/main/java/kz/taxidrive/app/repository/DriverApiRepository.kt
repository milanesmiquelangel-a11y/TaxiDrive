package kz.taxidrive.app.repository

import android.content.Context
import android.os.Handler
import org.json.JSONObject
import kz.taxidrive.app.auth.AuthRepository
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Authenticated driver operations against the TaxiDrive FastAPI backend.
 * This repository intentionally does not read or write Firebase.
 */
object DriverApiRepository {
    private const val BASE_URL = "https://cloud-001.tail3baa0f.ts.net"

    fun getActivationStatus(
        context: Context,
        onSuccess: (JSONObject) -> Unit,
        onFailure: (String) -> Unit
    ) = request(context, "GET", "/drivers/me/activation", null, onSuccess, onFailure)

    fun getPassengerRequests(
        context: Context,
        onSuccess: (JSONObject) -> Unit,
        onFailure: (String) -> Unit
    ) = request(context, "GET", "/requests", null, onSuccess, onFailure)

    fun createOffer(
        context: Context,
        requestId: Int,
        pricePerPassengerKzt: Int,
        message: String,
        onSuccess: (JSONObject) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val body = JSONObject()
            .put("request_id", requestId)
            .put("price_per_passenger_kzt", pricePerPassengerKzt)
            .put("message", message.takeIf { it.isNotBlank() })
        request(context, "POST", "/offers", body, onSuccess, onFailure)
    }

    private fun request(
        context: Context,
        method: String,
        path: String,
        body: JSONObject?,
        onSuccess: (JSONObject) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                val token = AuthRepository.accessToken(context)
                    ?: throw IllegalStateException("La sesión ha caducado. Inicie sesión nuevamente.")
                connection = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
                    requestMethod = method
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Accept", "application/json")
                    if (body != null) {
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    }
                }
                if (body != null) {
                    connection.outputStream.use {
                        it.write(body.toString().toByteArray(StandardCharsets.UTF_8))
                    }
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
                val response = if (responseText.isNotBlank()) JSONObject(responseText) else JSONObject()
                if (code in 200..299) {
                    Handler(context.mainLooper).post { onSuccess(response) }
                } else {
                    if (code == 401 || code == 403) AuthRepository.clearSession(context)
                    val detail = response.opt("detail")
                    val message = when (detail) {
                        is String -> detail
                        is org.json.JSONArray -> detail.toString()
                        else -> "No se pudo completar la operación (HTTP $code)"
                    }
                    Handler(context.mainLooper).post { onFailure(message) }
                }
            } catch (e: Exception) {
                Handler(context.mainLooper).post {
                    onFailure(e.message ?: "No se pudo conectar con TaxiDrive.")
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }
}
