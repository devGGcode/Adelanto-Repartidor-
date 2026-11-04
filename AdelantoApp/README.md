# Adelanto Repartidor - App Android

App para llevar el control del dinero del trabajo de repartidor.

## Funcionalidades
- **Icono en barra de notificaciones (Quick Settings Tile)**: baja la barra de notificaciones y toca el tile "Adelanto" para abrir directamente la pantalla de registro.
- **Caso A (Pago + Cobro con recargo)**: ingresas lo pagado en el local y lo cobrado al cliente; la ganancia = cobro - pago.
- **Caso B (Solo cobro)**: ingresas lo cobrado al cliente; la ganancia = cobro.
- **Adelanto acumulado por semana** (martes a lunes): se suman todas las ganancias de la semana.
- **Historial por semana** con total de adelanto y detalle de operaciones.
- Datos guardados solo en el teléfono (Room/SQLite).

## Cómo compilar e instalar
1. Instala **Android Studio** (https://developer.android.com/studio).
2. Abre la carpeta `AdelantoApp` como proyecto.
3. Espera a que Gradle sincronice dependencias (requiere internet la primera vez).
4. Conecta tu teléfono Android (con depuración USB activada) o crea un emulador.
5. Presiona **Run** (▶) para instalar la app.

### Agregar el tile a la barra de notificaciones
1. Instala la app.
2. Baja la barra de notificaciones (swipe desde arriba).
3. Toca el lápiz/editar (iconos de Quick Settings).
4. Busca "Adelanto" y arrástralo a los tiles activos.
5. Ahora al tocarlo abre la pantalla para registrar una operación.

## Moneda
Pesos (CLP). El separador de miles usa punto.

## Semana
Martes 00:00 a Lunes 23:59.
