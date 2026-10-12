# Bloqueo de autenticación y decisiones técnicas — TaxiDrive.kz
Fecha: 2026-10-12
Rama: audit/legal-readiness-2026-10

## Evidencia revisada
- `LoginScreen.kt` solo solicita un teléfono y llama a `loginCorrecto(telefono)`; no inicia sesión en Firebase Authentication.
- `RegisterScreen.kt` guarda un documento de usuario usando el teléfono como UID, pero no crea una cuenta/sesión de Firebase Authentication.
- `MainActivity.kt` permite abrir pasajero, conductor y administrador sin comprobar una sesión autenticada ni el rol.
- `PassengerScreen.kt` ahora exige un UID de Firebase Authentication antes de publicar. Por tanto, el flujo actual no permite publicar correctamente hasta que se implemente el inicio de sesión real.
- `UserRepository.kt` modifica aprobación y activación directamente desde el cliente. Estas operaciones deben migrarse a un backend confiable con autorización.

## Decisión de seguridad
No se debe crear una sesión fingida a partir del número escrito ni usar el número como UID. Eso no verifica la titularidad del teléfono y permitiría suplantación.

## Opciones para el inicio de sesión
1. Firebase Phone Authentication: verifica el número, normalmente mediante SMS; revisar costes, disponibilidad y cuotas antes de activarlo. No activarlo sin revisar el presupuesto del propietario.
2. Correo y contraseña: no requiere SMS, pero cambia la experiencia y necesita un correo real o un flujo de recuperación.
3. Proveedor de identidad externo: requiere elegir y configurar proveedor; puede tener limitaciones regionales y costes.
4. Código propio enviado por WhatsApp/SMS: requiere un proveedor externo y no es gratis garantizado.

No se debe escoger automáticamente una opción que genere cargos. Hasta elegir y configurar una opción real, el sistema debe fallar de forma segura y no permitir operaciones autenticadas.

## Siguiente implementación obligatoria
1. Definir el proveedor de autenticación sin introducir costes inesperados.
2. Cambiar el registro para crear primero la cuenta Firebase Auth y luego el perfil, usando el UID emitido por Firebase.
3. Cambiar inicio de sesión para validar credenciales y recuperar perfil mediante el UID autenticado.
4. Hacer que la navegación dependa de sesión y rol recuperado de una fuente confiable; ocultar botones no sustituye las reglas del servidor.
5. Quitar la ruta de ADMIN para cuentas ordinarias y proteger las operaciones administrativas con comprobación en servidor.
6. Migrar aprobación de conductores y activación de 500 ₸/24 h a backend confiable; el periodo gratuito será de 30 días desde la aprobación autorizada.
7. Probar en Firebase Emulator Suite o proyecto separado antes de modificar reglas de producción.

## Estado
Este documento registra evidencia y el bloqueo técnico. No se han cambiado reglas ni desplegado nada. No se ha ejecutado compilación o prueba automatizada en esta sesión.
