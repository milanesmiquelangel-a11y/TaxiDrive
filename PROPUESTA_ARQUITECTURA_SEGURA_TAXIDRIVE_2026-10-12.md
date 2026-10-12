# TaxiDrive.kz Android — propuesta de arquitectura segura para revisión
Fecha: 2026-10-12
Estado: propuesta técnica; NO desplegar ni implementar en producción sin contrastarla con la APK real, las reglas Firebase vigentes y el asesoramiento local.

## 1. Por qué se propone
La rama `audit/legal-readiness-2026-10` contiene un prototipo Android con Firebase. El código revisado presenta autenticación y autorización inconsistentes, solicitudes con datos incompletos, ofertas sin vínculo explícito a una solicitud y escrituras administrativas desde el cliente. Este documento propone un diseño objetivo, no afirma que se haya implementado.

## 2. Autenticación y roles
- El teléfono no puede ser la contraseña ni el identificador de autenticación por sí solo.
- El código revisado usa Firebase Authentication con email/contraseña en `AuthRepository`, pero `LoginScreen` no llama a esa autenticación: permite avanzar con solo escribir un teléfono. La pantalla principal permite elegir pasajero, conductor o administrador sin comprobar el rol autenticado.
- No permitir entrada a paneles solo por navegación/interfaz. Cada acción sensible debe comprobar la identidad autenticada y autorización en backend/reglas.
- Si se mantiene inicio de sesión por teléfono sin SMS de pago, no fingir verificación de titularidad: evaluar un proveedor de autenticación compatible, passkeys u otro flujo seguro. No guardar contraseñas caseras ni tratar un número introducido como prueba de identidad.
- Nunca permitir que el registro público asigne el rol ADMIN. La administración debe provisionarse por un proceso controlado, con verificación en servidor y acceso mínimo.

## 3. Modelo de datos recomendado (borrador)
Separar los datos de viaje visibles a conductores de los datos de contacto privados:

```
usuarios/{uid}
  nombre, apellidos, telefono, tipo, aprobado
  trialUntil, activationUntil, activationPending

solicitudes/{requestId}
  passengerUid, origin, destination, date, time
  commentPublic (solo si no identifica), status, createdAt

solicitudesPrivadas/{requestId}
  passengerUid, telefono, nombre (solo si realmente necesario)

solicitudes/{requestId}/ofertas/{offerId}
  driverUid, price, comment, status, createdAt

chats/{offerId}
  requestId, passengerUid, driverUid, status

chats/{offerId}/mensajes/{messageId}
  senderUid, text, createdAt
```

Este esquema es conceptual. La implementación exacta debe considerar consultas, índices, reglas, notificaciones y compatibilidad/migración con datos existentes. No copiar datos privados a documentos que los conductores puedan leer.

## 4. Reglas de autorización que deben probarse
- Pasajero: crear solicitudes solo como su UID; leer/modificar solo sus solicitudes según estado.
- Conductor: leer únicamente campos públicos de solicitudes activas que pueda ofertar; crear ofertas solo con su UID y si está aprobado y habilitado (30 días de prueba o activación vigente de 24 h).
- Conductor sin oferta: no puede leer teléfono, documentos privados ni chat de la solicitud.
- Pasajero y conductor: solo pueden leer/escribir mensajes si son los dos participantes de la oferta aceptada/autorizada y el chat sigue activo.
- Administrador: aprobación, confirmación de pagos y activación únicamente con identidad ADMIN provisionada en servidor y autorización comprobada por reglas/backend.
- Cliente no puede escribir directamente `aprobado`, `trialUntil`, `activationUntil`, roles, pago confirmado ni campos de seguridad.
- Usuarios no autenticados: sin acceso a datos privados ni operaciones de escritura.
- Probar todos los casos con Firebase Emulator Suite. Las reglas no pueden ocultar campos individuales dentro de un documento que ya se ha permitido leer.

## 5. Activación de 500 ₸ y prueba gratuita
- 30 días desde la aprobación real del conductor.
- 500 ₸ por exactamente 24 horas desde confirmación válida de pago.
- El cliente solo solicita activación y muestra estado; no concede la activación.
- El backend verifica el evento de pago o una confirmación manual de un administrador autorizado, registra auditoría e impide duplicados/repeticiones.
- Hasta integrar Kaspi Pay o definir un procedimiento manual verificable, no etiquetar el botón de prueba como pago real ni activar conductores a partir de una solicitud sin validar el pago.

## 6. Datos personales y alojamiento
La ley kazaja exige almacenamiento territorial de datos personales; la versión vigente y la fecha de entrada en vigor de las reformas deben confirmarse en la fuente oficial y con asesor local. Las regiones Firebase mostradas anteriormente estaban fuera de Kazajistán. No se debe asumir que un consentimiento general corrige la ubicación de la base. Inventariar todos los servicios que procesan datos (Authentication, Firestore, Storage, Analytics, FCM, logs, backups) y decidir qué puede alojarse legalmente en Kazajistán antes de migrar.
Fuente oficial: https://old.adilet.zan.kz/rus/docs/Z1300000094

## 7. Plan seguro de ejecución
1. Confirmar con el propietario qué APK/paquete fuente es la versión objetivo y obtener ese código.
2. Copiar reglas Firebase reales y exportar/backupear los datos/configuración; no cambiar regiones sin plan de migración.
3. Preparar cambios en rama aislada, nunca directamente en producción.
4. Implementar autenticación/roles y modelo público/privado en un entorno de prueba.
5. Añadir pruebas automáticas de reglas, acceso, activación, expiración, chat y ofertas.
6. Compilar y probar manualmente la APK, incluidos los cuatro idiomas y la tarifa de 500 ₸.
7. Revisar con asesor local la estructura ИП de Irina Vladimirovna Beloglazova, el tratamiento fiscal y las obligaciones de una plataforma de información/despacho.
8. Solo tras pruebas y revisión legal, proponer PR; desplegar por separado con respaldo y reversión.

## 8. Estado
No se ha compilado esta propuesta, no se han ejecutado pruebas de emulador, no se ha desplegado Firebase y no se han modificado reglas de producción. Es una especificación para orientar el siguiente cambio con seguridad.
