package kz.taxidrive.app.backend

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Singleton HTTP client. Authorization is passed explicitly; tokens are never logged or persisted here. */
object BackendClient {
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(BackendConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(BackendConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(BackendConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    val api: TaxiDriveApi by lazy {
        Retrofit.Builder()
            .baseUrl(BackendConfig.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaxiDriveApi::class.java)
    }
}
