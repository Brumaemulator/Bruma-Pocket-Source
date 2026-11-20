# Bruma Pocket 0.1.3 — prueba independiente

20 de noviembre de 2026. Package com.brumastudio.pocket, versionCode 4, Android 8+, ARM64; firma Android Debug independiente. No publicación.

- Filtros GBA → GBC → GB → favoritos; GBA inicial y sin Todos. Favoritos abarca todos los sistemas.
- Botón circular de doble flecha en los controles táctiles: activa/desactiva el avance rápido, refleja su estado y admite mover su posición igual que los demás botones. Respeta la velocidad 2×/4× y la preferencia de sonido existentes. Cancelar el toque o arrastrarlo en modo edición no activa la velocidad.
- Avance rápido incluido como acción remapeable del mando físico. Una pulsación conmuta una vez; mantener pulsado no repite ni introduce bits ajenos en las teclas del emulador.
- Confirmación de carátulas abreviada a Descargar carátulas, con Descargar/Cancelar y enlace a Privacidad. La explicación de datos sigue disponible expresamente; no se inicia la descarga al abrir el aviso.
- Ajustes agrupa Acerca de, licencias completas y privacidad en un enlace discreto Información legal al final. No se eliminan avisos ni textos de licencias, y el enlace es enfocable y legible.
- Nuevas etiquetas localizadas en los nueve idiomas existentes; sin SDKs ni dependencias nuevas.

33 comprobaciones superadas en AOSP Android 15 x86_64 con diagnósticos propios, incluidas todas las regresiones de juegos, guardados, .sav, ZIP y almacenamiento. Nuevas verificaciones: orden de pestañas y selección GBA, favoritos entre sistemas, atajo táctil y cancelación, movimiento sin activación, asignación de avance rápido con pulsaciones repetidas y acceso al contenido legal desde el enlace agrupado. Compilación ARM64 y lint completados. Actualización en el móvil real sin desinstalar; comprobado orden de filtros y confirmación abreviada de carátulas.

No se ha usado un mando físico USB/Bluetooth en esta iteración; la asignación y recepción se verificaron con eventos Android sintéticos. BRUMA Emulator y producción no se modifican.
