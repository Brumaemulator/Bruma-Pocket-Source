# Bruma Pocket 1.0.0 / 6

Primera candidata para Google Play, gratuita e independiente: com.brumastudio.pocket, minSdk 26, targetSdk 36, arm64-v8a. No sustituye BRUMA Emulator. Sin SDKs de Google/TapTap, cuentas, referidos, compras o anuncios. La promoción de BRUMA Emulator está en una opción explícita del menú principal.

Incluye la corrección de escala: proporción original, escala entera con márgenes y exclusión de estiramiento. Filtros GBA/GBC/GB/favoritos, controles movibles por orientación, mandos y remapeo de avance rápido, sonido opcional en 2×/4×, importación/exportación .sav y nueve idiomas.

Release QA: 34 comprobaciones satisfactorias sobre una app release x86_64 firmada, sin debuggable, con tests firmados con la misma clave de subida de Pocket en un emulador AOSP Android 15 aislado. Diagnósticos originales: GB/GBC/GBA, imagen/audio/SRAM/estados, continuidad ZIP, .sav aceptado por decodificador de BRUMA, validación de backups, cancelación, no copias retenidas, interfaz y locales. No se usó un mando USB/Bluetooth físico en esta iteración; el remapeo se verificó con eventos sintéticos. Las variantes anteriores se habían probado en el móvil ARM64; la release no se instaló encima de su app debug porque tienen firmas distintas. No se han borrado partidas del móvil.

La distribución ARM64 final es no depurable, solo pide Internet y vibración, desactiva backup automático y tráfico HTTP claro. Los segmentos LOAD de liblinkcore.so están alineados a 16 KB, y el APK supera zipalign -P16. La comprobación de alineación no equivale a haber ejecutado esta release en hardware físico de 16 KB.

Fuentes completas y receta de compilación incluidas. Claves privadas fuera de este proyecto y de las fuentes. Fuente original de mGBA 0.10.5 sin cambios; cada componente conserva sus avisos y licencia. Assets de ficha y capturas generados con material de demostración propio, no incluidos en el producto.

Pendientes manuales: crear ficha nueva, configurar Play App Signing, completar declaraciones y probar desde Google Play antes de solicitar producción. No se garantiza aprobación automática. Las portadas externas no adquieren licencia por estar en un repositorio público; no se ha acreditado una autorización general de todos sus titulares. Esta revisión de derechos/condiciones permanece como limitación antes de publicación pública.

Privacidad: https://brumastudio.netlify.app/bruma-pocket/privacidad
Licencias: https://brumastudio.netlify.app/bruma-pocket/licencias
Fuentes: https://github.com/Brumaemulator/Bruma-Pocket-Source/releases/tag/v1.0.0
