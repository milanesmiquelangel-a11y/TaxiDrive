# TaxiDrive backend integration foundation

This branch adds a Retrofit client for the new FastAPI service at `https://cloud-001.tail3baa0f.ts.net/`.

## Migration safety
- Firebase Authentication and Firestore repositories remain unchanged and active.
- No Firebase users or test profiles are migrated.
- No Android screens have been switched to the new backend.
- The API bearer token is supplied explicitly to authenticated methods; this foundation does not persist tokens or log credentials.
- The base URL is centralized in `BackendConfig.kt` for future endpoint rotation.

## Included API contract
- Registration and login with phone/password.
- Current profile.
- Passenger requests and driver offers.
- Offer acceptance and offer-scoped chat.
- Notifications and driver activation status.
- Public health check.

## Next required work
1. Build the Android project and resolve any dependency/toolchain issues.
2. Add secure token storage using Android Keystore-backed storage.
3. Implement explicit backend auth mode and UI flows without disabling Firebase fallback.
4. Add DTO mapping and end-to-end QA using temporary accounts only.
5. Do not enable production cutover until parity, security, and Play Store checks pass.
