package kz.taxidrive.qa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://cloud-001.tail3baa0f.ts.net/"

private data class LoginBody(val phone: String, val password: String)
private data class RegisterBody(
    val phone: String,
    val full_name: String,
    val password: String,
    val role: String
)

private interface TaxiDriveQaApi {
    @POST("auth/login") suspend fun login(@Body body: LoginBody): Response<com.google.gson.JsonElement>
    @POST("auth/register") suspend fun register(@Body body: RegisterBody): Response<com.google.gson.JsonElement>
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                var result by remember { mutableStateOf("Backend: $BASE_URL") }
                var busy by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                Scaffold(topBar = { TopAppBar(title = { Text("TaxiDrive QA · Backend directo") }) }) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Aplicación de pruebas independiente. No utiliza Firebase.", style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(phone, { phone = it }, label = { Text("Teléfono (+7...)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(name, { name = it }, label = { Text("Nombre completo (para registro)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = role == "passenger", onClick = { role = "passenger" }, label = { Text("Pasajero") })
                            FilterChip(selected = role == "driver", onClick = { role = "driver" }, label = { Text("Conductor") })
                        }
                        Button(
                            enabled = !busy && phone.isNotBlank() && password.length >= 10,
                            onClick = {
                                scope.launch {
                                    busy = true
                                    result = "Enviando inicio de sesión..."
                                    result = try {
                                        val response = api.login(LoginBody(phone.trim(), password))
                                        "HTTP ${response.code()}\n" + (response.body()?.toString() ?: response.errorBody()?.string().orEmpty())
                                    } catch (e: Exception) {
                                        "Error de conexión: ${e.message ?: "desconocido"}"
                                    }
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
                                    result = "Enviando registro..."
                                    result = try {
                                        val response = api.register(RegisterBody(phone.trim(), name.trim(), password, role))
                                        "HTTP ${response.code()}\n" + (response.body()?.toString() ?: response.errorBody()?.string().orEmpty())
                                    } catch (e: Exception) {
                                        "Error de conexión: ${e.message ?: "desconocido"}"
                                    }
                                    busy = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Crear cuenta de prueba") }
                        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text("Respuesta del servidor", style = MaterialTheme.typography.titleMedium)
                        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            Text(result, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
