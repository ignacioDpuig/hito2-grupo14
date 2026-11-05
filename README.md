# Epicentro Gourmet — Hito 2

Aplicación web para gestionar festivales gastronómicos: unidades de venta (food trucks y puestos desarmables), su personal, platos, pedidos, canon y liquidación de sueldos.

**Stack:** Java 21 · Spring Boot · Spring Security · Spring Data JPA · MySQL · Thymeleaf · Lombok · PDFBox · Spring Mail.

## Grupo

**Grupo 14**

| Integrante | Usuario de GitHub |
|---|---|
| Ignacio Puig | [@ignacioDpuig](https://github.com/ignacioDpuig) |
| Leandro Kaul | [@Leandroidev](https://github.com/Leandroidev) |
| Joaquín Adra | [@joacoadra](https://github.com/joacoadra) |

## Cómo correr la aplicación

**Requisitos:** JDK 21 y MySQL corriendo en `localhost:3306`. Maven viene incluido (`mvnw`).

```bash
# 1. Crear la base de datos (una sola vez)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS hito2_db;"

# 2. Variables de entorno: copiar la plantilla y completar los valores (el .env no se sube a GitHub)
cp .env.example .env

# 3. Levantar la aplicación
./mvnw spring-boot:run
```

En Windows (PowerShell):

```powershell
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS hito2_db;"
Copy-Item .env.example .env
notepad .env
.\mvnw.cmd spring-boot:run
```

La aplicación queda en **http://localhost:8080**.

**Variables de entorno.** Se leen del archivo `.env` de la raíz (ver `.env.example`) o de la terminal. Ninguna credencial está escrita en el código.

| Variable | Valor por defecto | Para qué |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/hito2_db` | Conexión a MySQL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `root` | Usuario de MySQL |

**Datos de prueba.** Al arrancar se ejecuta [`src/main/resources/data.sql`](src/main/resources/data.sql) con todos los inserts de prueba. La base se recrea en cada arranque (`ddl-auto=create-drop`).

**Tests:** `./mvnw test` (usan una base H2 en memoria, no necesitan MySQL).

## Diagrama de clases

[Poo2Hito2.dia](Poo2Hito2.dia) (se abre con [Dia](http://dia-installer.de/)).

## Casos de uso

Cada caso de uso se agregó a `main` con su propio Pull Request.

| # | Caso de uso | Responsable | Rama | Pull Request |
|---|---|---|---|---|
| — | Arquitectura base (modelo, repositorios, datos de prueba) | Ignacio Puig | `ArquitecturaBaseIgnacioPuig` | — |
| 1 | Login y seguridad | Ignacio Puig | `CULoginSeguridadIgnacioPuig` | — |
| 2 | Ver festivales y unidades | Ignacio Puig | `CUVerFestivalesUnidadesIgnacioPuig` | — |
| 3 | Reporte de mayor canon | Ignacio Puig | `CUMayorCanonIgnacioPuig` | — |
| 4 | Registro de unidad y responsable | Ignacio Puig | `CURegistroUnidadResponsableIgnacioPuig` | — |
| 5 | Alta de empleado | Leandro Kaul | `CUAltaEmpleadoLeandroKaul` | — |
| 6 | Historial del empleado | Leandro Kaul | `CUHistorialEmpleadoLeandroKaul` | — |
| 7 | Modificación y baja de empleado | Leandro Kaul | `CUModificacionBajaEmpleadoLeandroKaul` | — |
| 8 | Liquidación de sueldos | Leandro Kaul | `CULiquidacionSueldosLeandroKaul` | — |
| 9 | Recibo de sueldo en PDF | Leandro Kaul | `CUReciboPDFLeandroKaul` | — |
| 10 | Alta de plato | Joaquín Adra | `CUAltaPlatoJoaquinAdra` | — |
| 11 | Modificación y baja de plato | Joaquín Adra | `CUModificacionBajaPlatoJoaquinAdra` | — |
| 12 | Carga de pedidos | Joaquín Adra | `CUPedidoJoaquinAdra` | — |
| 13 | Cierre de caja | Joaquín Adra | `CUCerrarCajaJoaquinAdra` | — |
| 14 | Reportes del responsable | Joaquín Adra | `CUReportesResponsableJoaquinAdra` | — |
| 15 | Ranking de unidades | Ignacio Puig | `CURankingUnidadesIgnacioPuig` | — |

Los links a cada Pull Request se completan a medida que se mergean.
