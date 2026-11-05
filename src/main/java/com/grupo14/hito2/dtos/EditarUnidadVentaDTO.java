package com.grupo14.hito2.dtos;

import com.grupo14.hito2.validation.Patente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Modificación de una unidad por su responsable. El código y el tipo no se cambian: solo se muestran. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EditarUnidadVentaDTO {
    private String codigo;
    private String tipoUnidad;

    @NotBlank(message = "El nombre comercial es obligatorio")
    private String nombreComercial;
    @NotNull(message = "La superficie es obligatoria")
    @Positive(message = "La superficie debe ser mayor a 0")
    private Float superficie;

    // FoodTruck específico
    @Patente
    private String patente;
    private Boolean estaConectado = false;

    // PuestoDesarmable específico
    private Integer cantidadCarpas;
    private Float tiempoMontaje;
}
