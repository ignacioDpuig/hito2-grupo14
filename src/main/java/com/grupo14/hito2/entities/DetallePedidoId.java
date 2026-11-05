package com.grupo14.hito2.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DetallePedidoId implements Serializable {

    private Long pedidoId;
    private Long platoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetallePedidoId that = (DetallePedidoId) o;
        return Objects.equals(pedidoId, that.pedidoId) && Objects.equals(platoId, that.platoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pedidoId, platoId);
    }
}