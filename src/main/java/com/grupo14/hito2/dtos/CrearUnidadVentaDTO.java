package com.grupo14.hito2.dtos;

import com.grupo14.hito2.validation.CodigoUnidad;
import com.grupo14.hito2.validation.Patente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CodigoUnidad
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CrearUnidadVentaDTO {
    // Datos comunes
    @NotBlank(message = "El nombre comercial es obligatorio")
    private String nombreComercial;
    @NotBlank(message = "El código es obligatorio")
    private String codigo;
    @NotNull(message = "La superficie es obligatoria")
    @Positive(message = "La superficie debe ser mayor a 0")
    private Float superficie;
    @NotNull(message = "Hay que elegir un festival")
    private Long festivalId;
    @NotBlank(message = "Hay que elegir el tipo de unidad")
    private String tipoUnidad;

    // FoodTruck específico
    @Patente
    private String patente;
    private Boolean estaConectado = false;

    // PuestoDesarmable específico
    private Integer cantidadCarpas;
    private Float tiempoMontaje;

    // Responsable
    @NotBlank(message = "El nombre del responsable es obligatorio")
    private String nombreResponsable;
    @NotBlank(message = "El apellido del responsable es obligatorio")
    private String apellidoResponsable;
    @NotBlank(message = "El email del responsable es obligatorio")
    @Email(message = "El email del responsable no tiene un formato válido")
    private String emailResponsable;
}