# Bruma Pocket 0.1.2 — prueba independiente

20 de noviembre de 2026. `com.brumastudio.pocket`, versionCode 3, Android 8+, ARM64, firma de pruebas independiente.

## Cambios solicitados

- Toque en juego: diálogo compacto con Jugar, Más opciones y Continuar si existe sesión guardada. Más opciones abre la ficha completa con favoritos, carátulas, .sav y backup. No se abre la ficha automáticamente.
- Solo una tarjeta de Continuar jugando. Eliminada la tira adicional de recientes y el eslogan Pequeños clásicos. Grandes momentos.
- Panel vertical propio inspirado en una consola portátil: cruceta oscura, A/B magenta, Select/Start, L/R para GBA y marca Pocket multicolor, sin logotipos ni diseños de assets de Nintendo. En horizontal siguen disponibles los botones superpuestos.
- Botones arrastrables con posiciones independientes por orientación y familia GB/GBC o GBA. Tamaño, opacidad, restablecimiento y vibración siguen disponibles.
- Mostrar controles y Ocultar al conectar mando disponibles en ajustes globales y dentro del menú táctil del juego. El modo de edición fuerza su visibilidad; terminar con el botón Menú.
- Corregida la ocultación real de controles en el POCO: el dispositivo interno uinput-xiaomi anunciaba GAMEPAD. Los inyectores uinput no activan ocultación automática; los mandos con identidad/capacidades físicas siguen siendo reconocidos. No se altera el mapeo de los eventos.
- Sonido durante el avance rápido configurable en Ajustes y en Menú → Avance rápido. Preferencia persistente y silencio inicial por defecto. Reutiliza el remuestreador estéreo propio existente; no añade SDK ni dependencia.
- Icono vectorial original con P multicolor y letras Pocket de los mismos colores de la interfaz, sin fuentes o imágenes nuevas incorporadas.
- Todos los textos nuevos traducidos a los nueve idiomas ya disponibles.

## Pruebas

Compilación ARM64 y lint completados. Batería ampliada: 30 comprobaciones satisfactorias en AOSP Android 15 con juegos diagnósticos propios GB/GBC/GBA: frames, audio, SRAM, estados, .sav compatible, ZIP, seguridad, metadatos sin copias retenidas, locales y carátulas opcionales. Nuevas comprobaciones: arrastrar A y recrear conserva su posición sin cambiar la horizontal, panel táctil se dibuja, audio estéreo no nulo a 2× y 4×, uinput no oculta controles, menú de juego compacto y acceso explícito a la ficha, una sola tarjeta de continuar y sin tira/eslogan.

En el móvil real se ha actualizado sin desinstalar. El diagnóstico distingue uinput-xiaomi de los dispositivos que ocultan controles. Se ha abierto un juego desde el diálogo compacto y observado su pantalla y controles verticales visibles; también el menú de velocidad con el interruptor de sonido. Sesión real a 2×: unos 119 fps, sound=true y underruns=0. La instalación final conservó exactamente los hashes de los 12 archivos privados existentes (partidas, índice y preferencias).

No se ha probado conexión/desconexión de un mando físico en esta iteración: se han comprobado clasificación y lógica de visibilidad, no un dispositivo USB/Bluetooth real. No se ha modificado BRUMA Emulator, sus fuentes de distribución, motores upstream, producción, web ni backend. No se publica este APK de pruebas.
