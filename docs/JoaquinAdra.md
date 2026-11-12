# Área C — Platos, ventas y reportes del responsable

**Autor:** Joaquín Adra · **Grupo 14** · Hito 2 — Festival Gastronómico

Este documento describe la parte del sistema que gestiona lo que vende cada unidad: los platos, los pedidos, el cierre de caja y los reportes del responsable. Detalla cómo está construida, qué se valida y qué decisiones técnicas se tomaron.

| Caso de uso | PR | Quién lo usa |
|---|---|---|
| Alta de plato | #12 | Responsable |
| Modificación y baja de plato | #13 | Responsable |
| Carga de pedidos | #14 | Cajero |
| Cierre de caja | #15 | Cajero |
| Reportes del responsable | #16 | Responsable |

**Índice:** [1. Platos](#1-platos) · [2. Pedidos](#2-carga-de-pedidos) · [3. Cierre de caja](#3-cierre-de-caja) · [4. Reportes](#4-reportes-del-responsable) · [5. Demo](#5-guion-de-demo)

---

## 1. Platos

### 1.1 Modelo

`Plato` pertenece a una unidad y tiene nombre, costo de producción, precio de venta y un indicador `eliminado` (baja lógica). Los montos son `BigDecimal` con precisión fija (`precision = 12, scale = 2`).

El responsable gestiona solo los platos de **su** unidad: la unidad sale del usuario logueado (`@UnidadDelResponsable`) y, cuando la URL lleva el id de un plato, `PlatoService.buscarPlatoDeLaUnidad` verifica que la unidad lo ofrezca (`unidad.ofrece(plato)`).

### 1.2 Validaciones

**Formato** (`CrearPlatoDTO`):

| Campo | Regla |
|---|---|
| Nombre | Obligatorio |
| Costo de producción | Obligatorio, `@PositiveOrZero` |
| Precio de venta | Obligatorio, `@Positive` |
| Precio vs. costo | `@AssertTrue`: el precio de venta no puede ser menor al costo |

**Negocio** (`PlatoService`):

| Regla | Mensaje |
|---|---|
| Precio de venta ≥ costo (se repite en el servicio como protección) | "El precio de venta no puede ser menor al costo de producción" |
| Nombre único dentro de la unidad, sin distinguir mayúsculas | "Ya tenés un plato llamado X" (y, si está dado de baja, sugiere reactivarlo) |
| El plato es de la unidad del responsable | "No tenés permiso para modificar este plato" |

### 1.3 Modificación, baja y reactivación

- **Modificación:** cambia nombre, costo y precio. Las ventas ya realizadas no se ven afectadas, porque cada pedido guarda su propio precio y costo.
- **Baja lógica:** el plato deja de ofrecerse en pedidos nuevos, pero sigue apareciendo en los reportes de lo ya vendido.
- **Reactivación:** vuelve a ofrecerse.
- Dentro de una transacción, los cambios sobre la entidad se guardan solos (*dirty checking* de JPA), sin llamar a `save`.

---

## 2. Carga de pedidos

### 2.1 Quién y dónde

- Solo un **cajero** carga pedidos. Se decide por polimorfismo con `puedeCargarPedidos()`, que es `true` únicamente en `Cajero`; los demás reciben 403.
- El pedido se carga en la unidad del **festival en curso** donde el cajero está activo.
- El formulario ofrece solo los platos activos de esa unidad.

### 2.2 Modelo

```
Pedido ── 1..* ── DetallePedido ── * ── 1 ── Plato
                  clave compuesta (pedido, plato)
                  cantidad · precioUnitario · costoUnitario
```

- `Pedido.nuevo(unidad)` es un método de fábrica: fija la fecha de hoy y el festival de la unidad; el formulario no puede enviar otra fecha.
- `DetallePedido` usa una **clave compuesta** (`@EmbeddedId` con `@MapsId`) formada por pedido y plato. Si el mismo plato se agrega dos veces, se suma la cantidad en el mismo renglón.
- Cada renglón guarda el **precio y el costo del momento de la venta**. Si después se modifica el plato, la recaudación y la rentabilidad ya registradas no cambian.
- Los detalles se guardan en cascada con el pedido (`CascadeType.ALL`).

### 2.3 Validaciones

| Regla | Dónde |
|---|---|
| La caja de hoy no está cerrada | `PedidoService` |
| La unidad está habilitada (su responsable activó la cuenta) | `Pedido.nuevo` |
| El festival está en curso | `Pedido.nuevo` |
| El plato pertenece a la unidad | `Pedido.agregarDetalle` |
| El plato no está dado de baja | `Pedido.agregarDetalle` |
| El pedido tiene al menos un plato con cantidad mayor a 0 | `PedidoService` |

Las reglas del pedido viven en la propia entidad (`Pedido`), que garantiza que nunca se arme un pedido inválido.

---

## 3. Cierre de caja

El cajero cierra la caja al terminar la jornada:

1. Antes de cerrar, ve lo vendido en el día hasta el momento (pedidos y total).
2. Al cerrar, se guarda un `CierreCaja` con la fecha, la unidad, el cajero, la cantidad de pedidos y el total vendido.
3. A partir de ahí la unidad **no acepta más pedidos ese día**.
4. Se listan los cierres anteriores.

### Validaciones

| Regla | Cómo |
|---|---|
| Una sola vez por día y unidad | Se verifica en `PedidoService` y además hay una restricción única `(unidad_venta_id, fecha)` en la base |
| Solo un cajero puede cerrar | `CierreCaja.deHoy` verifica `puedeCargarPedidos()` |
| Solo la caja de hoy, en un festival en curso | La fecha la fija el sistema; la unidad es la del festival en curso |

Los totales se calculan con la misma consulta de ventas que usan los reportes (`resumirVentasEntreFechas` con hoy como inicio y fin).

---

## 4. Reportes del responsable

`/responsable/reportes` muestra, para la unidad del responsable en su festival:

| Reporte | Cómo se obtiene |
|---|---|
| **Ventas** | Recaudación, costo de los platos vendidos y cantidad de pedidos |
| **Plato estrella** | El plato con más unidades vendidas |
| **Rentabilidad neta** | `ventas − costo de platos − sueldos del staff − canon` |

### 4.1 Consultas

**Ventas** (`DetallePedidoRepository.resumirVentas`): una sola consulta devuelve los tres valores en un `record` (`ResumenVentas`). `COALESCE` asegura un 0 cuando todavía no hay ventas.

```sql
SELECT new com.grupo14.hito2.dtos.ResumenVentas(
    COALESCE(SUM(dp.cantidad * dp.precioUnitario), 0),
    COALESCE(SUM(dp.cantidad * dp.costoUnitario), 0),
    COUNT(DISTINCT dp.pedido.id))
FROM DetallePedido dp
WHERE dp.plato.unidadVenta.id = :unidadId AND dp.pedido.festival.id = :festivalId
```

**Plato estrella** (`PlatoRepository.findPlatoEstrella`): agrupa por plato, ordena por la cantidad vendida de mayor a menor (con desempate por nombre) y se queda con el primero (`LIMIT 1`).

### 4.2 Rentabilidad neta

Reúne datos de las tres áreas del sistema:

| Término | Origen |
|---|---|
| Ventas y costo de platos | Consulta de ventas (esta área) |
| Sueldos | Suma de `empleado.liquidar(unidad).total()` del staff (área de personal) |
| Canon | `unidad.calcularCanon()`, polimórfico por tipo de unidad (área del administrador) |

La resta la hace `RentabilidadDTO.getRentabilidadNeta()`, que se calcula siempre a partir de sus cuatro términos.

### 4.3 Rentabilidad entre fechas

`/responsable/reportes/fechas` calcula la misma rentabilidad para un período del festival:

- Las ventas y el costo de platos son los del período.
- Los sueldos y el canon se **prorratean** por los días que cubre el período: `monto × días del período / días del festival` (`Festival.prorratear`).
- `RentabilidadEntreFechasDTO` extiende `RentabilidadDTO` y agrega las fechas y los días.

**Validaciones:** la fecha de inicio no puede ser posterior a la de fin, y ambas tienen que estar dentro del festival.

---

## 5. Guion de demo

| Paso | Usuario | Acción | Qué se muestra |
|---|---|---|---|
| 1 | `juan@email.com` / `pass123` | Platos → nuevo: probar precio menor al costo y un nombre repetido, luego uno válido | Validaciones de formato y de negocio |
| 2 | `responsable@responsable` / `responsable` | Reactivar la Provoleta (está dada de baja) y volver a darla de baja | Baja lógica |
| 3 | `cajero@cajero` / `cajero` | Cargar un pedido con dos platos | Pedido en el festival en curso |
| 4 | Cajero | Cerrar la caja e intentar cargar otro pedido | Cierre único por día |
| 5 | `sofia@email.com` / `sofia123` | Reportes | Ventas, plato estrella y rentabilidad neta |
| 6 | Responsable | Reportes entre fechas: probar un rango fuera del festival, luego uno válido | Validación de fechas y prorrateo |
