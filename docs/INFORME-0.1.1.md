# Bruma Pocket 0.1.1 — prueba independiente

20 de noviembre de 2026. Package `com.brumastudio.pocket`, versionCode 2, Android 8+, ARM64. Firma Android Debug, no publicación.

## Cambios

- Los mismos nueve idiomas de BRUMA: inglés, español, portugués de Brasil, francés, alemán, italiano, japonés, coreano y chino simplificado. Idioma del móvil por defecto; selector en menú y ajustes. Preferencia propia, no modifica el idioma del dispositivo ni de BRUMA.
- Marca «Bruma Pocket» con letras de colores propias. «Consigue más sistemas.» y enlace «Descubre BRUMA Emulator →».
- Corregido el fallo real de la carpeta del móvil: el límite contaba recursos ajenos de RPG Maker. Ahora se omiten sus subárboles reconocidos, se cuentan solo candidatos compatibles y se aíslan archivos dañados. Cancelar o fallar al abrir la raíz conserva el índice anterior. Límites de profundidad y archivos se mantienen.
- Biblioteca basada en URI y metadatos, sin copiar todas las ROMs. Archivo normal: descriptor de lectura si el proveedor permite reabrirlo; ZIP o proveedor incompatible: solo copia temporal del juego activo, retirada al cerrar la sesión. Recuperación de temporales huérfanos al arrancar. Los originales nunca se borran ni se modifican.
- Exportación e importación `.sav` sin contenedor ni conversión. Tamaños admitidos por BRUMA: 512, 8192, 32768, 65536 o 131072 bytes. Los estados rápidos completos no son `.sav`.
- La importación valida antes de modificar, conserva la partida anterior y aparta los estados viejos. Una sesión nativa del mismo juego se cierra antes de importar para evitar que sobrescriba la partida recién importada. Sigue disponible el backup completo propio de Pocket en ZIP.

## Uso de .sav

1. Guardar dentro del propio juego en Pocket; salir a su ficha.
2. Pulsar «Exportar partida .sav para BRUMA» y elegir destino en Android.
3. Abrir el mismo juego, de la misma versión, en BRUMA Emulator e importar el archivo `.sav` mediante su opción existente.

Un `.sav` no acredita por sí mismo a qué ROM pertenece. Seleccionar otro juego o edición puede producir una partida incompatible. No es necesario comprar BRUMA para guardar o exportar en Pocket.

## Evidencia de pruebas

- Compilación ARM64 y lint Android completados.
- Móvil real: actualización sin desinstalar; conserva permisos de carpeta. Lectura real de 7 juegos en 223 ms, 5 directorios de RPG Maker reconocidos y omitidos, 0 archivos problemáticos.
- Selector real en el móvil: español → English comprobado por etiquetas de interfaz; restaurado a idioma automático.
- Emulador AOSP Android 15 x86_64: 26 comprobaciones satisfactorias. GB/GBC/GBA con diagnósticos propios producen imagen, audio, SRAM y estados. ZIP y archivo normal comparten identidad. Sin copias de cartuchos retenidas después de cerrar.
- Mezcla de ROM/ZIP repetidos, árbol RPG, archivo roto y ZIP inseguro: conserva los 3 juegos válidos y avisa de los archivos rechazados.
- `.sav` exportado leído por el decodificador real de BRUMA 1.0.4 copiado solo a pruebas, con igualdad exacta de bytes. Importación correcta, estados antiguos apartados y tamaño inválido rechazado sin modificar partida.
- Backup de juego incorrecto, ruta maliciosa y contenido alterado rechazados; rescaneo y cancelación preservan identidad/partidas/preferencias. Recursos de los nueve idiomas comprobados.
- No se ha ejecutado esta transferencia dentro de la aplicación publicada de BRUMA ni probado un mando físico en esta iteración. Los estados rápidos de Pocket no se anuncian como portables entre apps.

## Límites y separación

Solo se ha modificado Pocket. BRUMA Emulator, motores originales, producción, backend, web y claves de publicación permanecen ajenos a esta entrega. Sin SDKs ni servicios nuevos. No hay juegos comerciales en APK ni fuentes. Las capturas de QA del emulador usan exclusivamente diagnósticos originales.
