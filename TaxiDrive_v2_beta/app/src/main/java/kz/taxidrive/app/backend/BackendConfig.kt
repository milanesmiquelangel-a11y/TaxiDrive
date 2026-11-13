package kz.taxidrive.app.backend

/** Public HTTPS endpoint for the new TaxiDrive API. Keep Firebase active during staged migration. */
object BackendConfig {
    const val BASE_URL = "https://cloud-001.tail3baa0f.ts.net/"
    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 30L
}
