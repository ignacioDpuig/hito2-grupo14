package com.grupo14.hito2;

import com.grupo14.hito2.dtos.CrearUnidadVentaDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Bean Validation de los formularios, sin levantar Spring. */
class ValidacionesTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private <T> Set<String> errores(T dto) {
        return validator.validate(dto).stream().map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }

    private CrearUnidadVentaDTO unidadValida() {
        CrearUnidadVentaDTO dto = new CrearUnidadVentaDTO();
        dto.setNombreComercial("Sabores");
        dto.setCodigo("FTSABOR001");
        dto.setSuperficie(20f);
        dto.setFestivalId(1L);
        dto.setTipoUnidad("FoodTruck");
        dto.setNombreResponsable("Rita");
        dto.setApellidoResponsable("Paz");
        dto.setEmailResponsable("rita@test.com");
        return dto;
    }

    @Test
    void formulariosValidosNoTienenErrores() {
        assertThat(errores(unidadValida())).isEmpty();
    }

    @Test
    void laPatenteTieneFormatoViejoOMercosur() {
        CrearUnidadVentaDTO dto = unidadValida();
        for (String patente : new String[]{"ABC123", "AB123CD", "ab 123-cd", "", null}) {
            dto.setPatente(patente);
            assertThat(errores(dto)).as(patente).isEmpty();
        }
        for (String patente : new String[]{"AB12CD", "1234ABC", "ABCD123", "A1B2C3"}) {
            dto.setPatente(patente);
            assertThat(errores(dto)).as(patente).containsExactly("La patente tiene que ser del formato ABC123 o AB123CD");
        }
    }

    @Test
    void codigoDeUnidadConFormatoInvalido() {
        CrearUnidadVentaDTO dto = unidadValida();
        for (String codigo : new String[]{"FTABC", "ABC1234567", "ftsabor001", "FTSABOR00!"}) {
            dto.setCodigo(codigo);
            assertThat(errores(dto)).as(codigo).singleElement().asString().startsWith("El código debe tener 10 caracteres");
        }
    }

    @Test
    void elPrefijoDelCodigoTieneQueCoincidirConElTipo() {
        CrearUnidadVentaDTO dto = unidadValida();
        dto.setCodigo("PDSABOR001");
        assertThat(errores(dto)).containsExactly("El código de esta unidad tiene que empezar con FT");

        dto.setTipoUnidad("PuestoDesarmable");
        assertThat(errores(dto)).isEmpty();
    }

    @Test
    void elErrorDelCodigoSeMarcaEnElCampoCodigo() {
        CrearUnidadVentaDTO dto = unidadValida();
        dto.setCodigo("XX");
        assertThat(validator.validate(dto)).singleElement()
                .satisfies(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("codigo"));
    }
}
