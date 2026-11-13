# Adelanto Repartidor — App Android nativa

App para llevar el control del dinero del trabajo de repartidor (pagos, cobros y adelanto semanal).
Todo se guarda **solo en el teléfono** (Room / SQLite). No usa internet.

## Funcionalidades

- **Quick Settings Tile**: baja la barra de notificaciones y toca el tile **"Adelanto"** para abrir directo la pantalla de registro.
- **3 tipos de operación**:
  - **Pago solo** (queda pendiente, ganancia 0)
  - **Cobro solo** (ganancia = cobro)
  - **Pago + Cobro** (ganancia = cobro − pago)
- **Vincular cobro a un pago pendiente**: convierte ese pago en una sola operación "Pago + Cobro" y calcula la ganancia real.
- **Adelanto acumulado por semana**: la semana va de **martes 00:00 a lunes 23:59**.
- **Dinero en Caja**: un monto manual por semana, solo informativo (se guarda en SharedPreferences).
- **Historial por día**, agrupado por semana, con total por día y detalle de operaciones.
- **Tema claro/oscuro** automático según el sistema.
- Moneda: pesos (CLP), separador de miles con punto.

## Estructura del proyecto

```
.
├── .github/workflows/build-apk.yml   # compila el APK en GitHub Actions
├── app/                              # código de la app
├── gradle/wrapper/                   # wrapper de Gradle 8.7
├── gradlew / gradlew.bat
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Compilar el APK con GitHub Actions (sin instalar nada)

1. Sube este proyecto a un repositorio de GitHub (el contenido en la **raíz** del repo).
2. En el repo: **Settings → Actions → General → Actions permissions** → elige
   *"Allow all actions and reusable workflows"* → **Save**.
3. Ve a la pestaña **Actions** → en el panel izquierdo elige **"Build APK"** →
   botón **"Run workflow"** → **"Run workflow"**.
4. Espera 5–10 minutos (la primera vez tarda más porque descarga Gradle y las dependencias).
5. Entra a la ejecución terminada → sección **Artifacts** → descarga **`adelanto-apk`**.
6. Descomprime el `.zip`: dentro está `app-debug.apk`.

## Instalar en el teléfono

1. Pasa el `app-debug.apk` al teléfono (WhatsApp, Drive, cable USB, etc.).
2. Ajustes → Seguridad → **Instalar apps de origen desconocido** → permite la app con la que abrirás el APK.
3. Abre el APK → **Instalar**.

### Agregar el tile a la barra de notificaciones
1. Abre la app una vez.
2. Baja la barra de notificaciones y toca el lápiz / editar iconos.
3. Busca **"Adelanto"** y arrástralo a los iconos activos.

## Compilar con Android Studio (opcional)

1. Instala Android Studio.
2. **File → Open** → selecciona esta carpeta.
3. Espera la sincronización de Gradle y presiona **Run ▶** con el teléfono conectado (depuración USB activada).

## Tecnologías

- Kotlin 2.0.0 + Jetpack Compose (BOM 2024.06.00) + Material 3
- Room 2.6.1 (KSP)
- Navigation Compose
- AGP 8.5.0 / Gradle 8.7 / JDK 17
- minSdk 26, targetSdk 34
