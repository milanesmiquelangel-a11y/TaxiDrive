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

### A-04. Árbol Android localizado; configuración de seguridad no localizada
El árbol de la rama de auditoría sí se ha recuperado. Incluye `TaxiDrive_v2_beta/` y `Taxidrive_v1/`, con archivos Gradle, código Kotlin y `TaxiDrive_v2_beta/app/google-services.json`. En el árbol consultado no aparecen archivos `firestore.rules`, `storage.rules`, `firebase.json` ni el workflow `.github/workflows/android.yml`. El README los menciona de forma inconsistente con el árbol visible.
No se deben inventar reglas ni editar rutas supuestas. Hay que localizar la fuente exacta que generó la APK probada y recuperar las reglas reales del proyecto Firebase antes de proponer cambios.

### A-05. Titularidad prevista: ИП de Irina Vladimirovna Beloglazova
El propietario indica que TaxiDrive.kz se organizará mediante un ИП a nombre de su esposa, Irina Vladimirovna Beloglazova, ciudadana kazaja, todavía pendiente de registro. Esta es la estructura prevista, no una inscripción ya confirmada.
- El ИП, sus obligaciones fiscales, las cuentas de cobro y los contratos deben estar realmente a nombre de Irina, con su conocimiento y control; no basta con poner su nombre en la web.
- Irina sería la titular empresarial y asumiría las obligaciones que correspondan al ИП. Antes de operar, deben acordarse por escrito las funciones, acceso a cuentas, titularidad del código/marca, reparto de ingresos y qué ocurre si la colaboración termina.
- No publicar aún el nombre completo, número de identificación, domicilio, datos bancarios ni otra información personal de Irina en este repositorio público. Para documentos públicos, usar datos empresariales mínimos y solo después de la inscripción y validación.
- Confirmar con contador/abogado local el OKED, régimen fiscal aplicable en 2026, tratamiento de los 500 ₸ cobrados a conductores, recibos/caja cuando corresponda y si la actividad concreta requiere notificación o inscripción adicional.
- No presentar a TaxiDrive como transportista si el modelo es únicamente de intermediación. Aun así, la ley de transporte regula servicios de información y despacho de taxi y las reglas atribuyen funciones específicas a estos servicios. Un abogado debe confirmar si la plataforma, por el flujo real de solicitudes, ofertas, notificaciones y contacto, entra en esa categoría y qué contratos/avisos corresponden.

### A-06. Localización de datos personales — bloqueo de lanzamiento
El artículo 12(2) de la Ley de Datos Personales de Kazajistán establece almacenamiento en una base y/o objeto digital ubicado en Kazajistán. La fuente oficial consultada recoge cambios aprobados el 24-06-2026 con entrada en vigor diferida. Las regiones Firebase mostradas anteriormente por el propietario estaban fuera de Kazajistán.
Antes de utilizar datos reales, confirmar con asesor local el texto vigente, alcance y transferencias internacionales y diseñar una arquitectura compatible. No migrar ni borrar recursos sin inventario, copia de seguridad y plan de migración. El consentimiento por sí solo no debe darse por solución automática al requisito de ubicación.

### A-07. Seguridad de solicitudes y ofertas pendiente de prueba
Los repositorios y modelos todavía deben compararse con la APK real. La regla de producto obligatoria es que un conductor no vea teléfono/datos privados ni acceda al chat de una solicitud antes de presentar una oferta asociada. Las reglas Firestore deben comprobarse con el modelo real y pruebas automatizadas; no basta con ocultar elementos en la interfaz. Como Firestore no permite ocultar campos individuales de un documento al lector autorizado, puede ser necesario separar datos públicos del viaje y datos privados de contacto.

## Condiciones de la lógica comercial que no deben cambiar
- Tarifa de activación: 500 ₸.
- Duración de la activación: exactamente 24 horas desde la confirmación válida.
- Prueba gratuita: 30 días desde la aprobación del conductor.
- Idiomas: kazajo, ruso, español e inglés; el kazajo es el idioma predeterminado de TaxiDrive.kz.
- El pasajero publica viaje sin introducir precio; los conductores envían ofertas.
- Un conductor no debe ver teléfonos ni acceder al chat de una solicitud antes de presentar una oferta asociada.
- No añadir función de conductor favorito ni campos de capacidad de pasajeros/equipaje.

## Próximas verificaciones técnicas y legales
1. Identificar el commit/paquete fuente exacto que corresponde a la APK probada con tarifa de 500 ₸.
2. Recuperar las reglas Firebase reales, revisar la región de cada servicio y definir una migración con respaldo si es necesaria.
3. Ejecutar CI/compilación y pruebas unitarias; luego probar reglas Firebase en emulador.
4. Verificar que la activación de 24 horas solo pueda concederse por servidor confiable o acción administrativa autorizada, nunca por escritura libre del cliente.
5. Comprobar que las lecturas de solicitudes/ofertas no revelen datos privados y que el chat solo admita participantes autorizados.
6. Confirmar el alcance regulatorio de una plataforma de información/despacho, OKED, régimen fiscal, recibos, contratos y avisos con asesor local antes de cobrar o lanzar.
7. Preparar términos, política de privacidad, consentimientos y eliminación de cuenta con la identidad empresarial correcta, después del registro del ИП.
8. No desplegar a producción ni fusionar cambios de seguridad hasta completar pruebas y plan de reversión.

## Fuentes oficiales consultadas
- Ley de la República de Kazajistán «О персональных данных и их защите», artículo 12: https://old.adilet.zan.kz/rus/docs/Z1300000094
- Reglas de transporte de pasajeros y equipaje por carretera, incluidas disposiciones sobre notificación y servicios de información/despacho: https://old.adilet.zan.kz/eng/docs/V1500011550
- Información oficial sobre regímenes fiscales de Kazajistán en 2026: https://www.gov.kz/article/64584?lang=en

## Decisión
La rama principal se mantiene intacta. Esta actualización solo amplía la auditoría documental; no cambia la lógica de la app ni Firebase. El lanzamiento sigue bloqueado hasta verificar la versión Android exacta, resolver la localización de datos, confirmar las obligaciones del ИП y completar la revisión de seguridad y jurídica.
