# Matriz de pruebas de seguridad y aceptación — TaxiDrive.kz
Fecha: 2026-10-12
Estado: plan de pruebas; todavía NO ejecutado. No constituye certificación de seguridad ni autorización de lanzamiento.

## Objetivo
Convertir los hallazgos de auditoría en criterios verificables antes de modificar reglas de Firebase, generar un APK nuevo o publicar el servicio. Ejecutar primero en proyecto/entorno de pruebas, nunca directamente en producción.

## Bloqueadores previos
1. Identidad real: cada pantalla y operación debe requerir una sesión Firebase Authentication válida; escribir un teléfono en Firestore o introducirlo en la pantalla no demuestra posesión del número.
2. Roles: un usuario PASAJERO no puede invocar funciones de CONDUCTOR o ADMIN. La administración debe verificarse en servidor, no solo por navegación/ocultación de botones.
3. Privacidad: el teléfono del pasajero no puede estar en un documento de solicitud legible por conductores que no hayan ofertado. Firestore devuelve el documento completo; separar los datos privados en un documento con reglas propias.
4. Oferta: cada oferta debe incluir el ID de la solicitud y UID del conductor, validado en servidor/reglas; no basta con crear otro documento de tipo publicación.
5. Chat: solo pasajero propietario y conductor autor de una oferta válida para esa solicitud pueden leer/escribir mensajes; terceros y ofertas rechazadas/canceladas deben denegarse según la política de producto.
6. Activación: importe 500 ₸, duración de 24 horas desde confirmación válida; prueba gratuita de 30 días desde aprobación del conductor. El cliente no debe poder concederse a sí mismo aprobación, pago confirmado o activación.
7. Datos en Kazajistán: no lanzar hasta que se confirme dónde se almacenan realmente los datos personales y se resuelva la arquitectura/región con asesoramiento jurídico local. Las capturas previas indicaban servicios/regiones fuera de Kazajistán, pero no se ha verificado la configuración actual de la consola.
8. Reconciliar APK y código: demostrar qué commit produjo el APK que se distribuirá; la coincidencia de nombres de clases no demuestra reproducibilidad.

## Matriz de autorización
| Caso | Acción | Resultado esperado |
|---|---|---|
| A01 | Usuario no autenticado intenta leer solicitudes | Denegado |
| A02 | Persona escribe un teléfono sin sesión autenticada | No obtiene sesión ni acceso a datos |
| A03 | PASAJERO intenta abrir pantalla/operación de ADMIN | Denegado por servidor y reglas |
| A04 | CONDUCTOR aprobado, sin oferta, lee teléfono privado del pasajero | Denegado |
| A05 | CONDUCTOR aprobado, sin oferta, lee datos públicos de la solicitud | Solo campos públicos necesarios, si la política permite ver la solicitud |
| A06 | CONDUCTOR crea oferta sin UID autenticado propio | Denegado |
| A07 | Oferta enviada para solicitud inexistente o cerrada | Denegado |
| A08 | Conductor ofrece y obtiene relación válida con la solicitud | Se crea oferta vinculada a solicitud y UID autenticado |
| A09 | Conductor sin oferta intenta abrir chat | Denegado |
| A10 | Participante autorizado abre chat de la oferta correcta | Permitido |
| A11 | Conductor de otra oferta intenta leer/escribir chat | Denegado |
| A12 | Usuario cambia en cliente su rol a ADMIN | Denegado; rol protegido en servidor |
| A13 | Conductor se marca a sí mismo como aprobado | Denegado |
| A14 | Cliente marca pago como confirmado o se concede activationUntil | Denegado; solo backend/admin autorizado |
| A15 | Pago confirmado por el proceso autorizado | Activación de 24 h calculada en servidor, idempotente |
| A16 | Activación vence | Oferta nueva denegada hasta nueva activación; datos existentes conservados |
| A17 | Conductor dentro de los 30 días gratuitos desde aprobación | Puede ofertar sin pagar |
| A18 | Usuario no autorizado lee documento privado del conductor | Denegado |
| A19 | Usuario autenticado lee foto de perfil | Solo si la foto es deliberadamente pública; no usar esta regla para documentos de identidad |
| A20 | Consultas masivas que incluyen documentos no autorizados | Consulta completa denegada; las reglas no actúan como filtros |

## Matriz funcional y de producto
| Caso | Prueba | Resultado esperado |
|---|---|---|
| F01 | Pasajero publica viaje | No solicita precio ni teléfono manual; teléfono viene de perfil privado |
| F02 | Conductor publica oferta | Introduce precio; la oferta se asocia a una solicitud concreta |
| F03 | Pasajero acepta una oferta | Se registra la selección y el chat se asocia a esa oferta/solicitud |
| F04 | Notificación de nueva solicitud | Solo se envía a los conductores previstos por la política, sin revelar datos privados |
| F05 | Notificación/chat/contacto de una solicitud | Un conductor sin oferta no recibe acceso ni notificaciones de esa relación |
| F06 | Idiomas | Kazajo predeterminado; kazajo, ruso, español e inglés disponibles en selector visible |
| F07 | Fecha y hora | Campos seleccionables sin que el teclado tape los controles |
| F08 | Chat en móvil | Campo de escritura permanece visible al abrir el teclado |
| F09 | Precio de activación | 500 ₸; nunca 200 ₸ en interfaz, funciones, documentación o configuración activa |
| F10 | Vigencia de activación | 24 horas desde la confirmación autorizada, no desde la solicitud de pago |
| F11 | Prueba gratuita | 30 días desde aprobación del perfil del conductor |
| F12 | Roles | PASAJERO, CONDUCTOR y ADMIN separados y verificados |
| F13 | Campos excluidos | No añadir conductor favorito, capacidad de pasajeros ni capacidad de equipaje |

## Procedimiento de ejecución
1. Congelar y anotar commit, SHA-256 del APK, versionCode/versionName y huella del certificado de firma.
2. Confirmar que el APK proviene del código que se está auditando; producir un build reproducible en una máquina con Android SDK/Gradle y registrar salida completa.
3. Usar Firebase Emulator Suite o un proyecto Firebase de pruebas separado. No cambiar reglas de producción para facilitar las pruebas.
4. Crear cuentas de prueba separadas para anónimo, pasajero A, pasajero B, conductor A aprobado, conductor B aprobado, conductor pendiente y administrador legítimo.
5. Ejecutar todos los casos A01–A20 y F01–F13. Guardar resultados PASS/FAIL, logs sin teléfonos ni datos reales y evidencia del emulador.
6. Migrar datos de prueba a la nueva estructura pública/privada; verificar que el cliente antiguo no pueda leer campos privados.
7. Revisar notificaciones y Cloud Functions, incluidos permisos de ejecución y validación de UID/rol en servidor.
8. Confirmar ubicación real de bases de datos, copias, logs, Storage y otros objetos con datos personales; documentar la revisión legal antes de producción.
9. Solo tras revisión de código y resultados, preparar pull request. No desplegar ni fusionar automáticamente.

## Evidencia y limitaciones actuales
- Este archivo define las pruebas, no informa resultados de ejecución.
- No se ha ejecutado Gradle, Emulator Suite, pruebas en dispositivo ni verificación criptográfica completa del APK en esta sesión.
- No se han cambiado reglas de Firebase ni se ha desplegado nada.
- El archivo ZIP y el APK no se han demostrado como artefactos del mismo commit.
- Los requisitos legales y la ubicación de datos requieren verificación actual en consola y revisión profesional en Kazajistán.

## Referencias técnicas
- Firebase, control de acceso a campos: https://firebase.google.com/docs/firestore/security/rules-fields
- Firebase, reglas inseguras y pruebas antes de desplegar: https://firebase.google.com/docs/firestore/security/insecure-rules
- Ley de Kazajistán sobre datos personales (texto oficial Adilet): https://old.adilet.zan.kz/rus/docs/Z1300000094
