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
