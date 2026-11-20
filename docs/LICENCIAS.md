# Licencias y procedencia — 20 de noviembre de 2026

La app usa el mismo **mGBA 0.10.5** de BRUMA 1.0.3/46. No se ha cambiado ningún archivo del motor copiado. `REUSE-LOCK.json`, `M-GBA-SOURCE-HASHES.json` y `NATIVE-COMPILED-UNITS.json` documentan procedencia, hashes y compilación efectiva.

| Componente distribuido | Licencia | Tratamiento |
|---|---|---|
| Frontend nuevo y código adaptado de BRUMA | GPL-3.0-or-later | Se conserva GPL; fuentes completas, instrucciones y avisos junto al APK. No se presenta como código propietario. |
| mGBA 0.10.5 | MPL-2.0 | Fuentes y avisos originales. La sección 3.3 contempla combinación con licencias secundarias; no se ha añadido la declaración de incompatibilidad del Exhibit B a archivos del motor. Se mantienen los derechos MPL originales. |
| blip_buf 1.1.0, enlazado estáticamente | LGPL-2.1-or-later | Texto de licencia, código y receta completa para modificar y volver a enlazar. La distribución íntegra GPLv3 permite reconstruir el conjunto; no depende de una clave de firma propietaria para generar una variante propia. |
| inih | BSD-3-Clause | Copyright y condiciones originales conservados. |
| kotlin-stdlib 2.2.10 y JetBrains annotations 13.0 | Apache-2.0 | Licencia y atribución JetBrains. Declaraciones contrastadas con POMs de los artefactos usados; el NOTICE upstream de Kotlin se incluye como aviso suplementario del compilador. |
| libc++/compiler-rt del NDK 28.2.13676358 | Avisos LLVM/Apache-2.0 con excepción y avisos permisivos del runtime | Se incluyen los avisos oficiales del NDK. El archivo amplio de avisos no significa que se empaqueten todas las herramientas o bibliotecas que menciona. |

El código propio de BRUMA reutilizado ya declara GPLv3+. La ausencia de RPG Maker en Pocket **no elimina** esas obligaciones. GPLv3 es la opción de conjunto adoptada; no se relicencia unilateralmente cada biblioteca como GPL.

El APK no incorpora SDKs de Google, cuentas, DRM, referidos ni nuevos servicios propietarios. AGP, Gradle y el compilador son herramientas de desarrollo, no SDKs de Google integrados en el producto. Se conserva el wrapper abierto; no se incluyen el SDK/JDK completos.

## Carátulas: revisión pendiente antes de publicar

El cliente es código GPL, pero las imágenes no adquieren esa licencia. El servidor histórico de Libretro devolvió 404 durante la prueba. Se ha añadido como alternativa su repositorio oficial `libretro-thumbnails` en GitHub mediante HTTPS y API pública sin token. No se empaquetan imágenes comerciales, ni siquiera en las capturas entregadas.

No se ha encontrado una autorización general que acredite todos los derechos de explotación de las portadas. Antes de lanzamiento comercial debe confirmarse permiso/condiciones del proveedor y de las imágenes, o usar otro proveedor autorizado y las carátulas personalizadas. Este punto sigue abierto; no se afirma que tener un repositorio público autorice su explotación.

## Fuentes oficiales consultadas

- [mGBA: licencia MPL-2.0](https://mgba.io/faq.html)
- [MPL-2.0, texto y sección 3.3](https://www.mozilla.org/en-US/MPL/2.0/)
- [MPL: preguntas frecuentes de compatibilidad](https://www.mozilla.org/en-US/MPL/2.0/FAQ/)
- [Kotlin 2.2.10: Apache-2.0](https://github.com/JetBrains/kotlin/blob/v2.2.10/license/LICENSE.txt)
- [Kotlin: aviso upstream](https://github.com/JetBrains/kotlin/blob/v2.2.10/license/NOTICE.txt)
- [Catálogo de carátulas upstream GBA](https://github.com/libretro-thumbnails/Nintendo_-_Game_Boy_Advance)

Los textos exactos de mGBA, blip_buf e inih están en el árbol vendorizado y se muestran también en Ajustes → Licencias. Este informe es un inventario técnico de la distribución, no una garantía de titularidad sobre material de terceros.
