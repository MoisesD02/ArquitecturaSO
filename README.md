# simuladorSO — Versión Avance 1

Versión deliberadamente reducida del simulador para presentar únicamente la arquitectura, interfaz y núcleo base solicitados en el primer avance.

## Incluye
- Interfaz principal y navegación entre módulos.
- Clases base: Kernel, CPU, Proceso, Memoria, Dispositivo y SistemaArchivos.
- Reloj del sistema funcional.
- Registro cronológico de eventos (Log).
- Controles del núcleo: iniciar, detener, reiniciar y avanzar reloj.
- Visualización del estado base de CPU, procesos, memoria, dispositivos y sistema de archivos.

## No incluye todavía
- Creación/terminación y planificación real de procesos.
- Asignación dinámica de memoria.
- Interrupciones y operaciones de E/S.
- Creación/eliminación de archivos.
- Configuración avanzada.

Estas funcionalidades se reservan para avances posteriores, para que el Avance 1 represente claramente el núcleo base y no parezca el proyecto final.

## Ejecución
Requiere JDK 21. En IntelliJ abra la carpeta que contiene `pom.xml` y ejecute el goal Maven `javafx:run`.
