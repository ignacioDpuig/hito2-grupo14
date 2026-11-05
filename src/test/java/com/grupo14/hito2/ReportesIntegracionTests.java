package com.grupo14.hito2;

import com.grupo14.hito2.dtos.CierreCajaDTO;
import com.grupo14.hito2.dtos.CrearDetallePedidoDTO;
import com.grupo14.hito2.dtos.CrearPedidoDTO;
import com.grupo14.hito2.dtos.CrearEmpleadoDTO;
import com.grupo14.hito2.dtos.CrearPlatoDTO;
import com.grupo14.hito2.dtos.CrearUnidadVentaDTO;
import com.grupo14.hito2.dtos.EditarEmpleadoDTO;
import com.grupo14.hito2.dtos.EditarUnidadVentaDTO;
import com.grupo14.hito2.dtos.EmpleadoDeUnidad;
import com.grupo14.hito2.dtos.FiltroEmpleadosDTO;
import com.grupo14.hito2.dtos.LiquidacionDTO;
import com.grupo14.hito2.dtos.ReporteMayoresCanonDTO;
import com.grupo14.hito2.entities.Cocinero;
import com.grupo14.hito2.entities.Empleado;
import com.grupo14.hito2.entities.Plato;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.enums.CategoriaCocinero;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.services.LiquidacionService;
import com.grupo14.hito2.services.PedidoService;
import com.grupo14.hito2.services.CuentaService;
import com.grupo14.hito2.services.EmpleadoService;
import com.grupo14.hito2.services.PlatoService;
import com.grupo14.hito2.services.UnidadVentaService;
import com.grupo14.hito2.services.UsuarioDetailsService;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Corre contra la base H2 de los tests, cargada con data.sql. */
@SpringBootTest
@Transactional
class ReportesIntegracionTests {

    @Autowired
    private UnidadVentaService unidadVentaService;
    @Autowired
    private LiquidacionService liquidacionService;
    @Autowired
    private CuentaService cuentaService;
    @Autowired
    private EmpleadoService empleadoService;
    @Autowired
    private PlatoService platoService;
    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void mayoresCanon() {
        List<ReporteMayoresCanonDTO> reporte = unidadVentaService.obtenerMayoresCanon(1, 1);

        assertThat(reporte).hasSize(1);
        assertThat(reporte.get(0).getCodigo()).isEqualTo("PDPARRLL01");
        assertThat(reporte.get(0).getCanon()).isEqualByComparingTo("14975");
    }

    @Test
    void laCantidadDelReporteDeCanonTieneQueEstarEntre1y20() {
        assertThatThrownBy(() -> unidadVentaService.obtenerMayoresCanon(1, 0)).isInstanceOf(ReglaDeNegocioException.class);
        assertThatThrownBy(() -> unidadVentaService.obtenerMayoresCanon(1, -1)).isInstanceOf(ReglaDeNegocioException.class);
        assertThatThrownBy(() -> unidadVentaService.obtenerMayoresCanon(1, 21)).isInstanceOf(ReglaDeNegocioException.class);
        assertThat(unidadVentaService.obtenerMayoresCanon(1, 20)).hasSize(5);
    }

    @Test
    void recibosDelEmpleadoYResponsableSinRecibos() {
        List<LiquidacionDTO> recibos = liquidacionService.obtenerRecibos("empleado@empleado");

        assertThat(recibos).hasSize(1);
        assertThat(recibos.get(0).getTotalNeto()).isEqualByComparingTo("225000");
        assertThat(liquidacionService.obtenerRecibos("juan@email.com")).isEmpty();
    }

    @Test
    void responsableSoloVeLosEmpleadosDeSuUnidad() {
        List<EmpleadoDeUnidad> empleados = empleadoService.obtenerEmpleados(unidadDe("juan@email.com"), new FiltroEmpleadosDTO());

        assertThat(empleados).extracting(e -> e.empleado().getEmail()).containsExactly("carlos@email.com");
    }

    @Test
    void empleadoTrabajaEnSuUnidad() {
        long carlosId = cuentaService.buscarPorEmail("carlos@email.com").getId();
        assertThat(unidadVentaService.buscarPorStaff(carlosId).getCodigo()).isEqualTo("FTSDELSUR1");
    }

    @Test
    void noSePuedeEditarUnPlatoDeOtraUnidad() {
        UnidadVenta otraUnidad = unidadDe("responsable@responsable");
        long margheritaId = platoService.obtenerTodosLosPlatosPorUnidad(
                        unidadDe("juan@email.com").getId()).stream()
                .filter(p -> p.getNombre().equals("Pizza Margherita")).findFirst().orElseThrow().getId();

        assertThatThrownBy(() -> platoService.actualizarPlato(margheritaId, otraUnidad,
                new CrearPlatoDTO("Robada", new BigDecimal("1"), new BigDecimal("1"))))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void elNombreDeUnPlatoNoSeRepiteEnLaUnidad() {
        UnidadVenta unidad = unidadDe("juan@email.com");
        UnidadVenta otraUnidad = unidadDe("responsable@responsable");
        long margheritaId = platoService.obtenerTodosLosPlatosPorUnidad(unidad.getId()).stream()
                .filter(p -> p.getNombre().equals("Pizza Margherita")).findFirst().orElseThrow().getId();

        assertThatThrownBy(() -> platoService.crearPlato(new CrearPlatoDTO(" pizza margherita ", new BigDecimal("1000"), new BigDecimal("5000")), unidad))
                .hasMessageContaining("Ya tenés un plato llamado Pizza Margherita");
        assertThatThrownBy(() -> platoService.actualizarPlato(margheritaId, unidad,
                new CrearPlatoDTO("Pizza Pepperoni", new BigDecimal("1000"), new BigDecimal("5000"))))
                .hasMessageContaining("Pizza Pepperoni");

        platoService.darDeBajaPlato(margheritaId, unidad);
        assertThatThrownBy(() -> platoService.crearPlato(new CrearPlatoDTO("Pizza Margherita", new BigDecimal("1000"), new BigDecimal("5000")), unidad))
                .hasMessageContaining("dado de baja: podés reactivarlo");

        // Editarlo con su mismo nombre no cuenta como repetido, y otra unidad puede usar el nombre
        platoService.actualizarPlato(margheritaId, unidad, new CrearPlatoDTO("Pizza Margherita", new BigDecimal("3000"), new BigDecimal("11000")));
        platoService.crearPlato(new CrearPlatoDTO("Pizza Margherita", new BigDecimal("1000"), new BigDecimal("5000")), otraUnidad);
    }

    @Test
    void elPrecioDeVentaNoPuedeSerMenorAlCostoAlCrearNiEditar() {
        UnidadVenta unidad = unidadDe("juan@email.com");
        long margheritaId = platoService.obtenerTodosLosPlatosPorUnidad(unidad.getId()).stream()
                .filter(p -> p.getNombre().equals("Pizza Margherita")).findFirst().orElseThrow().getId();

        assertThatThrownBy(() -> platoService.crearPlato(
                new CrearPlatoDTO("Pizza Nueva", new BigDecimal("3000"), new BigDecimal("2999.99")), unidad))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("no puede ser menor");
        assertThatThrownBy(() -> platoService.actualizarPlato(margheritaId, unidad,
                new CrearPlatoDTO("Pizza Margherita", new BigDecimal("3000"), new BigDecimal("2999.99"))))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("no puede ser menor");

        Plato platoAlCosto = platoService.crearPlato(
                new CrearPlatoDTO("Pizza Al Costo", new BigDecimal("3000"), new BigDecimal("3000")), unidad);
        assertThat(platoAlCosto.getPrecioVenta()).isEqualByComparingTo("3000");
    }

    @Test
    void elResponsableModificaSuUnidadSinCambiarCodigoNiTipo() {
        EditarUnidadVentaDTO dto = unidadVentaService.obtenerUnidadParaEditar(unidadDe("juan@email.com"));
        assertThat(dto.getTipoUnidad()).isEqualTo("FoodTruck");
        dto.setNombreComercial("Sabores Renovados");
        dto.setSuperficie(30f);
        dto.setEstaConectado(false);
        dto.setCodigo("PDOTRO0001");
        dto.setTipoUnidad("PuestoDesarmable");

        unidadVentaService.modificarUnidad(unidadDe("juan@email.com"), dto);

        UnidadVenta unidad = unidadDe("juan@email.com");
        assertThat(unidad.getNombreComercial()).isEqualTo("Sabores Renovados");
        assertThat(unidad.getCodigo()).isEqualTo("FTSDELSUR1");
        assertThat(unidad.getTipo()).isEqualTo("FoodTruck");
        assertThat(unidadVentaService.obtenerMayoresCanon(1, 2)).extracting(ReporteMayoresCanonDTO::getCanon)
                .usingElementComparator(BigDecimal::compareTo)
                .contains(new BigDecimal("15000"));
    }

    @Test
    void laPatenteSeGuardaNormalizadaYNoSeRepite() {
        EditarUnidadVentaDTO dto = unidadVentaService.obtenerUnidadParaEditar(unidadDe("juan@email.com"));
        dto.setPatente("ab 123-cd");   // la del food truck de Sofía
        assertThatThrownBy(() -> unidadVentaService.modificarUnidad(unidadDe("juan@email.com"), dto))
                .hasMessageContaining("Ya existe un food truck con la patente AB123CD");

        dto.setPatente("aa 111-bb");
        unidadVentaService.modificarUnidad(unidadDe("juan@email.com"), dto);
        assertThat(unidadVentaService.obtenerUnidadParaEditar(unidadDe("juan@email.com")).getPatente()).isEqualTo("AA111BB");

        // La propia patente no cuenta como repetida
        unidadVentaService.modificarUnidad(unidadDe("juan@email.com"), dto);
    }

    @Test
    void unFoodTruckNoPuedeQuedarSinPatente() {
        EditarUnidadVentaDTO dto = unidadVentaService.obtenerUnidadParaEditar(unidadDe("juan@email.com"));
        dto.setPatente(" ");

        assertThatThrownBy(() -> unidadVentaService.modificarUnidad(unidadDe("juan@email.com"), dto))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("patente");
    }

    @Test
    void cierreDeCajaDelCajeroEnElFestivalEnCurso() {
        long cajeroId = cuentaService.buscarPorEmail("cajero@cajero").getId();
        UnidadVenta unidad = unidadVentaService.buscarUnidadEnFestivalEnCurso(cajeroId);

        CierreCajaDTO cierre = pedidoService.calcularCierreDelDia(unidad);

        assertThat(cierre.getNombreUnidad()).isEqualTo("Food Truck La Primavera");
        assertThat(cierre.getFecha()).isEqualTo(LocalDate.now());
        assertThat(cierre.getCantidadPedidos()).isEqualTo(1);
        assertThat(cierre.getTotalVendido()).isEqualByComparingTo("57000");
        assertThat(cierre.isCerrada()).isFalse();
    }

    @Test
    void cerrarLaCajaGuardaElTotalYNoAceptaMasPedidosEseDia() {
        Personal cajero = cuentaService.buscarPorEmail("cajero@cajero");
        UnidadVenta unidad = unidadVentaService.buscarUnidadEnFestivalEnCurso(cajero.getId());
        long platoId = platoService.obtenerPlatosActivosPorUnidad(unidad.getId()).get(0).getId();

        CierreCajaDTO cierre = pedidoService.cerrarCaja(unidad, cajero);

        assertThat(cierre.isCerrada()).isTrue();
        assertThat(cierre.getTotalVendido()).isEqualByComparingTo("57000");
        assertThat(cierre.getCerradaPor()).isEqualTo("Lucas Fernández");
        assertThat(pedidoService.calcularCierreDelDia(unidad).isCerrada()).isTrue();
        assertThat(pedidoService.obtenerCierresDeUnidad(unidad)).hasSize(1);
        assertThatThrownBy(() -> pedidoService.cerrarCaja(unidad, cajero))
                .isInstanceOf(ReglaDeNegocioException.class);
        CrearPedidoDTO pedido = new CrearPedidoDTO(List.of(new CrearDetallePedidoDTO(platoId, 1)));
        assertThatThrownBy(() -> pedidoService.crearPedido(pedido, unidad))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("caja");
    }

    @Test
    void soloUnCajeroCierraLaCaja() {
        Personal cajero = cuentaService.buscarPorEmail("cajero@cajero");
        UnidadVenta unidad = unidadVentaService.buscarUnidadEnFestivalEnCurso(cajero.getId());
        Personal responsable = cuentaService.buscarPorEmail("sofia@email.com");

        assertThatThrownBy(() -> pedidoService.cerrarCaja(unidad, responsable))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void empleadoSinFestivalEnCurso() {
        long carlosId = cuentaService.buscarPorEmail("carlos@email.com").getId();
        assertThat(unidadVentaService.obtenerUnidadEnFestivalEnCurso(carlosId)).isEmpty();
        assertThatThrownBy(() -> unidadVentaService.buscarUnidadEnFestivalEnCurso(carlosId))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void unEmpleadoDadoDeBajaSeLiquidaIgualYSeRestaEnLaRentabilidad() {
        long carlosId = cuentaService.buscarPorEmail("carlos@email.com").getId();

        empleadoService.darDeBajaEmpleado(carlosId, unidadDe("juan@email.com"));

        List<LiquidacionDTO> sueldos = liquidacionService.obtenerLiquidacionesDeLaUnidad(unidadDe("juan@email.com"));
        assertThat(sueldos).singleElement().satisfies(l -> {
            assertThat(l.isDadoDeBaja()).isTrue();
            assertThat(l.getTotalNeto()).isEqualByComparingTo("375000");
            // El recibo no muestra el DNI ni el email anonimizados por la baja definitiva
            assertThat(l.getDniParaMostrar()).isEqualTo("Dado de baja");
            assertThat(l.getEmailParaMostrar()).isEqualTo("Dado de baja");
        });
    }

    @Test
    void dadoDeBajaEnSuUnicaUnidadNoVendeNiEntraAlSistema() {
        Personal cajero = cuentaService.buscarPorEmail("cajero@cajero");
        assertThat(usuarioDetailsService.loadUserByUsername("cajero@cajero").isEnabled()).isTrue();

        empleadoService.darDeBajaEmpleado(cajero.getId(), unidadDe("sofia@email.com"));

        assertThat(unidadVentaService.obtenerUnidadEnFestivalEnCurso(cajero.getId())).isEmpty();
        assertThat(usuarioDetailsService.loadUserByUsername(cajero.getEmail()).isEnabled()).isFalse();
        // Responsables y admin no dependen de una unidad activa
        assertThat(usuarioDetailsService.loadUserByUsername("admin@admin").isEnabled()).isTrue();
        assertThat(empleadoService.obtenerEmpleados(unidadDe("sofia@email.com"), new FiltroEmpleadosDTO()))
                .noneMatch(item -> item.empleado().getId().equals(cajero.getId()));
    }

    @Test
    void responsableSoloDaDeBajaEmpleadosDeSuUnidad() {
        Personal carlos = cuentaService.buscarPorEmail("carlos@email.com");
        long carlosId = carlos.getId();
        long albertoId = cuentaService.buscarPorEmail("empleado@empleado").getId();
        long mariaId = cuentaService.buscarPorEmail("responsable@responsable").getId();

        empleadoService.darDeBajaEmpleado(carlosId, unidadDe("juan@email.com"));
        UnidadVenta foodTruck = unidadDe("juan@email.com");
        assertThat(foodTruck.tieneActivoEnStaff(carlos)).isFalse();

        // Alberto es de otra unidad
        assertThatThrownBy(() -> empleadoService.darDeBajaEmpleado(albertoId, unidadDe("juan@email.com")))
                .isInstanceOf(ReglaDeNegocioException.class);
        // Un responsable no es empleado: ni siquiera se encuentra
        assertThatThrownBy(() -> empleadoService.darDeBajaEmpleado(mariaId, unidadDe("juan@email.com")))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void altaDeEmpleadoValidaDuplicadosYDatosDelTipo() {
        CrearEmpleadoDTO dto = new CrearEmpleadoDTO();
        dto.setNombre("Otro");
        dto.setApellido("Carlos");
        dto.setDni(99_000_111L);
        dto.setEmail("carlos@email.com");
        dto.setTipoPersonal("Cajero");
        dto.setTurno("Noche");
        dto.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        assertThatThrownBy(() -> empleadoService.crearEmpleado(dto, unidadDe("juan@email.com")))
                .hasMessageContaining("Ya existe un usuario con el email");

        dto.setEmail("nuevo@test.com");
        dto.setDni(87654321L); // el DNI de Carlos, que ya trabaja en la unidad de Juan
        assertThatThrownBy(() -> empleadoService.crearEmpleado(dto, unidadDe("juan@email.com")))
                .hasMessageContaining("no se puede volver a incorporar");

        dto.setDni(99_000_111L);
        dto.setTipoPersonal("Cocinero");
        assertThatThrownBy(() -> empleadoService.crearEmpleado(dto, unidadDe("juan@email.com")))
                .hasMessageContaining("categoría del cocinero");
    }

    // Los datos de Carlos López (cocinero de la unidad de Juan, Festival 2024) en un formulario de alta
    private CrearEmpleadoDTO altaDeCarlos() {
        CrearEmpleadoDTO dto = new CrearEmpleadoDTO();
        dto.setNombre("Carlos");
        dto.setApellido("López");
        dto.setDni(87654321L);
        dto.setEmail("carlos@email.com");
        dto.setTipoPersonal("Cocinero");
        dto.setCategoria(CategoriaCocinero.COCINERO_AYUDANTE);
        dto.setEspecialidad("Pastas");
        dto.setFechaNacimiento(LocalDate.of(1988, 3, 20));
        return dto;
    }

    @Test
    void unEmpleadoDadoDeBajaPuedeVolverComoUnaCuentaNuevaConLosMismosDatos() {
        long carlosId = cuentaService.buscarPorEmail("carlos@email.com").getId();
        empleadoService.darDeBajaEmpleado(carlosId, unidadDe("juan@email.com"));

        Empleado nuevoCarlos = empleadoService.crearEmpleado(altaDeCarlos(), unidadDe("juan@email.com"));

        assertThat(nuevoCarlos.getId()).isNotEqualTo(carlosId);
        assertThat(nuevoCarlos.getEmail()).isEqualTo("carlos@email.com");
        assertThat(nuevoCarlos.getDni()).isEqualTo(87654321L);
        assertThat(unidadVentaService.obtenerHistorial(carlosId)).hasSize(1);
        assertThat(empleadoService.obtenerEmpleados(unidadDe("juan@email.com"), new FiltroEmpleadosDTO()))
                .extracting(item -> item.empleado().getId()).contains(nuevoCarlos.getId()).doesNotContain(carlosId);
    }

    @Test
    void unaCuentaDadaDeBajaPuedeRegistrarseDeCeroEnOtraUnidad() {
        // Alberto trabajó en el Festival 2024 (puesto de María)
        long albertoId = cuentaService.buscarPorEmail("empleado@empleado").getId();
        CrearEmpleadoDTO dto = new CrearEmpleadoDTO();
        dto.setNombre("Alberto");
        dto.setApellido("Martínez");
        dto.setDni(22222222L);
        dto.setEmail("empleado@empleado");
        dto.setTipoPersonal("Cocinero");
        dto.setCategoria(CategoriaCocinero.LAVAPLATOS);
        dto.setFechaNacimiento(LocalDate.of(1989, 11, 10));

        empleadoService.darDeBajaEmpleado(albertoId, unidadDe("responsable@responsable"));
        Empleado nuevoAlberto = empleadoService.crearEmpleado(dto, unidadDe("sofia@email.com"));

        assertThat(unidadVentaService.obtenerHistorial(albertoId)).hasSize(1);
        assertThat(nuevoAlberto.getId()).isNotEqualTo(albertoId);
        assertThat(nuevoAlberto.getEmail()).isEqualTo("empleado@empleado");
        assertThat(empleadoService.obtenerEmpleados(unidadDe("sofia@email.com"), new FiltroEmpleadosDTO()))
                .extracting(item -> item.empleado().getId()).contains(nuevoAlberto.getId()).doesNotContain(albertoId);
    }

    @Test
    void altaDeUnidadValidaCodigoRepetidoYDatosDelTipo() {
        CrearUnidadVentaDTO dto = new CrearUnidadVentaDTO();
        dto.setCodigo("FTSDELSUR1");
        dto.setTipoUnidad("FoodTruck");
        dto.setPatente("AA111AA");
        assertThatThrownBy(() -> unidadVentaService.crearUnidadVenta(dto))
                .hasMessageContaining("Ya existe una unidad con el código");

        dto.setCodigo("PDNUEVO001");
        dto.setTipoUnidad("PuestoDesarmable");
        assertThatThrownBy(() -> unidadVentaService.crearUnidadVenta(dto))
                .hasMessageContaining("al menos una carpa");
    }

    // Un puesto válido en el Festival Primavera 2026 (en curso)
    private CrearUnidadVentaDTO puestoNuevo() {
        CrearUnidadVentaDTO dto = new CrearUnidadVentaDTO();
        dto.setCodigo("PDNUEVO001");
        dto.setTipoUnidad("PuestoDesarmable");
        dto.setNombreComercial("Puesto nuevo");
        dto.setSuperficie(10f);
        dto.setCantidadCarpas(1);
        dto.setTiempoMontaje(30f);
        dto.setFestivalId(2L);
        dto.setNombreResponsable("Ana");
        dto.setApellidoResponsable("Paz");
        dto.setEmailResponsable("ana.paz@email.com");
        return dto;
    }

    // El registro solo pide nombre, apellido y mail: dos responsables sin DNI no chocan por el DNI único
    @Test
    void elResponsableSeRegistraSinDniNiFechaDeNacimiento() {
        UnidadVenta unidad = unidadVentaService.crearUnidadVenta(puestoNuevo());
        CrearUnidadVentaDTO otro = puestoNuevo();
        otro.setCodigo("PDNUEVO002");
        otro.setEmailResponsable("otro.responsable@email.com");
        unidadVentaService.crearUnidadVenta(otro);

        Personal responsable = cuentaService.buscarPorEmail("ana.paz@email.com");
        assertThat(responsable.getNombre()).isEqualTo("Ana");
        assertThat(responsable.getApellido()).isEqualTo("Paz");
        assertThat(responsable.getDni()).isNull();
        assertThat(responsable.getFechaNacimiento()).isNull();
        assertThat(responsable.isEstaActivo()).isFalse();
        assertThat(unidad.isActiva()).isFalse();
        assertThat(cuentaService.buscarPorEmail("otro.responsable@email.com").getDni()).isNull();
    }

    @Test
    void noSeRegistranUnidadesEnUnFestivalTerminado() {
        CrearUnidadVentaDTO dto = puestoNuevo();
        dto.setFestivalId(1L);
        assertThatThrownBy(() -> unidadVentaService.crearUnidadVenta(dto)).hasMessageContaining("ya terminó");

        assertThat(unidadVentaService.crearUnidadVenta(puestoNuevo()).getFestival().getNombre())
                .isEqualTo("Festival Primavera 2026");
    }

    @Test
    void elCanonDeUnaUnidadNoPuedeDarNegativo() {
        // 1 m² × $500 − 100 min × $10 = −$500
        CrearUnidadVentaDTO dto = puestoNuevo();
        dto.setSuperficie(1f);
        dto.setTiempoMontaje(100f);
        assertThatThrownBy(() -> unidadVentaService.crearUnidadVenta(dto))
                .hasMessageContaining("canon da negativo");
        assertThat(cuentaService.obtenerPersonalPorEmail("ana.paz@email.com")).isEmpty();

        EditarUnidadVentaDTO edicion = unidadVentaService.obtenerUnidadParaEditar(unidadDe("responsable@responsable"));
        edicion.setSuperficie(1f);
        edicion.setTiempoMontaje(100f);
        assertThatThrownBy(() -> unidadVentaService.modificarUnidad(unidadDe("responsable@responsable"), edicion))
                .hasMessageContaining("canon da negativo");
    }

    @Test
    void filtrosDeEmpleadosExcluyenLosDadosDeBajaPorDefecto() {
        FiltroEmpleadosDTO filtro = new FiltroEmpleadosDTO();
        filtro.setTexto("LÓP");   // Carlos López, sin importar mayúsculas
        assertThat(empleadoService.obtenerEmpleados(unidadDe("juan@email.com"), filtro))
                .extracting(e -> e.empleado().getEmail()).containsExactly("carlos@email.com");

        filtro.setTexto(null);
        filtro.setTipoPersonal("Cajero");
        assertThat(empleadoService.obtenerEmpleados(unidadDe("juan@email.com"), filtro)).isEmpty();

        long carlosId = cuentaService.buscarPorEmail("carlos@email.com").getId();
        empleadoService.darDeBajaEmpleado(carlosId, unidadDe("juan@email.com"));
        assertThat(empleadoService.obtenerEmpleados(unidadDe("juan@email.com"), new FiltroEmpleadosDTO())).isEmpty();

        assertThat(empleadoService.obtenerEmpleados(unidadDe("juan@email.com"), new FiltroEmpleadosDTO()))
                .noneMatch(e -> e.empleado().getId().equals(carlosId));
    }

    @Test
    void modificarEmpleadoDeLaUnidad() {
        long carlosId = cuentaService.buscarPorEmail("carlos@email.com").getId();
        EditarEmpleadoDTO dto = empleadoService.obtenerEmpleadoParaEditar(carlosId, unidadDe("juan@email.com"));
        assertThat(dto.getTipoPersonal()).isEqualTo("Cocinero");
        assertThat(dto.getCategoria()).isEqualTo(CategoriaCocinero.COCINERO);

        dto.setNombre("Carlos Alberto");
        dto.setCategoria(CategoriaCocinero.COCINERO_AYUDANTE);
        empleadoService.actualizarEmpleado(carlosId, dto, unidadDe("juan@email.com"));

        Cocinero carlos = (Cocinero) cuentaService.buscarPorEmail("carlos@email.com");
        assertThat(carlos.getNombre()).isEqualTo("Carlos Alberto");
        assertThat(carlos.getCategoria()).isEqualTo(CategoriaCocinero.COCINERO_AYUDANTE);
    }

    @Test
    void modificarEmpleadoValidaDuplicadosPuestoYUnidad() {
        long carlosId = cuentaService.buscarPorEmail("carlos@email.com").getId();
        long albertoId = cuentaService.buscarPorEmail("empleado@empleado").getId();

        EditarEmpleadoDTO dto = empleadoService.obtenerEmpleadoParaEditar(carlosId, unidadDe("juan@email.com"));
        dto.setEmail("juan@email.com");
        assertThatThrownBy(() -> empleadoService.actualizarEmpleado(carlosId, dto, unidadDe("juan@email.com")))
                .hasMessageContaining("Ya existe un usuario con el email");

        EditarEmpleadoDTO sinCategoria = empleadoService.obtenerEmpleadoParaEditar(carlosId, unidadDe("juan@email.com"));
        sinCategoria.setCategoria(null);
        assertThatThrownBy(() -> empleadoService.actualizarEmpleado(carlosId, sinCategoria, unidadDe("juan@email.com")))
                .hasMessageContaining("categoría del cocinero");

        assertThatThrownBy(() -> empleadoService.obtenerEmpleadoParaEditar(albertoId, unidadDe("juan@email.com")))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void responsableVeLaLiquidacionDeSusEmpleados() {
        List<LiquidacionDTO> liquidaciones = liquidacionService.obtenerLiquidacionesDeLaUnidad(unidadDe("juan@email.com"));
        assertThat(liquidaciones).singleElement().satisfies(l -> {
            assertThat(l.getNombreEmpleado()).isEqualTo("Carlos");
            assertThat(l.getTotalNeto()).isEqualByComparingTo("375000");
        });

        long albertoId = cuentaService.buscarPorEmail("empleado@empleado").getId();
        assertThatThrownBy(() -> liquidacionService.calcularLiquidacionDeEmpleadoDeLaUnidad(unidadDe("juan@email.com"), albertoId))
                .isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void noHayReciboDeUnFestivalDondeNoTrabajo() {
        long festivalPrimaveraId = unidadVentaService.obtenerUnidadPorCodigo("FTPRIMAV26").orElseThrow().getFestival().getId();
        assertThatThrownBy(() -> liquidacionService.calcularLiquidacion("carlos@email.com", festivalPrimaveraId))
                .isInstanceOf(NoEncontradoException.class)
                .hasMessageContaining("No trabajaste");
    }

    @Test
    void elEmpleadoSinFestivalEnCursoNoTieneUnidadParaVender() {
        long carlosId = cuentaService.buscarPorEmail("carlos@email.com").getId();
        long cajeroId = cuentaService.buscarPorEmail("cajero@cajero").getId();

        assertThatThrownBy(() -> unidadVentaService.buscarUnidadEnFestivalEnCurso(carlosId))
                .isInstanceOf(NoEncontradoException.class);
        assertThat(unidadVentaService.buscarUnidadEnFestivalEnCurso(cajeroId).getCodigo()).isEqualTo("FTPRIMAV26");
    }

    private UnidadVenta unidadDe(String emailResponsable) {
        return unidadVentaService.buscarUnidadDelResponsable(emailResponsable);
    }
}
