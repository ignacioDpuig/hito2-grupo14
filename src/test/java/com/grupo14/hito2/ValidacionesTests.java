package com.grupo14.hito2;

import com.grupo14.hito2.dtos.CrearEmpleadoDTO;
import com.grupo14.hito2.dtos.CrearUnidadVentaDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
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

    private CrearEmpleadoDTO empleadoValido() {
        CrearEmpleadoDTO dto = new CrearEmpleadoDTO();
        dto.setNombre("Ana");
        dto.setApellido("Paz");
        dto.setDni(33333333L);
        dto.setEmail("ana@test.com");
        dto.setTipoPersonal("Cajero");
        dto.setFechaNacimiento(LocalDate.of(1995, 1, 1));
        return dto;
    }

    @Test
    void formulariosValidosNoTienenErrores() {
        assertThat(errores(unidadValida())).isEmpty();
        assertThat(errores(empleadoValido())).isEmpty();
    }

    @Test
    void empleadoMenorDeEdad() {
        CrearEmpleadoDTO dto = empleadoValido();
        dto.setFechaNacimiento(LocalDate.now().minusYears(18).plusDays(1));
        assertThat(errores(dto)).containsExactly("El empleado tiene que ser mayor de edad");

        dto.setFechaNacimiento(LocalDate.now().minusYears(18));
        assertThat(errores(dto)).isEmpty();
    }

    @Test
    void empleadoConCamposVaciosYEmailInvalido() {
        CrearEmpleadoDTO dto = empleadoValido();
        dto.setNombre(" ");
        dto.setDni(null);
        dto.setEmail("no-es-un-mail");
        dto.setFechaIngreso(LocalDate.now().plusDays(1));

        assertThat(errores(dto)).containsExactlyInAnyOrder(
                "El nombre es obligatorio",
                "El DNI es obligatorio",
                "El email no tiene un formato válido",
                "La fecha de ingreso no puede ser futura");
    }

    @Test
    void elDniTieneQueTener7u8Digitos() {
        CrearEmpleadoDTO dto = empleadoValido();
        for (long dni : new long[]{1, 999_999, 100_000_000}) {
            dto.setDni(dni);
            assertThat(errores(dto)).as("DNI " + dni).containsExactly("El DNI tiene que tener 7 u 8 dígitos");
        }
        for (long dni : new long[]{1_000_000, 99_999_999}) {
            dto.setDni(dni);
            assertThat(errores(dto)).as("DNI " + dni).isEmpty();
        }
    }

    @Test
    void laFechaDeIngresoTieneQueSerDeCuandoYaEraMayorDeEdad() {
        CrearEmpleadoDTO dto = empleadoValido();   // nació el 01/01/1995
        dto.setFechaIngreso(LocalDate.of(2012, 12, 31));
        assertThat(validator.validate(dto)).singleElement().satisfies(v -> {
            assertThat(v.getMessage()).isEqualTo("La fecha de ingreso tiene que ser de cuando ya era mayor de edad");
            assertThat(v.getPropertyPath().toString()).isEqualTo("fechaIngreso");
        });

        dto.setFechaIngreso(LocalDate.of(2013, 1, 1));
        assertThat(errores(dto)).isEmpty();
    }

    @Test
    void laFechaDeNacimientoNoPuedeSerAnteriorA1900() {
        CrearEmpleadoDTO dto = empleadoValido();
        dto.setFechaNacimiento(LocalDate.of(1899, 12, 31));
        assertThat(errores(dto)).containsExactly("La fecha de nacimiento no puede ser anterior a 1900");
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
