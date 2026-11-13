# TaxiDrive.kz — Auditoría y plan de migración del backend de la aplicación original

Fecha: 2026-11-13
Base de trabajo: `main` (solo lectura). Rama de migración: `migration-original-app-backend`.

## Objetivo y reglas de seguridad

Migrar la aplicación Android original `kz.taxidrive.app` desde Firebase Auth/Firestore hacia la API propia y PostgreSQL, sin crear otra aplicación de producto ni interrumpir el AAB que está sometido a revisión de Google Play.

**No desconectar Firebase, no borrar datos, no modificar `main`, no cambiar el AAB ya enviado y no publicar** hasta que la sustitución esté implementada y probada de extremo a extremo. La migración se hará por capas en esta rama.

## Hallazgos verificados en el código de `main`

### Configuración
- `TaxiDrive_v2_beta/app/build.gradle.kts` aplica `com.google.gms.google-services` y declara Firebase Auth 23.2.0 y Firestore 25.1.0.
- El módulo no declara aún Retrofit/OkHttp en la rama `main`.
- `AndroidManifest.xml` no declara permiso INTERNET explícito.
- El identificador de producción es `kz.taxidrive.app`; no se cambiará.

### Navegación y autenticación
- `MainActivity.kt` usa navegación manual con estados login/registro/inicio/pasajero/conductor/admin.
- El login actual (`LoginScreen.kt`) solo pide teléfono y llama a un callback; no autentica contra un servicio.
- `AuthRepository.kt` implementa Firebase email/password, no el flujo telefónico del backend.
- `RegisterScreen.kt` crea un documento Firestore, pero no crea una credencial de autenticación coherente. También inicializa el UID con el teléfono.
- La pantalla de inicio muestra roles como botones; el rol debe proceder del perfil autenticado, no de una elección libre después de iniciar sesión.

### Datos y funciones Firebase detectadas
- `UserRepository.kt`: colección `usuarios`; lectura/alta de perfil; aprobación; trial; activación de 500 ₸ y 24 horas; listado de conductores.
- `RequestRepository.kt`: colección `solicitudes`.
- `OfferRepository.kt`: colección `ofertas`.
- `FeedRepository.kt`: lectura de ofertas y solicitudes, filtrado local.
- `TripRepository.kt`: colección `viajes`.
- `DriverScreen.kt` y `AdminScreen.kt` dependen directamente de UserRepository/OfferRepository.
- `PassengerScreen.kt` publica directamente a RequestRepository.

### Brechas funcionales encontradas en la aplicación original
- Pasajero: la pantalla solicita un precio, aunque la regla de negocio establece que el pasajero no fija precio; no incluye cantidad de pasajeros; fechas/horas son texto libre; no se ve ciclo completo de ofertas, aceptación, chat e historial.
- Conductor: pantalla crea ofertas independientes con origen/destino/fecha/hora, en lugar de responder a una solicitud concreta; falta expediente documental y carga de fotos; no presenta flujo completo de aprobación/activación.
- Administrador: aprueba conductores y confirma activaciones desde cliente; la confirmación de pago no debe ser una acción administrativa manual sin comprobante/verificación de proveedor. Las acciones privilegiadas deben residir en API.
- Navegación: existe NavGraph adicional, pero MainActivity no lo utiliza; revisar y unificar sin duplicar rutas.

## Backend propio ya disponible (implementado y probado en la etapa anterior)
- API FastAPI y PostgreSQL en VPS; API HTTPS pública vía Tailscale Funnel.
- Registro/login por teléfono y contraseña, JWT, perfil actual, throttling.
- Perfil de conductor, aprobación administrativa, trial 30 días desde aprobación.
- Carga privada de foto frontal y volante.
- Solicitudes de pasajero sin precio, con origen/destino/fecha/hora/cantidad.
- Ofertas asociadas a solicitud, precio por pasajero obligatorio, aceptación y rechazo de otras ofertas.
- Chat asociado a oferta; notificaciones relacionadas con oferta.
- Activación de 500 ₸/24h y registro de intención de pago; integración real Kaspi aún pendiente de credenciales/onboarding.
- No se migran cuentas de prueba Firebase. Admin se recrea de forma segura cuando corresponda.

## Plan de migración secuencial — no saltar etapas

### Etapa 0 — Auditoría y contrato (actual)
- Inventariar todos los usos Firebase y modelos/campos.
- Comparar contrato JSON de API con DTOs Android.
- Definir matriz de pantallas, endpoints, estados, errores y criterios de aceptación.
- Identificar datos reales que deban conservarse; excluir perfiles de prueba.

### Etapa 1 — Capa de infraestructura Android
- Añadir cliente Retrofit/OkHttp y DTOs tipados al módulo original.
- Base URL configurable por build type, no duplicada en pantallas.
- Almacenamiento de token cifrado con Android Keystore.
- Interceptor Authorization, gestión 401/expiración, errores de red y estados de carga.
- Pruebas unitarias del cliente/repositorios.
- Firebase sigue instalado pero no debe recibir nuevas escrituras desde los flujos ya migrados.

### Etapa 2 — Pasajero completo
- Registro/login/logout y perfil.
- Crear solicitud: origen, destino, fecha/hora seleccionadas, cantidad de pasajeros, comentario opcional; no teléfono ni precio.
- Listar/ver/editar/cancelar propias solicitudes.
- Recibir ofertas; mostrar precio por pasajero; aceptar una y rechazar restantes.
- Chat solo con conductor de oferta aceptada; contacto y notificaciones; historial.
- Pruebas E2E con dos cuentas separadas.

### Etapa 3 — Conductor completo
- Registro/login, datos personales y vehículo.
- Subir frontal con matrícula y volante (ambas obligatorias).
- Estado pendiente/aprobado/rechazado y corrección de expediente.
- 30 días gratis desde aprobación.
- Solicitudes disponibles solo cuando aprobado y activado.
- Oferta vinculada a solicitud: precio por pasajero obligatorio; el conductor no introduce número de plazas.
- Chat solo para sus ofertas; historial y notificaciones.
- Activación 500 ₸/24h; Kaspi real solo cuando el comercio esté habilitado.

### Etapa 4 — Administrador completo
- Login administrativo seguro; no selección libre de rol.
- Cola de conductores y revisión de fotos.
- Aprobar/rechazar; inicio de trial desde el backend.
- Pagos, estados y métricas; sin endpoints privilegiados en cliente.
- Auditoría de acciones y controles de acceso.

### Etapa 5 — Retirada de Firebase
- Buscar y eliminar todas las referencias Firebase Auth/Firestore/Storage/FCM del código de producción.
- Sustituir notificaciones por el mecanismo propio/FCM con tokens registrados en backend si FCM se mantiene solo como transporte.
- Retirar plugin google-services, dependencias y configuración Firebase únicamente cuando no haya referencias.
- Ejecutar pruebas completas y comprobar que no hay llamadas ni escrituras Firebase.
- No borrar el proyecto ni sus datos hasta terminar cualquier conservación/exportación legalmente necesaria.

### Etapa 6 — Release
- QA en dispositivo, pruebas de seguridad y regresión.
- Generar AAB firmado con la configuración de producción correcta.
- Publicar solo con aprobación explícita del propietario y según el estado de Google Play.

## Criterio de cierre por etapa
No se considera completa por compilar. Debe haber: pantalla integrada en la aplicación original, endpoint conectado, validaciones, autorización por rol, estados de error/carga, pruebas automatizadas y prueba real en dispositivo. Registrar evidencia y actualizar este documento antes de pasar a la siguiente etapa.

## Estado al crear este documento
- Auditoría inicial de archivos clave: realizada.
- Cambios funcionales en la aplicación original: ninguno todavía.
- Firebase: permanece conectado e intacto.
- `main`: intacta.
- Rama de trabajo: `migration-original-app-backend`.
- Siguiente tarea: completar inventario de pantallas/modelos/configuración y cerrar el contrato de API antes de implementar la capa de red.
