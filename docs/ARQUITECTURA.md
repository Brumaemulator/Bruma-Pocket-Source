# Arquitectura aislada

`app` contiene MainActivity (biblioteca), DetailActivity (juego/backup/carátula), PlayerActivity (sesión emulada), SettingsActivity y UI nativa propia. No depende de la instalación de BRUMA. La estética nueva usa crema, salvia y lima, tipografía del sistema e ilustraciones abstractas propias.

| Módulo | Responsabilidad | Procedencia |
|---|---|---|
| bruma-core | JNI, mGBA, presentación de frames/cadencia y audio | Motor idéntico; puente adaptado para estados y escalado entero. |
| bruma-library | SAF, hashes SHA-256, ZIP, índice atómico, favoritos/recientes/tiempos | RomAccess/ArchiveCatalog adaptados; almacén nuevo y acotado a la familia GB. |
| bruma-covers | Matching, catálogo HTTPS, caché, persistencia de imágenes limitada a 768 px | Cliente BRUMA adaptado; fallback oficial GitHub y servicio de imágenes nuevos. |
| bruma-input | Mando Android y controles táctiles | Mapeo BRUMA adaptado; táctiles nuevos y posiciones separadas por orientación/sistema. |
| bruma-saves | SRAM, estados automáticos/manuales, backups ZIP íntegros | Serializador de mGBA; gestión e importación segura nuevas. |

Las clases nativas conservan el namespace Kotlin `com.linkcore.emulator` para preservar símbolos JNI reutilizados; **no es el applicationId**. El package instalado es `com.brumastudio.pocket`. No hay sharedUserId ni datos compartidos con BRUMA.

Esta extracción vive en el proyecto nuevo. No se ha migrado ni enlazado el proyecto de BRUMA contra estos módulos. Se deja una frontera reutilizable para una futura migración expresa, evitando arriesgar la app publicada.

## Almacenamiento y sesiones

El escaneo recorre documentos SAF y calcula hashes de los contenidos. Deduplica por contenido, conserva preferencias por SHA-256 y prefiere ROM directa frente a la misma dentro de ZIP. Solo persiste metadatos. Una segunda revisión reutiliza huellas cuando tamaño/fecha son fiables. Una cancelación no sustituye el índice anterior. No hay importación masiva de ROMs.

Al ejecutar un juego se intenta un descriptor de lectura; cuando el proveedor/SELinux no permite reabrirlo, se crea una copia **temporal del juego activo**. Un ZIP se extrae solo para la sesión. Al terminar se libera; los restos de procesos abandonados se limpian al siguiente arranque. El original nunca se modifica. Las partidas y estados son datos separados bajo `files/saves`.

PlayerActivity posee la sesión nativa de forma exclusiva. Un cambio rápido de actividad retira primero al propietario anterior para evitar cerrar un core que ya pertenece a otra sesión. Se guarda SRAM periódicamente y al pausar, y estado automático al pausar. Guardar estados rota un backup anterior; importar backups verifica juego, formato, rutas, límites y SHA-256 de todos los archivos antes de reemplazar datos.

## Red y privacidad

Solo hay red para carátulas opcionales mediante HTTPS: servidor de Libretro, API pública de GitHub y raw.githubusercontent.com limitados a repositorios GB/GBC/GBA de libretro-thumbnails. Sin tokens, redirecciones o hosts arbitrarios. Catálogo cacheado siete días; imágenes con límites de bytes/dimensiones. Puede haber límites de uso externos, ausencia de portada o cambios del proveedor. La emulación/biblioteca/guardados funcionan offline.

El enlace de promoción a BRUMA usa market:// y fallback HTTPS; si falta también navegador se copia el enlace. No desbloquea ni bloquea funciones. No hay backend, identidad, DRM ni referidos en Pocket.

## Traducciones

Solo existe el contrato de frontera `TranslationAvailability`. No hay descarga/aplicación de parches en esta fase, ni se muestra un botón que finja hacerlo. El acceso de BRUMA al backend está vinculado a su configuración/identidad y exige una integración independiente autorizada, con SHA-256 de original/parche, límites y reversión. No se han modificado la web, el catálogo ni las credenciales actuales.

## Mejoras futuras deliberadamente fuera

Rewind, shaders/LCD, cheats, enlace multiplayer, perfiles de emulación por juego y metadata de año/género/editor. La primera fase no inventa metadatos ni añade opciones que no funcionen. La BIOS HLE de mGBA evita distribuir BIOS propietarias.
