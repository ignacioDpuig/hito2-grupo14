package com.grupo14.hito2.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CrearPedidoDTO {
    private List<CrearDetallePedidoDTO> detalles;
}