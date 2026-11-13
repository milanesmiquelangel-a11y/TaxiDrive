package kz.taxidrive.app.data.network

import android.content.Context
import kz.taxidrive.app.BuildConfig
import kz.taxidrive.app.data.security.TokenStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(context: Context) {
    private val tokenStore = TokenStore(context)

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("Accept", "application/json")
            .apply {
                tokenStore.read()?.takeIf { it.isNotBlank() }?.let {
                    header("Authorization", "Bearer $it")
                }
            }
            .build()
        chain.proceed(request)
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> create(service: Class<T>): T = retrofit.create(service)

    fun clearSession() = tokenStore.clear()

    fun saveSessionToken(token: String) = tokenStore.save(token)

    fun readSessionToken(): String? = tokenStore.read()
}
