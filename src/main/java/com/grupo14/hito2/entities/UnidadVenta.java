package com.grupo14.hito2.entities;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.grupo14.hito2.enums.TipoUnidad;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class UnidadVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreComercial;
    @Column(unique = true)
    private String codigo;

    private float superficie;

    @OneToOne
    @JoinColumn(name = "responsable_id", unique = true)
    private Personal responsable;

    @OneToMany(mappedBy = "unidadVenta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Plato> platos = new HashSet<>();

    @OneToMany(mappedBy = "unidadVenta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AsignacionStaff> staff = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    public abstract BigDecimal calcularCanon(ConfiguracionCostos config);

    public abstract TipoUnidad getTipoUnidad();

    public String getTipo() {
        return getTipoUnidad().getValor();
    }

    public String getTipoDescripcion() {
        return getTipoUnidad().getDescripcion();
    }

    // Los costos del festival que le corresponden a este tipo de unidad
    public ConfiguracionCostos getConfiguracionCostos() {
        return festival.getConfiguracionCostos(getTipoUnidad());
    }

    public BigDecimal calcularCanon() {
        return calcularCanon(getConfiguracionCostos());
    }

    // Un puesto con poca superficie y mucho montaje daría un canon negativo: el predio le estaría pagando a la unidad
    public void validarCanon() {
        BigDecimal canon = calcularCanon();
        if (canon.signum() < 0) {
            throw new ReglaDeNegocioException(String.format(
                    "Con esos datos el canon da negativo ($%.2f): la superficie es muy chica para ese tiempo de montaje", canon));
        }
    }

    // Superficie y tiempo de montaje son float: se pasan por su texto para que 14.3 no quede 14.300000190734863
    protected static BigDecimal decimal(float valor) {
        return new BigDecimal(Float.toString(valor));
    }

    public abstract DatosUnidad getDatosDelTipo();

    // Cada tipo valida y actualiza sus propios datos: el código y el tipo no se modifican
    public abstract void actualizarDatosDelTipo(DatosUnidad datos);

    // Habilitada = su responsable ya activó la cuenta con la contraseña que recibió por mail
    public boolean isActiva() {
        return responsable != null && responsable.isEstaActivo();
    }

    public void asignarResponsable(Responsable responsable) {
        this.responsable = responsable;
        agregarAlStaff(responsable);
    }

    public void agregarAlStaff(Personal personal) {
        if (!tieneEnStaff(personal)) {
            staff.add(new AsignacionStaff(this, personal));
        }
    }

    // Activo o dado de baja: alguna vez fue parte de la unidad
    public boolean tieneEnStaff(Personal personal) {
        return buscarEnStaff(personal).isPresent();
    }

    public boolean tieneActivoEnStaff(Personal personal) {
        return buscarEnStaff(personal).filter(asignacion -> !asignacion.isEliminado()).isPresent();
    }

    public void darDeBajaDelStaff(Personal personal) {
        asignacionDe(personal).setEliminado(true);
    }

    public long getCantidadStaffActivo() {
        return staff.stream().filter(asignacion -> !asignacion.isEliminado()).count();
    }

    private Optional<AsignacionStaff> buscarEnStaff(Personal personal) {
        return staff.stream().filter(asignacion -> asignacion.esDe(personal)).findFirst();
    }

    private AsignacionStaff asignacionDe(Personal personal) {
        return buscarEnStaff(personal)
                .orElseThrow(() -> new ReglaDeNegocioException(personal.getNombre() + " no es parte de " + nombreComercial));
    }
}