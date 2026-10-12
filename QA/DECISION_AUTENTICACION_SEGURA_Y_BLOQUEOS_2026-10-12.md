# Bloqueo de autenticación y decisión pendiente — TaxiDrive.kz
Fecha: 2026-10-12
Rama: audit/legal-readiness-2026-10

## Hallazgo confirmado en el código
- `LoginScreen.kt` solo pide un teléfono y llama a `loginCorrecto(telefono)`; no inicia una sesión de Firebase Authentication.
- `RegisterScreen.kt` guarda el perfil usando el teléfono como ID de documento, sin demostrar control del número ni crear una sesión de Firebase Auth.
- `MainActivity.kt` permite elegir pasajero, conductor o administrador después de esa entrada simulada.
- `AuthRepository.kt` tiene funciones email/contraseña, pero la interfaz actual no las utiliza.
- `PassengerScreen.kt` ya exige `FirebaseAuth.currentUser.uid` antes de publicar; con el acceso actual, la publicación falla de forma cerrada hasta que exista sesión real.
- `AdminScreen.kt` ahora exige sesión Firebase, `usuarios/{uid}.tipo == "ADMIN"` y existencia de `administradores/{uid}` antes de cargar conductores. Esto también bloquea el acceso legítimo hasta implementar autenticación.

## Decisión de seguridad
No se debe aceptar un teléfono escrito como identidad autenticada ni usar el número telefónico como UID. Cualquiera podría suplantar a otra persona.

No se habilitará autenticación por teléfono en producción sin aprobación explícita del coste/condiciones de SMS. Los números ficticios de prueba solo sirven para pruebas controladas, no para usuarios reales.

La autenticación anónima de Firebase no verifica la propiedad del teléfono y no es una solución final para perfiles, roles, pagos ni contacto privado. La autenticación email/contraseña existente tampoco satisface el requisito de acceso solo por teléfono.

## Camino de implementación
1. Confirmar un método de autenticación aceptable para producción y sin cargos sorpresa; revisar precios y disponibilidad en el proyecto Firebase antes de activarlo.
2. Crear cuentas/perfiles vinculados exclusivamente a Firebase UID verificado.
3. Resolver rol mediante documento de perfil y, para ADMIN, también mediante `administradores/{uid}`; el cliente no debe poder asignarse roles.
4. Exigir sesión y rol válido en cada pantalla y reforzar las reglas de Firestore; las comprobaciones de interfaz nunca sustituyen las reglas del servidor.
5. Mover aprobación de conductores y activación de 500 ₸/24 h a una función de backend confiable. El cliente no debe escribir directamente los campos de aprobación o activación.
6. Probar acceso anónimo, pasajero, conductor pendiente/aprobado, administrador y usuario sin sesión antes de producir APK.

## No hacer
- No usar el teléfono como UID.
- No confiar en `tipo` enviado por el cliente.
- No permitir que el cliente se apruebe como conductor o confirme su propio pago.
- No desplegar reglas nuevas ni funciones en Firebase hasta revisar la configuración real y probarlas.
- No fusionar esta rama de auditoría en la rama principal todavía.

## Estado
Documento de decisión técnica; no implementa autenticación ni confirma pagos. No se ha compilado el APK ni se ha desplegado Firebase.
