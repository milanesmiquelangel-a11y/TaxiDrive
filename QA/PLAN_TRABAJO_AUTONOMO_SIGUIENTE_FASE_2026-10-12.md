# TaxiDrive.kz — plan de trabajo autónomo, siguiente fase
Fecha: 2026-10-12
Rama de trabajo: `audit/legal-readiness-2026-10`

## Objetivo
Avanzar sin pedir al usuario tareas que se puedan resolver desde el repositorio. Mantener el producto fuera de producción hasta cerrar autenticación real, autorización del servidor y privacidad de datos.

## Estado verificado en el código revisado
- La pantalla de inicio permite elegir pasajero, conductor o administrador sin que el inicio de sesión cree una sesión de Firebase Authentication.
- El formulario de pasajero ya no solicita precio ni teléfono manual; usa el UID de Firebase Auth y falla de forma cerrada si no hay sesión. Con el flujo de login actual, publicar no funcionará hasta integrar autenticación real.
- La pantalla de conductor comprueba UID autenticado, rol, aprobación y vigencia de prueba/activación antes de publicar. Estas comprobaciones de interfaz no sustituyen las reglas de Firestore ni la validación del servidor.
- El conductor todavía publica una oferta como registro independiente con origen/destino/precio; el flujo no obliga a seleccionar una solicitud concreta. Esto impide garantizar la regla de que chat, notificaciones y contacto estén vinculados a una oferta de una solicitud específica.
- El cliente todavía incluye rutas para escribir datos de aprobación/activación; esa autoridad debe trasladarse a un backend confiable.
- No se ha ejecutado una compilación Android en este entorno y no se ha desplegado nada a Firebase.

## Orden de ejecución
1. **No romper producción:** trabajar solo en esta rama; no desplegar Firebase ni cambiar reglas activas sin revisión explícita.
2. **Autenticación real:** conectar las pantallas a Firebase Authentication y eliminar la identidad simulada basada en teléfono. No fingir verificación por SMS ni generar cargos. Antes de habilitar SMS, revisar con el propietario los métodos de acceso y posibles costes en Firebase Console.
3. **Modelo solicitud → oferta:** definir IDs estables para la solicitud y la oferta; una oferta debe contener `requestId`, `passengerUid` y `driverUid` derivados del contexto autenticado, nunca confiar en IDs suministrados libremente por el cliente.
4. **Separación de datos privados:** mantener los datos de búsqueda visibles en una solicitud pública y almacenar teléfono/contacto del pasajero en un documento privado separado. Las reglas de Firestore no pueden ocultar un campo concreto dentro de un documento que ya se puede leer.
5. **Autorización del servidor:** mover aprobación de conductores, comienzo de la prueba gratuita, confirmación de pago y activación de 24 horas a una función de confianza. No aceptar del cliente el rol ADMIN, la aprobación ni el estado de pago como autoridad.
6. **Acceso a chat/notificaciones:** permitir acceso solo a los participantes autorizados de la solicitud/oferta correspondiente y revocarlo cuando corresponda. No enviar notificaciones generales ni exponer el contacto a conductores que no hayan ofertado.
7. **Pruebas de seguridad:** ejecutar la matriz de QA con dos cuentas de conductor, una de pasajero y una de administrador; incluir intentos de leer solicitudes/contactos ajenos y modificar campos privilegiados.
8. **Comprobación de despliegue y datos:** verificar con el propietario la configuración real de Firebase, las reglas activas y la ubicación de almacenamiento antes de publicar. La región de los datos es un bloqueo legal que no se resuelve con cambios de interfaz.

## Criterios para declarar la fase terminada
- Login crea una sesión real y la identidad de la app coincide con Firebase Auth.
- No hay ofertas huérfanas: cada oferta referencia una solicitud concreta.
- El teléfono no está en documentos legibles por conductores no autorizados.
- Un cliente no puede aprobar conductores, marcar pagos como confirmados ni extender activaciones.
- Las pruebas negativas de reglas pasan.
- Build y pruebas quedan documentados con resultados reales; no se declara compilado si no se ejecutó.
- No se despliega hasta que el propietario autorice el cambio y se confirme la configuración de producción.

## Bloqueos que requieren al propietario
Solo se pedirá intervención cuando sea imprescindible entrar en la consola de Firebase para verificar Authentication, reglas/regiones o billing. No compartir contraseñas ni claves privadas por el chat.
