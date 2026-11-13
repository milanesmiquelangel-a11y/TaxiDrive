package kz.taxidrive.qa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://cloud-001.tail3baa0f.ts.net/"

private data class LoginBody(val phone: String, val password: String)
private data class RegisterBody(
    val phone: String,
    @SerializedName("full_name") val fullName: String,
    val password: String,
    val role: String
)
private data class PassengerRequestBody(
    @SerializedName("origin_text") val originText: String,
    @SerializedName("destination_text") val destinationText: String,
    @SerializedName("departure_at") val departureAt: String,
    @SerializedName("passenger_count") val passengerCount: Int,
    val comment: String?
)

private interface TaxiDriveQaApi {
    @POST("auth/login") suspend fun login(@Body body: LoginBody): Response<JsonElement>
    @POST("auth/register") suspend fun register(@Body body: RegisterBody): Response<JsonElement>
    @POST("requests") suspend fun createRequest(
        @Header("Authorization") bearer: String,
        @Body body: PassengerRequestBody
    ): Response<JsonElement>
    @GET("auth/me") suspend fun me(@Header("Authorization") bearer: String): Response<JsonElement>
    @GET("drivers/me") suspend fun driverProfile(@Header("Authorization") bearer: String): Response<JsonElement>
    @GET("drivers/me/activation") suspend fun driverActivation(@Header("Authorization") bearer: String): Response<JsonElement>
    @GET("requests") suspend fun availableRequests(@Header("Authorization") bearer: String): Response<JsonElement>
    @GET("requests/{requestId}") suspend fun request(@Header("Authorization") bearer: String, @retrofit2.http.Path("requestId") requestId: Long): Response<JsonElement>
    @POST("offers") suspend fun createOffer(@Header("Authorization") bearer: String, @Body body: DriverOfferBody): Response<JsonElement>
    @GET("requests") suspend fun myRequests(@Header("Authorization") bearer: String): Response<JsonElement>
}

private data class DriverOfferBody(
    @SerializedName("request_id") val requestId: Long,
    @SerializedName("price_per_passenger_kzt") val pricePerPassengerKzt: Long,
    @SerializedName("available_seats") val availableSeats: Int,
    val message: String?
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenStore = TokenStore(this)
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaxiDriveQaApi::class.java)

        setContent {
            MaterialTheme {
                var phone by remember { mutableStateOf("") }
                var name by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var role by remember { mutableStateOf("passenger") }
                var accountRole by remember { mutableStateOf<String?>(null) }
                var driverProfileJson by remember { mutableStateOf("") }
                var activationJson by remember { mutableStateOf("") }
                var availableRequestsJson by remember { mutableStateOf("") }
                var offerRequestId by remember { mutableStateOf("3") }
                var offerPrice by remember { mutableStateOf("") }
                var offerMessage by remember { mutableStateOf("") }
                var token by remember { mutableStateOf(tokenStore.read()) }
                var status by remember { mutableStateOf(if (token != null) "Sesión recuperada de forma segura." else "Conectado a TaxiDrive API.") }
                var busy by remember { mutableStateOf(false) }
                var origin by remember { mutableStateOf("") }
                var destination by remember { mutableStateOf("") }
                var departure by remember {
                    mutableStateOf(OffsetDateTime.now().plusDays(1).withSecond(0).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                }
                var passengerCount by remember { mutableStateOf("1") }
                var comment by remember { mutableStateOf("") }
                var requestsJson by remember { mutableStateOf("") }
                val scope = rememberCoroutineScope()

                suspend fun acceptAuthResponse(response: Response<JsonElement>, action: String) {
                    if (!response.isSuccessful) {
                        status = "No se pudo $action (HTTP ${response.code()}): ${response.errorBody()?.string()?.take(500).orEmpty()}"
                        return
                    }
                    val json = response.body()?.asJsonObject
                    val accessToken = json?.get("access_token")?.asString
                    if (accessToken.isNullOrBlank()) {
                        status = "Respuesta recibida, pero no contiene token de acceso."
                        return
                    }
                    tokenStore.save(accessToken)
                    token = accessToken
                    val user = json.getAsJsonObject("user")
                    accountRole = user?.get("role")?.asString ?: role
                    status = "Sesión iniciada correctamente. Token guardado cifrado en este dispositivo."
                }

                LaunchedEffect(token) {
                    val currentToken = token
                    if (currentToken != null) {
                        try {
                            val me = api.me("Bearer $currentToken")
                            if (me.isSuccessful) {
                                val user = me.body()?.asJsonObject
                                accountRole = user?.get("role")?.asString ?: accountRole
                            } else if (me.code() == 401) {
                                tokenStore.clear(); token = null; accountRole = null
                                status = "La sesión expiró. Inicia sesión nuevamente."
                            }
                        } catch (_: Exception) { status = "No se pudo verificar la sesión. Comprueba tu conexión." }
                    } else accountRole = null
                }

                Scaffold(topBar = {
                    TopAppBar(title = { Text(if (token == null) "TaxiDrive QA · Backend directo" else if (accountRole == "driver") "TaxiDrive QA · Conductor" else "TaxiDrive QA · Pasajero") })
                }) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Entorno de pruebas independiente. No utiliza Firebase.", style = MaterialTheme.typography.bodyMedium)

                        if (token == null) {
                            OutlinedTextField(phone, { phone = it }, label = { Text("Teléfono (+7...)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(name, { name = it }, label = { Text("Nombre completo (para registro)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(password, { password = it }, label = { Text("Contraseña (mínimo 10 caracteres)") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = role == "passenger", onClick = { role = "passenger" }, label = { Text("Pasajero") })
                                FilterChip(selected = role == "driver", onClick = { role = "driver" }, label = { Text("Conductor") })
                            }
                            Button(
                                enabled = !busy && phone.isNotBlank() && password.length >= 10,
                                onClick = {
                                    scope.launch {
                                        busy = true
                                        try { acceptAuthResponse(api.login(LoginBody(phone.trim(), password)), "iniciar sesión") }
                                        catch (e: Exception) { status = "Error de conexión: ${e.message ?: "desconocido"}" }
                                        busy = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Iniciar sesión") }
                            OutlinedButton(
                                enabled = !busy && phone.isNotBlank() && name.isNotBlank() && password.length >= 10,
                                onClick = {
                                    scope.launch {
                                        busy = true
                                        try { acceptAuthResponse(api.register(RegisterBody(phone.trim(), name.trim(), password, role)), "crear la cuenta") }
                                        catch (e: Exception) { status = "Error de conexión: ${e.message ?: "desconocido"}" }
                                        busy = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Crear cuenta de prueba") }
                        } else {
                            Text("Sesión autenticada. El token no se muestra en pantalla.")
                            if (accountRole == "passenger" || accountRole == null) {
                                Text("Publicar solicitud de viaje", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(origin, { origin = it }, label = { Text("Origen") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(destination, { destination = it }, label = { Text("Destino") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(departure, { departure = it }, label = { Text("Fecha y hora ISO con zona horaria") }, supportingText = { Text("Ejemplo: 2026-11-14T10:00:00+05:00") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(passengerCount, { passengerCount = it.filter(Char::isDigit).take(2) }, label = { Text("Cantidad de pasajeros") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(comment, { comment = it }, label = { Text("Comentario (opcional)") }, modifier = Modifier.fillMaxWidth())
                                Button(
                                    enabled = !busy && origin.trim().length >= 2 && destination.trim().length >= 2 && (passengerCount.toIntOrNull() ?: 0) in 1..20,
                                    onClick = {
                                        scope.launch {
                                            busy = true
                                            try {
                                                val response = api.createRequest(
                                                    "Bearer $token",
                                                    PassengerRequestBody(origin.trim(), destination.trim(), departure.trim(), passengerCount.toInt(), comment.trim().ifBlank { null })
                                                )
                                                status = if (response.isSuccessful) {
                                                    "Solicitud creada correctamente (HTTP ${response.code()})."
                                                } else {
                                                    "No se pudo crear la solicitud (HTTP ${response.code()}): ${response.errorBody()?.string()?.take(500).orEmpty()}"
                                                }
                                            } catch (e: Exception) { status = "Error de conexión: ${e.message ?: "desconocido"}" }
                                            busy = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Publicar solicitud") }
                                OutlinedButton(
                                    enabled = !busy,
                                    onClick = {
                                        scope.launch {
                                            busy = true
                                            try {
                                                val response = api.myRequests("Bearer $token")
                                                requestsJson = if (response.isSuccessful) response.body()?.toString().orEmpty()
                                                    else "HTTP ${response.code()}: ${response.errorBody()?.string().orEmpty()}"
                                                status = "Consulta de solicitudes completada."
                                            } catch (e: Exception) { status = "Error de conexión: ${e.message ?: "desconocido"}" }
                                            busy = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Ver mis solicitudes") }
                                if (requestsJson.isNotBlank()) {
                                    Text("Mis solicitudes", style = MaterialTheme.typography.titleMedium)
                                    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                        Text(requestsJson, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            } else if (accountRole == "driver") {
                                Text("Panel del conductor", style = MaterialTheme.typography.titleMedium)
                                OutlinedButton(
                                    enabled = !busy,
                                    onClick = {
                                        scope.launch {
                                            busy = true
                                            try {
                                                val p = api.driverProfile("Bearer $token")
                                                driverProfileJson = if (p.isSuccessful) p.body()?.toString().orEmpty() else "HTTP ${p.code()}: ${p.errorBody()?.string().orEmpty()}"
                                                val a = api.driverActivation("Bearer $token")
                                                activationJson = if (a.isSuccessful) a.body()?.toString().orEmpty() else "HTTP ${a.code()}: ${a.errorBody()?.string().orEmpty()}"
                                                status = "Estado del conductor actualizado."
                                            } catch (e: Exception) { status = "Error al consultar perfil: ${e.message ?: "desconocido"}" }
                                            busy = false
                                        }
                                    }, modifier = Modifier.fillMaxWidth()
                                ) { Text("Consultar mi perfil y aprobación") }
                                if (driverProfileJson.isNotBlank()) Text("Perfil y aprobación: $driverProfileJson")
                                if (activationJson.isNotBlank()) Text("Activación: $activationJson")
                                OutlinedButton(
                                    enabled = !busy,
                                    onClick = {
                                        scope.launch {
                                            busy = true
                                            try {
                                                val r = api.availableRequests("Bearer $token")
                                                availableRequestsJson = if (r.isSuccessful) r.body()?.toString().orEmpty() else "HTTP ${r.code()}: ${r.errorBody()?.string().orEmpty()}"
                                                status = "Consulta de solicitudes disponibles completada."
                                            } catch (e: Exception) { status = "Error de conexión: ${e.message ?: "desconocido"}" }
                                            busy = false
                                        }
                                    }, modifier = Modifier.fillMaxWidth()
                                ) { Text("Ver solicitudes disponibles") }
                                if (availableRequestsJson.isNotBlank()) {
                                    Text("Solicitudes disponibles", style = MaterialTheme.typography.titleMedium)
                                    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                        Text(availableRequestsJson, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Text("Enviar oferta", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(offerRequestId, { offerRequestId = it.filter(Char::isDigit) }, label = { Text("ID de solicitud") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(offerPrice, { offerPrice = it.filter(Char::isDigit) }, label = { Text("Precio por pasajero (₸)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(offerMessage, { offerMessage = it }, label = { Text("Mensaje (opcional)") }, modifier = Modifier.fillMaxWidth())
                                Button(
                                    enabled = !busy && (offerRequestId.toLongOrNull() ?: 0) > 0 && (offerPrice.toLongOrNull() ?: 0) > 0,
                                    onClick = {
                                        scope.launch {
                                            busy = true
                                            try {
                                                val requestResponse = api.request("Bearer $token", offerRequestId.toLong())
                                                if (!requestResponse.isSuccessful) {
                                                    status = "No se pudo consultar la solicitud (HTTP ${requestResponse.code()})."
                                                } else {
                                                    val requestJson = requestResponse.body()?.asJsonObject
                                                    val passengerCount = requestJson?.get("passenger_count")?.asInt ?: 1
                                                    val response = api.createOffer(
                                                        "Bearer $token",
                                                        DriverOfferBody(offerRequestId.toLong(), offerPrice.toLong(), passengerCount, offerMessage.trim().ifBlank { null })
                                                    )
                                                    status = if (response.isSuccessful) "Oferta enviada correctamente (HTTP ${response.code()}). Las plazas se ajustaron automáticamente a la cantidad solicitada por el pasajero." else "No se pudo enviar la oferta (HTTP ${response.code()}): ${response.errorBody()?.string()?.take(500).orEmpty()}"
                                                }
                                            } catch (e: Exception) { status = "Error de conexión: ${e.message ?: "desconocido"}" }
                                            busy = false
                                        }
                                    }, modifier = Modifier.fillMaxWidth()
                                ) { Text("Enviar oferta") }
                            } else {
                                Text("No se pudo determinar el rol de esta cuenta. Cierra sesión e inicia nuevamente.")
                            }
                            OutlinedButton(
                                enabled = !busy,
                                onClick = {
                                    tokenStore.clear()
                                    token = null
                                    accountRole = null
                                    requestsJson = ""
                                    status = "Sesión cerrada. El token local fue eliminado."
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Cerrar sesión") }
                        }

                        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text("Estado", style = MaterialTheme.typography.titleMedium)
                        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            Text(status, Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
