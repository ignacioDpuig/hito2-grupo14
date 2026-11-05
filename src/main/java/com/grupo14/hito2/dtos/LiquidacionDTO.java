package com.grupo14.hito2.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LiquidacionDTO {
    // Empleado
    private Long empleadoId;
    private String nombreEmpleado;
    private String apellidoEmpleado;
    private Long dni;
    private String cargo;
    private String especialidadOTurno;
    private boolean dadoDeBaja;     // se liquida igual: trabajó en el festival
    private String emailEmpleado;
    private LocalDate fechaIngreso;

    // Unidad de venta que paga el sueldo: su id identifica el recibo
    private Long unidadId;
    private String unidadCodigo;
    private String unidadNombre;
    private String unidadTipo;

    // Responsable de la unidad
    private String responsableNombre;
    private String responsableApellido;
    private Long responsableDni;   // null: el responsable se registra sin DNI
    private String responsableEmail;

    // Festival
    private String nombreFestival;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long diasDuracion;
    private Long festivalId;

    // Cálculos
    private BigDecimal sueldoBase;
    private BigDecimal plus;
    private String conceptoPlus;
    private String desglose;
    private String porcentajeAplicado;
    private BigDecimal totalNeto;

    // Metadata
    private LocalDate fechaGeneracion;

    // La baja definitiva anonimiza DNI y email para liberarlos: el recibo no muestra esos datos inventados
    public String getDniParaMostrar() {
        if (dadoDeBaja) return DADO_DE_BAJA;
        return dni == null ? "-" : String.valueOf(dni);
    }

    public String getEmailParaMostrar() {
        return dadoDeBaja ? DADO_DE_BAJA : emailEmpleado;
    }

    private static final String DADO_DE_BAJA = "Dado de baja";
}
