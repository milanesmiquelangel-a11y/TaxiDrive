package kz.taxidrive.app.repository

import android.content.Context
import android.os.Handler
import org.json.JSONArray
import org.json.JSONObject
import kz.taxidrive.app.auth.AuthRepository
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object PassengerApiRepository {
    private const val BASE_URL = "https://cloud-001.tail3baa0f.ts.net"

    fun createRequest(
        context: Context,
        origin: String,
        destination: String,
        departureAt: String,
        passengerCount: Int,
        comment: String,
        onSuccess: (JSONObject) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val body = JSONObject()
            .put("origin_text", origin.trim())
            .put("destination_text", destination.trim())
            .put("departure_at", departureAt)
            .put("passenger_count", passengerCount)
            .put("comment", comment.trim().takeIf { it.isNotBlank() })

        Thread {
            var connection: HttpURLConnection? = null
            try {
                val token = AuthRepository.accessToken(context)
                    ?: throw IllegalStateException("La sesión ha caducado. Inicie sesión nuevamente.")
                connection = (URL(BASE_URL + "/requests").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
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
                    Handler(context.mainLooper).post { onSuccess(response) }
                } else {
                    if (code == 401 || code == 403) AuthRepository.clearSession(context)
                    val detail = response.opt("detail")
                    val message = when (detail) {
                        is String -> detail
                        is org.json.JSONArray -> detail.toString()
                        else -> "No se pudo publicar la solicitud (HTTP $code)"
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

    fun getRequestOffers(
        context: Context,
        requestId: Int,
        onSuccess: (JSONArray) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null
            try {
                val token = AuthRepository.accessToken(context)
                    ?: throw IllegalStateException("La sesión ha caducado. Inicie sesión nuevamente.")
                connection = (URL("$BASE_URL/requests/$requestId/offers").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Accept", "application/json")
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
                val response = if (responseText.isNotBlank()) JSONObject(responseText) else JSONObject()
                if (code in 200..299) {
                    val items = response.optJSONArray("items") ?: JSONArray()
                    Handler(context.mainLooper).post { onSuccess(items) }
                } else {
                    if (code == 401 || code == 403) AuthRepository.clearSession(context)
                    val detail = response.opt("detail")
                    val message = when (detail) {
                        is String -> detail
                        is org.json.JSONArray -> detail.toString()
                        else -> "No se pudieron consultar las ofertas (HTTP $code)"
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
