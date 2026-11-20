# Entrega funcional independiente — Bruma Pocket 0.1.0

20 de noviembre de 2026. Nombre/icono provisionales. Package `com.brumastudio.pocket`, versión 0.1.0/1, Android 8.0+ (API 26), APK ARM64 con firma de pruebas. No se ha publicado nada ni se ha tocado BRUMA Emulator, BRUMA OS, sus motores, datos, web o producción.

## Hitos cerrados

1. **Inspección y licencias.** Localizado mGBA 0.10.5 en BRUMA 1.0.3/46, commit c0f00eb36b0d1062da8882a617db71b007f836d9. El mismo motor soporta GB/GBC/GBA. 2.418 archivos upstream copiados verificados byte a byte; cero cambios en el algoritmo del core. Archivos BRUMA reutilizados: GameSurface, FrameCadence, SpeedAudio, NativeCore, GamepadInput, RomAccess, ArchiveCatalog, LibretroClient y puente JNI. Procedencia/hashes en REUSE-LOCK. Se mantienen GPLv3+ y licencias de terceros.
2. **Proyecto y módulos.** Proyecto completamente nuevo con módulos core, library, covers, input y saves. ApplicationId/icono/datos independientes. No se ha convertido BRUMA en dependencia ni modificado su estructura. Fuente completa recompilable en el ZIP de entrega.
3. **Biblioteca e interfaz nueva.** Biblioteca SAF automática, ZIP con subcarpetas, deduplicación SHA-256, búsqueda, filtros, favoritos, recientes, continuar y tiempo jugado. UI Android nativa salvia/crema/lima en español/inglés, adaptable a móvil, horizontal y tablet; carátulas propias/personalizadas y matching online optativo.
4. **Emulación y partidas.** GB/GBC/GBA con vídeo/audio reales, táctiles específicos, configuración de tamaño/posición/transparencia/vibración, mando USB/Bluetooth mediante APIs Android, remapeo, ocultación táctil ante mando real, SRAM, estado automático + cuatro slots, 2×/4×, escala entera, proporción/estirar y capturas. Exportación/importación de backups con integridad, respaldo previo y protección de rutas.
5. **Robustez y entrega.** Corregidos: detección falsa de teclado como mando, carrera de propiedad del core al abrir sesiones, archivo de estado abierto con modo inadecuado para mmap, cancelación de escaneo, controles solapados con Menú en horizontal, adaptación de biblioteca horizontal y texto ampliado. Proveedor original de carátulas devolvía 404: fallback al repositorio oficial, sin claves ni SDK nuevo.

## Pruebas realizadas

- 22 comprobaciones instrumentadas PASS en AOSP Android 15 x86_64, con actividades y emulación nativa reales; no es una maqueta ni una imagen.
- Diagnósticos originales escritos para GB/GBC/GBA: frames esperados y PCM no nulo, SRAM, ranuras 0–4, reapertura desde ZIP con las mismas partidas, borrado de extracción temporal al terminar.
- Carpeta con 500 ZIP de diagnóstico adicionales: deduplicación a los tres contenidos únicos y cero importación permanente de cartuchos. Esta prueba de cantidad **no representa** 500 juegos comerciales distintos ni demuestra toda su compatibilidad.
- Backups: ida/vuelta, copia anterior, juego equivocado, rutas peligrosas y manipulación de contenido rechazada por SHA-256 sin perder el guardado.
- Cancelación conserva la biblioteca; favoritos, recientes y tiempos sobreviven al escaneo.
- Táctil A/cancelación y lógica de mando/diagonal; teclado virtual excluido.
- Carátulas: consulta real HTTPS al catálogo alternativo, matching y decodificación de imagen. La imagen de terceros se descarta en la prueba; no se entrega como asset. Imagen propia de 2048×1024 reducida a 768×384, archivo inválido no sustituye portada anterior.
- Actividades reales de biblioteca/ficha/ajustes y Player de los tres sistemas; cambio a horizontal y vuelta durante emulación GBA. Capturas en español, texto 130% y tamaño de tablet.
- Reinstalación con `adb install -r`: hashes de 37 archivos de partidas/preferencias intactos. Misma versión interna 0.1.0/1; no equivale aún a certificar una futura migración de versión pública.
- Compilación ARM64 y revisión lint sin errores; package/versión/API/ABI/firma verificados. Alineación ZIP de 16 KB comprobada; las bibliotecas ELF se revisan en el manifiesto de entrega.

Las pruebas instrumentadas usan un APK x86_64 equivalente para el emulador. El APK entregado al usuario es ARM64. **Todavía no se ha instalado ni validado esta nueva app en el móvil físico**, que se desconectó antes de la entrega. No se han probado realmente un mando Bluetooth/USB, latencia audible, temperatura ni juegos comerciales en ARM64. Eso queda para la sesión con el teléfono, sin afirmar resultados que no se han medido.

## Cambios y decisiones

Todos los archivos nuevos/adaptados están dentro de BrumaPocket. `app/src/main` contiene la experiencia nueva; los cinco módulos encapsulan la tecnología reutilizada. La lista exacta entregada está en SOURCE-MANIFEST.json dentro del ZIP. `docs/ARQUITECTURA.md` explica sesiones, almacenamiento, APIs y límites; `docs/LICENCIAS.md` conserva el inventario y enlaces primarios.

No hay ROMs ni juegos en el APK entregado, BIOS externas, SDKs Google/TapTap/Firebase, anuncios, compras, suscripciones, cuentas o referidos. No se usa la clave de firma de producción. Las capturas muestran diagnósticos originales, no juegos comerciales. La app arranca vacía en una instalación nueva.

## Pendientes antes de publicar

- Prueba física ARM64 con tus archivos, audio, suspensión/rotación prolongadas y mando real. La compilación ARM64 por sí sola no demuestra la ejecución en ese dispositivo.
- Confirmar nombre, icono, diseño final y política pública propia. Revisar los requisitos de Play al preparar esa publicación, sin reutilizar la ficha/versionado de BRUMA.
- Resolver derechos/condiciones de las imágenes del proveedor: descargar desde un repositorio público no acredita licencia de explotación. La opción de portada personalizada ya funciona.
- Traducciones: solo estudiadas y separadas mediante contrato, no integradas en esta v0.1.0.
- Rewind, cheats, shaders/LCD, metadatos externos, perfiles avanzados y BIOS opcional aportada por usuario quedan fuera de esta fase. No son botones simulados.

## Prueba sugerida al volver

Instalar el APK ARM64 independiente → elegir tu carpeta → filtrar GB/GBC/GBA → abrir un juego de cada sistema → guardar normalmente → crear estado → volver y continuar → exportar backup → probar mando cuando esté disponible. BRUMA seguirá instalada y con sus propios datos. Para este producto nuevo las partidas no se migran automáticamente desde BRUMA.
