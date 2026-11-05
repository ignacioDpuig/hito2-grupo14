package com.grupo14.hito2.validation;

import com.grupo14.hito2.dtos.CrearUnidadVentaDTO;
import com.grupo14.hito2.enums.TipoUnidad;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

public class CodigoUnidadValidator implements ConstraintValidator<CodigoUnidad, CrearUnidadVentaDTO> {

    private static final String FORMATO = "^(FT|PD)[A-Z0-9]{8}$";
    private static final Map<String, String> PREFIJO_POR_TIPO = Map.of(
            TipoUnidad.FOOD_TRUCK.getValor(), "FT",
            TipoUnidad.PUESTO_DESARMABLE.getValor(), "PD");

    @Override
    public boolean isValid(CrearUnidadVentaDTO dto, ConstraintValidatorContext context) {
        String codigo = dto.getCodigo();
        if (codigo == null || codigo.isBlank()) return true; // de eso se encarga @NotBlank

        if (!codigo.matches(FORMATO)) {
            return rechazar(context, context.getDefaultConstraintMessageTemplate());
        }
        String prefijoEsperado = PREFIJO_POR_TIPO.get(dto.getTipoUnidad());
        if (prefijoEsperado != null && !codigo.startsWith(prefijoEsperado)) {
            return rechazar(context, "El código de esta unidad tiene que empezar con " + prefijoEsperado);
        }
        return true;
    }

    // El error se marca en el campo codigo para que el formulario lo muestre ahí
    private boolean rechazar(ConstraintValidatorContext context, String mensaje) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(mensaje).addPropertyNode("codigo").addConstraintViolation();
        return false;
    }
}
