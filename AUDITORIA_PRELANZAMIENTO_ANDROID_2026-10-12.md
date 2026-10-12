# TaxiDrive Android — auditoría de continuidad, titularidad y lanzamiento
Fecha: 2026-10-12
Rama: audit/legal-readiness-2026-10
Estado: diagnóstico documental; no desplegar ni declarar lanzamiento listo.

## Hallazgos confirmados

### A-01. Hay dos repositorios distintos
- `milanesmiquelangel-a11y/TaxiDrive`: repositorio público descrito como Kotlin, Jetpack Compose y Firebase.
- `milanesmiquelangel-a11y/TaxiDrive.kz`: repositorio privado que contiene una app iOS SwiftUI y configuración Firebase.
No se deben mezclar árboles de código ni asumir que una corrección en el repositorio iOS corrige la APK Android.

### A-02. Existe una discrepancia documentada entre APK probada y paquete fuente anterior
El archivo `RELEASES/TaxiDrive_2026-09-27_VERIFICADA.md` registra una APK probada manualmente con tarifa de 500 ₸/24 horas y flujos de registro, aprobación, período gratuito de un mes, solicitudes, ofertas, aceptación, chat, notificaciones, panel administrativo y cuatro idiomas.
Ese mismo documento indica que un paquete de fuente del 02/09/2026 aún tenía referencias a 200 ₸ y que no se sustituyó el árbol principal con ese paquete para evitar desincronización.
Conclusión: no cambiar la lógica comercial ni reemplazar el árbol hasta identificar el código que corresponde exactamente a la APK probada.

### A-03. La documentación no sustituye una prueba reproducible
La ficha de lanzamiento indica que los flujos fueron comprobados manualmente antes de actualizar el repositorio. Esta revisión no ha reproducido esas pruebas, no ha compilado el proyecto y no ha inspeccionado la APK firmada. Por tanto, el estado se considera documentado, no revalidado de forma independiente.

### A-04. El árbol Android sí se recuperó; la configuración de seguridad no aparece
La rama contiene `TaxiDrive_v2_beta/` y `Taxidrive_v1/`, con archivos Gradle, Kotlin y `google-services.json`. En el árbol revisado no aparecen `firestore.rules`, `storage.rules`, `firebase.json` ni `.github/workflows/android.yml`, aunque el README los menciona. No inventar reglas ni editar rutas supuestas.

### A-05. Hallazgos críticos en el código fuente recuperado
La inspección de `TaxiDrive_v2_beta` encontró los siguientes riesgos en el código consultado:
- `PassengerScreen.kt` pide un precio al pasajero, contrario al requisito de producto de que el pasajero no fije el precio; además crea la solicitud con `phone = ""` y `userId = ""`.
- `DriverScreen.kt` publica ofertas en la colección `ofertas` como publicaciones independientes, no como oferta vinculada a un identificador de solicitud concreta.
- `FeedRepository.kt` ejecuta lecturas completas de `solicitudes` y `ofertas`, sin filtros por relación entre conductor, oferta y solicitud en el cliente. La autorización efectiva depende de reglas Firebase, que no están presentes en el árbol revisado.
- `UserRepository.kt` concede aprobación del conductor y activación de 24 horas mediante escrituras directas del cliente. `AdminScreen.kt` llama estas operaciones y presenta la activación como «Confirmar activación (prueba)». Sin reglas estrictas o backend confiable, un cliente modificado podría intentar alterar estos campos.
- El modelo `TripPost` agrupa precio, comentario, teléfono y userId en el mismo documento. No debe permitirse a conductores leer documentos que contengan datos privados si no cumplen la condición de oferta asociada; Firestore no permite ocultar campos individuales dentro de un documento ya legible.
Estos son hallazgos del árbol consultado, no una afirmación de que el APK verificado tenga exactamente el mismo código. No se han probado las reglas de producción ni se ha compilado este árbol.

- `LoginScreen.kt` no invoca el método de autenticación de Firebase: acepta un teléfono escrito y llama a `loginCorrecto`. `MainActivity.kt` permite navegar a las pantallas de pasajero, conductor y administrador sin validar sesión ni rol. `RegisterScreen.kt` usa el teléfono como UID de Firestore y registra al usuario sin crear una sesión autenticada. En el código consultado, esto no demuestra identidad del titular del número y deja las pantallas confiando en navegación del cliente. Debe considerarse un bloqueo crítico de autenticación/autorización. No publicar ni usar esta rama con datos reales hasta corregirlo y probar reglas/backend.

### A-06. Titularidad prevista: ИП de Irina Vladimirovna Beloglazova
El propietario indica que TaxiDrive.kz se organizará mediante un ИП a nombre de su esposa, Irina Vladimirovna Beloglazova, ciudadana kazaja, todavía pendiente de registro. Es una estructura prevista, no una inscripción ya confirmada.
- El ИП, sus obligaciones fiscales, cuentas de cobro y contratos deben estar realmente a nombre de Irina, con su conocimiento y control; no basta con poner su nombre en la web.
- Antes de operar, conviene acordar por escrito funciones, acceso a cuentas, titularidad del código/marca, reparto de ingresos y qué ocurre si la colaboración termina.
- No publicar en este repositorio público su número de identificación, domicilio, datos bancarios u otra información personal innecesaria.
- Confirmar con asesor local el código de actividad económica (ОКЭД), régimen fiscal aplicable en 2026, tratamiento de los 500 ₸ cobrados a conductores, recibos/caja cuando corresponda y si la actividad requiere notificación o registro adicional.
- No presentar a TaxiDrive como transportista si el modelo es de intermediación. Un asesor debe confirmar si las funciones reales de la plataforma la sitúan dentro de las reglas de servicios de información/despacho de taxi y qué contratos/avisos corresponden.

### A-07. Localización de datos personales — bloqueo de lanzamiento
El artículo 12(2) de la Ley de Datos Personales de Kazajistán establece que los datos personales deben almacenarse en una base ubicada en Kazajistán. La redacción oficial consultada también muestra reformas con fechas de entrada en vigor diferida. Las regiones Firebase mostradas anteriormente por el propietario estaban fuera de Kazajistán.
Antes de usar datos reales, confirmar con asesor local el texto vigente, alcance y transferencias internacionales, y diseñar una arquitectura compatible. No migrar ni borrar recursos sin inventario, copia de seguridad y plan de migración. No asumir que el consentimiento por sí solo resuelve el requisito territorial.

## Condiciones de la lógica comercial que no deben cambiar
- Tarifa de activación: 500 ₸.
- Duración de la activación: exactamente 24 horas desde la confirmación válida.
- Prueba gratuita: 30 días desde la aprobación del conductor.
- Idiomas: kazajo, ruso, español e inglés; el kazajo es el idioma predeterminado de TaxiDrive.kz.
- El pasajero publica viaje sin introducir precio; los conductores envían ofertas vinculadas a una solicitud concreta.
- Un conductor no debe ver teléfonos ni acceder al chat de una solicitud antes de presentar una oferta asociada.
- No añadir función de conductor favorito ni campos de capacidad de pasajeros/equipaje.

## Próximas verificaciones técnicas y legales
1. Identificar el commit/paquete fuente exacto que corresponde a la APK probada con tarifa de 500 ₸.
2. Recuperar las reglas Firebase reales, revisar la región de cada servicio y definir migración con respaldo si es necesaria.
3. Crear pruebas de reglas Firebase con usuarios pasajero, conductor sin oferta, conductor con oferta, conductor aprobado, administrador y usuario no autenticado.
4. Hacer que la aprobación y la activación solo se concedan mediante backend confiable o una acción administrativa realmente autorizada; la activación debe ser idempotente y durar 24 horas desde la confirmación válida.
5. Separar los datos públicos del viaje de la información privada de contacto, o aplicar un diseño equivalente que impida lecturas no autorizadas.
6. Confirmar alcance regulatorio de plataforma de información/despacho, ОКЭД, régimen fiscal, recibos, contratos y avisos con asesor local antes de cobrar o lanzar.
7. Preparar términos, política de privacidad, consentimientos y eliminación de cuenta con la identidad empresarial correcta después del registro del ИП.
8. No desplegar a producción ni fusionar cambios de seguridad hasta completar pruebas y plan de reversión.

## Fuentes oficiales consultadas
- Ley de la República de Kazajistán «О персональных данных и их защите», artículo 12: https://old.adilet.zan.kz/rus/docs/Z1300000094
- Portal oficial sobre regímenes fiscales de Kazajistán para 2026: https://www.gov.kz/situations/814/intro?lang=en
- Información oficial sobre actividad empresarial en Kazajistán: https://www.gov.kz/article/64584?lang=en

## Decisión
La rama principal se mantiene intacta. Esta actualización amplía la auditoría documental; no cambia la lógica de la app ni Firebase. El lanzamiento sigue bloqueado hasta verificar la versión Android exacta, resolver la localización de datos, confirmar las obligaciones del ИП y completar la revisión de seguridad y jurídica.
