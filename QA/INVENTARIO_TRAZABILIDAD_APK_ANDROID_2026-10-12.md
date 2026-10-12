# TaxiDrive Android — inventario de procedencia del APK
Fecha: 2026-10-12
Rama: audit/legal-readiness-2026-10
Estado: inspección del árbol GitHub; no se recibió ni inspeccionó un APK binario en esta revisión.

## Resultado de la inspección del árbol
Se consultó el árbol recursivo completo de la rama `audit/legal-readiness-2026-10` (respuesta sin truncamiento).

- APK/AAB incluidos en Git: **ninguno**.
- Wrapper de Gradle: sí, para `TaxiDrive_v2_beta` y `Taxidrive_v1`.
- Configuración versionada de Firebase CLI (`firebase.json`, `.firebaserc`) y reglas (`firestore.rules`, `storage.rules`): **no aparecen** en este árbol.
- Workflow de GitHub Actions bajo `.github/workflows/`: **no aparece**.
- Pruebas existentes: solo los ejemplos de plantilla `ExampleUnitTest.kt` y `ExampleInstrumentedTest.kt` en los dos proyectos; no se identificaron suites de pruebas funcionales o de reglas en este árbol.

## Lo que esto permite concluir
1. El repositorio no aporta el APK binario que permita calcular su SHA-256, inspeccionar su certificado de firma o comparar su código con una compilación reproducible.
2. El registro de versión verificada del 27/09/2026 documenta pruebas manuales, pero no incluye en ese archivo una huella criptográfica del APK ni un SHA del commit fuente asociado.
3. La presencia de `gradlew` permite intentar una compilación si se dispone de un entorno compatible y de dependencias; no prueba que el código compile. En esta sesión no se ejecutó Gradle.
4. La ausencia de reglas en el repositorio no significa que Firebase Console no tenga reglas activas. Las reglas reales deben exportarse/consultarse desde el proyecto Firebase con acceso autorizado antes de modificar nada.
5. No hay evidencia suficiente para afirmar que `TaxiDrive_v2_beta` o `Taxidrive_v1` sea el origen exacto del APK que se probó con 500 ₸/24 h.

## Evidencia mínima para cerrar la trazabilidad
- Archivo APK exacto que se probó, sin volver a compilarlo ni modificarlo.
- SHA-256 del APK y certificado/fingerprint de firma.
- Fecha, dispositivo y versión instalada; confirmar que la interfaz muestra 500 ₸ y 24 horas.
- Commit/tag del código fuente que produjo el APK y, si existe, registro de compilación.
- Exportación respaldada de las reglas y configuración actuales de Firebase, sin publicar credenciales o datos personales.
- Confirmación de que el APK no apunta a un proyecto Firebase de pruebas o de otra versión.

## Siguiente paso seguro
No corregir todavía el flujo de autenticación en este árbol, porque la APK probada puede proceder de otra fuente. Primero hay que recuperar la fuente exacta y las reglas reales. Cualquier corrección posterior se hará en una rama separada, con pruebas negativas/positivas y revisión antes de fusionar.

## Estado de acciones
- Inspección de árbol remoto: realizada.
- APK binario y certificado: no disponibles en el árbol inspeccionado.
- Compilación local/CI: no ejecutada.
- Consulta o despliegue de Firebase: no realizado.


## Actualización tras recibir archivos del propietario (12/10/2026)

Se recibieron dos archivos en esta conversación y se inspeccionaron localmente. El APK no se ha subido a GitHub para evitar publicar un binario de lanzamiento sin necesidad.

### APK recibido
- Archivo: `app-release.apk`.
- Tamaño aproximado: 16 MiB.
- SHA-256: `a7044f09d5434ac9436897e038c62faa577b70a5b772780c06b2d2af8f367273`.
- El paquete contiene DEX y recursos Android; su metadato de compilación declara Android Gradle Plugin 8.7.0.
- No se ha verificado todavía el certificado/fingerprint de firma ni se ha extraído de forma fiable el applicationId/versionName del manifiesto binario en esta sesión. No se afirma que este APK proceda del ZIP recibido.

### Fuente ZIP recibido
El archivo `TaxiDrive_LOCALIZACION_CORREGIDA.zip` sí contiene un proyecto Android más completo que el árbol del repositorio inspeccionado anteriormente:
- `app/`, `functions/`, `firestore.rules`, `storage.rules`, `firebase.json` y `firebaserc`.
- `firebase.json` enlaza las reglas locales y las Cloud Functions.
- `.firebaserc` apunta al proyecto Firebase `taxidrivekz`.
- El módulo Android declara `applicationId = kz.taxidrive.app`, `versionCode = 1`, `versionName = 1.0.0`, `targetSdk = 35`.
- `README_LANZAMIENTO.md` dice que Kaspi Pay no está integrado todavía y que el flujo de 500 ₸ queda preparado para integrarlo después. Las Cloud Functions inspeccionadas gestionan notificaciones; el paquete no muestra una integración real de cobro Kaspi.
- El ZIP no contiene `local.properties` ni una clave privada de firma, según el inventario inspeccionado.

### Hallazgo de seguridad P0: teléfono del pasajero antes de ofertar
En el ZIP:
- El modelo `SolicitudViaje.kt` incluye `pasajeroTelefono` en el documento de solicitud.
- La regla `firestore.rules`, bloque `solicitudes/{id}`, permite leer una solicitud a cualquier usuario autenticado que tenga un documento de conductor con `estadoAprobacion == 'APROBADO'`; no exige que ese conductor haya ofertado en esa solicitud.
- Como las reglas de Firestore autorizan la lectura del documento completo y no ocultan campos individuales, un conductor aprobado puede potencialmente leer `pasajeroTelefono` antes de enviar una oferta. Esto contradice el requisito del producto.
- El propio comentario de las reglas dice que el teléfono se copia a la solicitud/oferta, pero la autorización de lectura de la solicitud sigue siendo más amplia que el requisito de privacidad.
- Recomendación: antes de producción, separar los datos públicos de la solicitud de los datos privados de contacto y permitir el acceso al contacto únicamente a los participantes autorizados después de una oferta válida; añadir pruebas de reglas que demuestren la denegación a un conductor aprobado sin oferta. No se modificaron reglas ni se desplegó nada.

### Limitaciones de esta revisión
- No se ejecutó Gradle, no se instaló el APK ni se realizó prueba de interfaz en dispositivo.
- No se verificó el fingerprint de firma del APK ni se comparó su DEX con el código del ZIP.
- No se consultó Firebase Console; el contenido del ZIP no demuestra que esas reglas sean las que están desplegadas.
- No se desplegaron Firestore Rules, Storage Rules ni Cloud Functions.
- No se subieron el APK ni el ZIP al repositorio.
