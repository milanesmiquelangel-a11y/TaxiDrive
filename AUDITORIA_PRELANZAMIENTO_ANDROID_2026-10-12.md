# TaxiDrive Android — auditoría de continuidad y lanzamiento
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

### A-04. Archivos de configuración y código fuente Android por verificar
El README menciona `.github/workflows/android.yml` y módulos `passenger-app/` y `driver-app/`, pero el conector de repositorio no pudo recuperar el archivo de workflow en esa ruta ni los archivos fuente Android usando las rutas probadas. Esto no demuestra que falten en GitHub; significa que el árbol exacto y las rutas aún deben verificarse mediante listado completo o ZIP fuente. No crear reglas Firebase ni editar código Android basándose en rutas supuestas.

### A-05. Requisito de localización de datos personales
El artículo 12(2) de la Ley de Datos Personales de Kazajistán establece que el almacenamiento debe hacerse en una base y/o objeto digital ubicado en Kazajistán. La ficha oficial consultada señala una reforma de 24-06-2026 con entrada en vigor diferida. Las regiones Firebase mostradas anteriormente por el propietario estaban fuera de Kazajistán. Antes de usar datos reales, se debe confirmar con abogado local el texto consolidado vigente, el alcance del almacenamiento y los flujos internacionales, y diseñar una arquitectura compatible. No migrar ni borrar recursos sin plan de respaldo y migración.

## Condiciones de la lógica comercial que no deben cambiar
- Tarifa de activación: 500 ₸.
- Duración de la activación: 24 horas desde la confirmación válida.
- Prueba gratuita: 30 días desde la aprobación del conductor.
- Idiomas: kazajo, ruso, español e inglés; el kazajo es el idioma predeterminado de TaxiDrive.kz.
- El pasajero publica viaje sin introducir precio; los conductores envían ofertas.
- Un conductor no debe ver teléfonos ni acceder al chat de una solicitud antes de presentar una oferta asociada.
- No añadir función de conductor favorito ni campos de capacidad de pasajeros/equipaje.

## Próximas verificaciones técnicas
1. Obtener/listar el árbol exacto de la rama principal y localizar los Gradle files, reglas Firebase, workflow de CI y fuente que generó la APK verificada.
2. Ejecutar CI/compilación y pruebas unitarias, luego pruebas de reglas Firebase en emulador.
3. Comprobar que la activación de 24 horas solo pueda concederse por servidor confiable o por acción administrativa autorizada, nunca por escritura libre del cliente.
4. Comprobar que las lecturas de solicitudes y ofertas no revelen teléfonos ni notas a conductores sin oferta y que el chat solo admita participantes autorizados.
5. Verificar política de privacidad, consentimiento, eliminación de cuenta y documentación contractual con identidad real del operador.
6. No desplegar a producción ni fusionar cambios de seguridad hasta completar pruebas y plan de reversión.

## Fuente oficial consultada
- Ley de la República de Kazajistán «О персональных данных и их защите», artículo 12: https://old.adilet.zan.kz/rus/docs/Z1300000094

## Decisión
La rama principal se mantiene intacta. Esta auditoría no introduce cambios en la lógica de la aplicación ni en Firebase. El lanzamiento sigue bloqueado hasta verificar la versión Android exacta, resolver la localización de datos y completar la revisión de seguridad y jurídica.
