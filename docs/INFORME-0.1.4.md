# Pocket 0.1.4 — presentación de imagen

Prueba independiente, com.brumastudio.pocket, código 5, Android 8+, ARM64. Sin publicación.

Se verificó visualmente en el móvil el supuesto recorte: con escala entera activada el fotograma GBA seguía completo y con proporción 3:2, pero ocupaba 960×640 píxeles dentro de un área mayor. La reducción a 4× y los márgenes se interpretaban como recorte. El modo proporcional aprovecha mayor superficie sin deformar; los márgenes del modo entero son inevitables cuando el área disponible no es un múltiplo de la resolución nativa.

Se aclara la opción en los nueve idiomas como Escala entera (con márgenes). Escala entera y Estirar son excluyentes en Ajustes. El render garantiza la proporción original cuando está seleccionada escala entera, incluso ante preferencias antiguas contradictorias, y alinea tanto dimensiones como origen a píxeles completos dentro del rectángulo disponible. En pantallas menores a la resolución nativa permite reducción proporcional para mostrar toda la imagen.

34 comprobaciones superadas: incluye geometría para GB/GBC 160×144 y GBA 240×160 en vertical, horizontal, pantalla pequeña y límites fraccionarios por densidad. Se comprueba contención, proporción, múltiplos enteros y prioridad frente a estiramiento, además de las regresiones de emulación y partidas. Compilación y lint satisfactorios. Instalación de prueba sobre Pocket existente; no se modifica BRUMA Emulator ni producción.

La preparación para Google Play, GitHub y web continúa pausada a petición del usuario. No se ha creado repositorio ni publicado documentación web en esta corrección.
