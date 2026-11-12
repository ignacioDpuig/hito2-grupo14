# Área B — Personal y sueldos

**Autor:** Leandro Kaul · **Grupo 14** · Hito 2 — Festival Gastronómico

Este documento describe la parte del sistema que gestiona al personal de cada unidad y sus sueldos: cómo está construida, qué se valida y qué decisiones técnicas se tomaron.

| Caso de uso | PR | Quién lo usa |
|---|---|---|
| Alta de empleado | #7 | Responsable |
| Historial del empleado | #8 | Empleado |
| Modificación y baja de empleado | #9 | Responsable |
| Liquidación de sueldos | #10, #20 | Responsable / Empleado |
| Recibo de sueldo en PDF | #11, #19 | Responsable / Empleado |

**Índice:** [1. Modelo](#1-modelo-del-personal) · [2. Alta](#2-alta-de-empleado) · [3. Modificación y filtros](#3-modificación-y-filtros) · [4. Baja](#4-baja-de-empleado) · [5. Historial](#5-historial-del-empleado) · [6. Liquidación](#6-liquidación-de-sueldos) · [7. Recibo PDF](#7-recibo-de-sueldo-en-pdf) · [8. Demo](#8-guion-de-demo)

---

## 1. Modelo del personal

```
Personal ── Empleado (abstracta) ─┬─ Cajero    turno · plus por antigüedad
                                  └─ Cocinero  especialidad · categoría → plus de la categoría
```

`Empleado` declara lo que cada puesto resuelve a su manera (polimorfismo):

| Método | Cajero | Cocinero |
|---|---|---|
| `calcularPlus(costos, fin)` | Monto por año × años de antigüedad | El plus de su categoría |
| `getCargo()` | "Cajero" | Cocinero / Cocinero Ayudante / Lavaplatos |
| `actualizarDatosDelPuesto(datos)` | Exige turno | Exige categoría |
| `puedeCargarPedidos()` | `true` | `false` |

- `CategoriaCocinero` es un enum que sabe qué plus de la configuración del festival le corresponde a cada categoría.
- `TipoPersonal.desde(...).nuevoEmpleado()` crea la subclase correcta según lo elegido en el formulario (Factory).

### Relación con la unidad: `AsignacionStaff`

Un empleado se vincula a una unidad mediante `AsignacionStaff` (tabla `unidad_personal`), con la restricción única `(unidad, persona)` y un indicador `eliminado`. Así se conserva en qué unidades (y festivales) trabajó cada persona aunque después se la dé de baja.

---

## 2. Alta de empleado

El responsable da de alta a un cajero o cocinero, que queda siempre en **su propia unidad** (la unidad sale del usuario logueado mediante `@UnidadDelResponsable`).

```
ResponsableController.crearEmpleado(dto, @UnidadDelResponsable unidad)
   └─ EmpleadoService.crearEmpleado(dto, unidad)         @Transactional
        ├─ PersonalMapper: TipoPersonal crea la subclase y ésta valida sus datos
        ├─ DNI no registrado
        ├─ CuentaService.darDeAlta(empleado, EMPLEADO)    contraseña temporal + BCrypt + mail
        └─ unidad.agregarAlStaff(empleado)
```

La creación de la cuenta reutiliza el servicio común de cuentas: el empleado recibe una contraseña temporal por mail (enviada después del commit) y debe elegir la suya en el primer ingreso.

### Validaciones

**Formato** (`CrearEmpleadoDTO`):

| Campo | Regla |
|---|---|
| Nombre, apellido, email, tipo | Obligatorios; email con formato válido |
| DNI | `@Dni` (propia): 7 u 8 dígitos |
| Fecha de nacimiento | `@MayorDeEdad` (propia): al menos 18 años y posterior a 1900 |
| Fecha de ingreso | `@PastOrPresent`: no puede ser futura |
| Ingreso vs. nacimiento | `@IngresoMayorDeEdad` (propia, **a nivel de clase**): al ingresar ya tenía que ser mayor de edad |

**Negocio:**

| Regla | Dónde |
|---|---|
| Un cajero necesita turno; un cocinero, categoría | En cada subclase (`actualizarDatosDelPuesto`) |
| El DNI no puede pertenecer a otra persona registrada; si es de un empleado existente, no se puede volver a incorporar | `EmpleadoService` |
| El email no puede pertenecer a otra persona | `CuentaService.validarEmailLibre` |

---

## 3. Modificación y filtros

- **Modificación:** solo sobre empleados de la propia unidad (`buscarEmpleadoDeLaUnidad`, que verifica `unidad.tieneActivoEnStaff(empleado)`). El email y el DNI no pueden quedar repetidos con otra persona, excluyendo al propio empleado. Se aplican las mismas validaciones de formato que en el alta.
- **Listado con filtros:** consulta JPQL con filtros opcionales por texto (nombre, apellido o email) y por tipo de empleado:

```sql
SELECT new com.grupo14.hito2.dtos.EmpleadoDeUnidad(e)
FROM AsignacionStaff a, Empleado e
WHERE a.personal.id = e.id AND a.unidadVenta.id = :unidadId
  AND a.eliminado = FALSE
  AND (:texto IS NULL OR LOWER(e.nombre) LIKE :texto OR ...)
  AND (:tipo IS NULL OR TYPE(e) = :tipo)
```

`TYPE(e) = :tipo` filtra por subclase (Cajero o Cocinero) sobre la herencia JOINED.

---

## 4. Baja de empleado

La baja es **definitiva**:

1. Se marcan como eliminadas todas sus asignaciones, en todas las unidades.
2. Se anonimizan el DNI y el email (el DNI pasa a un valor negativo y el email a una dirección de bajas), para que no identifiquen a la persona en el sistema.
3. Se conservan las asignaciones, así el historial y los sueldos ya liquidados siguen disponibles.

Efectos en el resto del sistema:
- No puede iniciar sesión (`UsuarioDetailsService` lo marca deshabilitado), y si tenía la sesión abierta, `CuentaActivaInterceptor` la cierra.
- Deja de aparecer en la gestión de empleados.
- En los recibos su DNI y email se muestran como "Dado de baja".

---

## 5. Historial del empleado

`/empleado/historial` muestra todas las unidades (y por lo tanto los festivales) en las que trabajó el empleado, de la más reciente a la más antigua:

```sql
SELECT a.unidadVenta FROM AsignacionStaff a
WHERE a.personal.id = :personalId
ORDER BY a.unidadVenta.festival.fechaInicio DESC
```

El perfil muestra además la unidad del festival en curso, si la tiene: solo cuenta una asignación activa en un festival cuyas fechas incluyen el día de hoy.

---

## 6. Liquidación de sueldos

### 6.1 Fórmula

```
sueldo = (sueldo base + plus) × porcentaje según los días trabajados
```

| Parte | De dónde sale |
|---|---|
| Sueldo base | `ConfiguracionCostos` del festival, para el tipo de unidad (la específica gana a la general) |
| Plus del cocinero | El de su categoría |
| Plus del cajero | Monto por año × años de antigüedad, contados **al terminar el festival** |
| Días trabajados | `Festival.getDiasTrabajadosDesde(fechaIngreso)`: desde el inicio del festival (o el ingreso, si fue posterior) hasta el fin del festival (o hoy, si todavía no terminó), ambos días incluidos |

**Porcentaje** (`Liquidacion.porcentaje()`), tomando 30 días como mes completo:

| Días trabajados | Porcentaje |
|---|---|
| 0 | 0% |
| 1 a 15 | `días × 50% / 30` |
| 16 a 30 | `días / 30` |
| Más de 30 | `(30 + (días − 30) × 50%) / 30`: los primeros 30 al 100% y el resto al 50% |

**Ejemplo:** lavaplatos en el Festival Gastronómico 2024 (38 días, sueldo base $100.000, plus $50.000):
```
porcentaje = (30 + 8 × 0,5) / 30 = 113,33%
sueldo     = (100.000 + 50.000) × 1,133333 = $169.999,95
```

### 6.2 Diseño

- **Template Method:** `Empleado.liquidar(unidad)` arma la liquidación igual para todos (sueldo base, días trabajados) y delega en cada subclase solo el plus (`calcularPlus`).
- **Value object:** `Liquidacion` es un `record` que no se persiste. El sueldo siempre se calcula a partir de los datos vigentes.
- **`BigDecimal`** en todos los montos, con redondeo explícito (`HALF_UP`).
- `LiquidacionService` no calcula: le pide al empleado `liquidar(unidad)` y `LiquidacionMapper` arma el recibo (`LiquidacionDTO`).

### 6.3 Quién ve qué

| Usuario | Pantalla | Control |
|---|---|---|
| Empleado | `/empleado/recibos`: un recibo por cada festival donde trabajó | Un festival donde no trabajó → 404 "No trabajaste en ese festival" |
| Responsable | `/responsable/sueldos`: el sueldo de cada empleado de su unidad | Un empleado de otra unidad → 404 "Ese empleado no es de tu unidad" |

Los responsables y el administrador no cobran sueldo: su lista de recibos queda vacía.

---

## 7. Recibo de sueldo en PDF

El recibo se genera en memoria con **Apache PDFBox** (`PDFService.generarReciboPDF`) y se descarga desde el recibo del empleado o desde la vista de sueldos del responsable.

**Contenido:**
1. Banda con el título y el código de la unidad.
2. Empleador (unidad y responsable) y empleado (nombre, DNI, cargo, festival).
3. Tabla de conceptos: sueldo base, plus, desglose por días y porcentaje aplicado.
4. Total neto destacado.

**Detalles técnicos:**
- `DescargaPdf` arma la respuesta HTTP con `Content-Type: application/pdf` y `Content-Disposition: attachment`, para que el navegador descargue el archivo con su nombre.
- Los textos que no entran en su columna se cortan con "...".
- Los caracteres que las fuentes estándar del PDF no soportan se reemplazan, para que la generación no falle.
- Usa los mismos datos que el recibo en pantalla (`LiquidacionDTO`), así los dos siempre coinciden.

---

## 8. Guion de demo

| Paso | Usuario | Acción | Qué se muestra |
|---|---|---|---|
| 1 | `juan@email.com` / `pass123` | Empleados → nuevo: probar un menor de edad y un DNI repetido, luego uno válido | Validaciones; alta con mail de credenciales |
| 2 | Responsable | Filtrar por texto y por tipo | Consulta con filtros opcionales |
| 3 | Responsable | Editar un empleado y luego darlo de baja | Modificación y baja definitiva |
| 4 | Responsable | Sueldos → ver un recibo → descargar PDF | Liquidación y PDF |
| 5 | `martin@email.com` / `martin123` | Historial y recibos | Un empleado que trabajó en dos festivales, con un recibo por cada uno |
| 6 | `rocio@email.com` / `rocio123` | Intentar ingresar | Un empleado dado de baja no puede entrar |
