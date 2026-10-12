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
