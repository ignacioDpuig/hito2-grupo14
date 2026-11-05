package com.grupo14.hito2;

import com.grupo14.hito2.dtos.CrearUnidadVentaDTO;
import com.grupo14.hito2.entities.FoodTruck;
import com.grupo14.hito2.entities.PuestoDesarmable;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.mappers.PersonalMapper;
import com.grupo14.hito2.mappers.UnidadVentaMapper;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MappersTests {

    private final PersonalMapper personalMapper = new PersonalMapper();
    private final UnidadVentaMapper unidadVentaMapper = new UnidadVentaMapper();

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
