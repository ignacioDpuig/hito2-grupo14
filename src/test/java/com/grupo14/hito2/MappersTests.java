package com.grupo14.hito2;

import com.grupo14.hito2.dtos.CrearEmpleadoDTO;
import com.grupo14.hito2.dtos.CrearUnidadVentaDTO;
import com.grupo14.hito2.entities.Cajero;
import com.grupo14.hito2.entities.Cocinero;
import com.grupo14.hito2.entities.Empleado;
import com.grupo14.hito2.entities.FoodTruck;
import com.grupo14.hito2.entities.PuestoDesarmable;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.enums.CategoriaCocinero;
import com.grupo14.hito2.mappers.PersonalMapper;
import com.grupo14.hito2.mappers.UnidadVentaMapper;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MappersTests {

    private final PersonalMapper personalMapper = new PersonalMapper();
    private final UnidadVentaMapper unidadVentaMapper = new UnidadVentaMapper();

    @Test
    void creaCocineroConSusDatos() {
        CrearEmpleadoDTO dto = new CrearEmpleadoDTO();
        dto.setTipoPersonal("Cocinero");
        dto.setNombre("Carlos");
        dto.setDni(87654321L);
        dto.setEspecialidad("Pizzas");
        dto.setCategoria(CategoriaCocinero.COCINERO);
        dto.setFechaIngreso(LocalDate.of(2023, 2, 15));

        Empleado empleado = personalMapper.toEmpleado(dto);

        assertThat(empleado).isInstanceOf(Cocinero.class);
        assertThat(empleado.getNombre()).isEqualTo("Carlos");
        assertThat(empleado.getDetalleCargo()).isEqualTo("Pizzas");
        assertThat(empleado.getFechaIngreso()).isEqualTo(LocalDate.of(2023, 2, 15));
    }

    @Test
    void creaCajeroYSinFechaIngresoUsaHoy() {
        CrearEmpleadoDTO dto = new CrearEmpleadoDTO();
        dto.setTipoPersonal("Cajero");
        dto.setTurno("Mañana");
        dto.setDni(40111222L);

        Empleado empleado = personalMapper.toEmpleado(dto);

        assertThat(empleado).isInstanceOf(Cajero.class);
        assertThat(empleado.getDetalleCargo()).isEqualTo("Mañana");
        assertThat(empleado.getFechaIngreso()).isEqualTo(LocalDate.now());
    }

    @Test
    void tipoDePersonalInvalidoFalla() {
        CrearEmpleadoDTO dto = new CrearEmpleadoDTO();
        dto.setTipoPersonal("cocinero");
        assertThatThrownBy(() -> personalMapper.toEmpleado(dto))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("Tipo de personal inválido");
    }

    @Test
    void creaFoodTruckYPuesto() {
        CrearUnidadVentaDTO foodTruck = new CrearUnidadVentaDTO();
        foodTruck.setTipoUnidad("FoodTruck");
        foodTruck.setSuperficie(25.5f);
        foodTruck.setEstaConectado(true);
        foodTruck.setPatente("abc 123");

        CrearUnidadVentaDTO puesto = new CrearUnidadVentaDTO();
        puesto.setTipoUnidad("PuestoDesarmable");
        puesto.setSuperficie(30f);
        puesto.setCantidadCarpas(3);
        puesto.setTiempoMontaje(2.5f);

        UnidadVenta ft = unidadVentaMapper.toEntity(foodTruck);
        UnidadVenta pd = unidadVentaMapper.toEntity(puesto);

        assertThat(ft).isInstanceOf(FoodTruck.class);
        assertThat(ft.getTipo()).isEqualTo("FoodTruck");
        assertThat(((FoodTruck) ft).getPatente()).isEqualTo("ABC123");
        assertThat(pd).isInstanceOf(PuestoDesarmable.class);
        assertThat(pd.getTipo()).isEqualTo("PuestoDesarmable");
    }

    @Test
    void tipoDeUnidadInvalidoFalla() {
        CrearUnidadVentaDTO dto = new CrearUnidadVentaDTO();
        dto.setTipoUnidad("");
        assertThatThrownBy(() -> unidadVentaMapper.toEntity(dto))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("Tipo de unidad inválido");
    }
}
