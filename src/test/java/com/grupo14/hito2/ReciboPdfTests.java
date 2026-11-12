package com.grupo14.hito2;

import com.grupo14.hito2.dtos.LiquidacionDTO;
import com.grupo14.hito2.services.LiquidacionService;
import com.grupo14.hito2.services.PDFService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** El recibo en PDF tiene la unidad que paga (con su id), su responsable y el empleado. */
@SpringBootTest
@Transactional
class ReciboPdfTests {

    @Autowired
    private LiquidacionService liquidacionService;
    @Autowired
    private PDFService pdfService;

    private String textoDelPdf(LiquidacionDTO recibo) throws Exception {
        try (var documento = Loader.loadPDF(pdfService.generarReciboPDF(recibo))) {
            return new PDFTextStripper().getText(documento);
        }
    }

    @Test
    void elReciboTieneLaUnidadSuResponsableYElEmpleado() throws Exception {
        LiquidacionDTO recibo = liquidacionService.obtenerRecibos("carlos@email.com").get(0);

        String texto = textoDelPdf(recibo);

        assertThat(texto).contains(
                "N° " + recibo.getUnidadId(), "FTSDELSUR1", "Food Truck Sabores del Sur", "Food truck",
                "Juan García", "12345678", "juan@email.com",
                "Carlos López", "87654321", "Cocinero",
                "$ 283.333,25");
    }

    @Test
    void unNombreConCaracteresFueraDeLaFuenteNoRompeElPdf() throws Exception {
        LiquidacionDTO recibo = liquidacionService.obtenerRecibos("carlos@email.com").get(0);
        recibo.setNombreEmpleado("Łukasz 🍕");

        assertThat(textoDelPdf(recibo)).contains("?ukasz ?");
    }
}
