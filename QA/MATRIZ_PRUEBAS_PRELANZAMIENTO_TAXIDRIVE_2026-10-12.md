# TaxiDrive.kz — matriz de pruebas de aceptación y seguridad
Fecha: 2026-10-12
Rama: audit/legal-readiness-2026-10
Estado: plan de pruebas; todavía no ejecutado. No usar con datos reales.

## Regla de trabajo
Esta matriz se aplica únicamente después de identificar el commit y APK exactos que se quieren validar. El árbol Android actualmente inspeccionado puede no corresponder a la APK probada el 27/09/2026. No se deben considerar aprobadas pruebas solo porque estén descritas en una nota de versión.

## P0 — bloqueantes antes de producción

| ID | Prueba | Preparación | Resultado esperado | Evidencia que debe guardarse |
|---|---|---|---|---|
| AUTH-01 | Usuario no autenticado intenta abrir pantallas de pasajero/conductor/admin | Cerrar sesión o usar instalación limpia | Acceso denegado; navegación no crea una identidad falsa | Captura/log y UID de prueba |
| AUTH-02 | Registro con teléfono | Número de prueba controlado | No se considera verificado solo por escribirlo; la sesión de Firebase debe corresponder al usuario | Registro de Auth y documento creado |
| AUTH-03 | Pasajero intenta entrar como conductor/admin | Cuenta de rol PASAJERO | Backend/reglas deniegan operaciones privilegiadas aunque se modifique la interfaz | Resultado de prueba de reglas |
| AUTH-04 | Usuario intenta autoconcederse ADMIN | Cuenta normal | No puede crear ni modificar su rol privilegiado ni registrarse en la lista de administradores | Resultado de prueba de reglas |
| DATA-01 | Conductor sin oferta lee solicitudes/contactos | Conductor aprobado sin ofertas | No obtiene teléfono, notas privadas ni chat de solicitudes no vinculadas | Prueba automatizada de lectura denegada |
| DATA-02 | Conductor con oferta vinculada lee contacto permitido | Oferta válida asociada a solicitud existente | Solo obtiene la información permitida por la política; no otras solicitudes | Prueba automatizada y esquema |
| DATA-03 | Lectura de datos privados de otro usuario | Usuario autenticado distinto | Lectura denegada | Prueba automatizada |
| DATA-04 | Datos públicos/privados separados | Solicitud de prueba | La colección pública no contiene teléfono ni otros campos privados; acceso privado está protegido por reglas | Esquema/documentos de prueba |
| DATA-05 | Ubicación de almacenamiento | Inventario de Firebase/Storage/backup/logs | Servicios y copias que almacenan datos personales cumplen el diseño aprobado para Kazajistán y la normativa vigente | Exportación de configuración y revisión legal |
| PAY-01 | Usuario normal intenta confirmar activación | Cuenta de conductor no admin | El cliente no puede marcar pago como confirmado ni escribir directamente activación | Prueba denegada |
| PAY-02 | Confirmación repetida de un mismo pago | Pago de prueba con identificador único | Operación idempotente; no duplica ni extiende indebidamente la activación | Logs con IDs y timestamps |
| PAY-03 | Duración de activación | Pago válido confirmado en T0 | Activa hasta T0 + 24 h; expira al llegar el límite según zona horaria acordada | Timestamps UTC y pruebas de borde |
| PAY-04 | Período gratuito | Conductor aprobado en T0 | 30 días desde la aprobación; no se cobra activación dentro del período según la regla del producto | Registro de aprobación y pruebas de fechas |
| PAY-05 | Pago no confirmado o rechazado | Pago pendiente/rechazado | No activa el período de 24 h | Estado antes/después |
| TRIP-01 | Pasajero publica solicitud | Cuenta PASAJERO | No aparece campo para fijar precio; teléfono procede del perfil de forma controlada, no de texto libre | Capturas y documento de prueba |
| TRIP-02 | Conductor crea oferta | Solicitud existente | La oferta contiene ID de solicitud y UID del conductor; no es una publicación independiente sin vínculo | Documento de oferta |
| TRIP-03 | Oferta para solicitud inexistente/cerrada | Cuenta CONDUCTOR | Backend/reglas rechazan oferta inválida | Prueba denegada |
| CHAT-01 | Chat antes de oferta | Conductor no ha ofertado | Sin acceso al chat ni notificaciones de esa solicitud | Prueba de acceso/notificación |
| CHAT-02 | Chat asociado a oferta | Oferta válida y solicitud correspondiente | Chat pertenece a una oferta y solicitud específicas; participantes limitados a pasajeros/conductor correctos | IDs y prueba de acceso |
| NOTIF-01 | Notificación a conductor no vinculado | Nueva actividad en solicitud | No recibe notificación de solicitud si no ha presentado oferta asociada, según requisito del producto | Registro de FCM de prueba |
| ROLE-01 | Conductor pendiente de aprobación | Cuenta CONDUCTOR no aprobada | No puede publicar ofertas ni activar acceso de pago | Pruebas de reglas/backend |
| LANG-01 | Idiomas disponibles | Instalación limpia | Kazajo, ruso, español e inglés disponibles; kazajo predeterminado | Capturas de cada idioma |
| REG-01 | Campos comerciales prohibidos | Revisión UI y modelo | No existe conductor favorito ni campos de capacidad de pasajeros/equipaje | Inspección UI/modelo |

## P1 — funcionamiento y recuperación

| ID | Prueba | Resultado esperado |
|---|---|---|
| REL-01 | Conexión perdida al crear solicitud/oferta | Mensaje claro; no se duplica la operación al reintentar |
| REL-02 | Dos conductores ofertan a la vez | Las ofertas se guardan por separado y con IDs únicos |
| REL-03 | Pasajero acepta una oferta | La oferta aceptada queda vinculada a la solicitud; estados incompatibles se bloquean |
| REL-04 | Solicitud cancelada/caducada | No acepta nuevas ofertas ni permite usar chat como si siguiera activa |
| REL-05 | Cuenta eliminada/desactivada | Se aplica el proceso documentado de eliminación/retención de datos |
| REL-06 | Restauración de respaldo | Se prueba en entorno separado, con acceso restringido y datos sintéticos |
| REL-07 | Cambios de reglas Firebase | Se ejecuta emulador o conjunto automatizado de reglas antes de desplegar |

## Datos de prueba y privacidad
- Usar exclusivamente cuentas, teléfonos y viajes ficticios/controlados.
- No copiar documentos reales de usuarios a tickets, capturas, logs ni repositorios.
- No guardar secretos, tokens, claves privadas ni archivos de servicio en Git.
- Los resultados deben identificar versión/commit, fecha, entorno, ejecutor y aprobado/fallido.
- Si una prueba no se puede ejecutar, marcarla «NO EJECUTADA», nunca «APROBADA».

## Orden recomendado
1. Congelar y registrar SHA del APK que se pretende lanzar y su firma.
2. Identificar el commit de fuente que produjo ese APK, o documentar que no se puede demostrar.
3. Exportar/inspeccionar configuración real de Firebase y respaldar antes de cualquier cambio.
4. Ejecutar AUTH-01 a AUTH-04 y DATA-01 a DATA-05 en entorno de pruebas.
5. Corregir backend, esquema y reglas en rama separada; volver a ejecutar pruebas negativas y positivas.
6. Ejecutar PAY-01 a PAY-05, después TRIP/CHAT/NOTIF y los cuatro idiomas.
7. Hacer revisión legal local y aprobación explícita del plan de migración antes de cualquier cambio de región.
8. Solo entonces preparar build firmado de candidato y plan de reversión. No fusionar ni desplegar por el mero hecho de que compile.

## Estado actual
No se ha compilado la rama, no se han ejecutado estas pruebas, no se ha inspeccionado la APK firmada y no se han desplegado reglas ni cambios a Firebase. Este documento define criterios de aceptación, no certifica que el sistema los cumpla.
