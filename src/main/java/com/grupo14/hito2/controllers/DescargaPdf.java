package com.grupo14.hito2.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Respuesta HTTP para que el navegador descargue un PDF con el nombre indicado. */
final class DescargaPdf {

    private DescargaPdf() {
    }

    static ResponseEntity<byte[]> de(byte[] pdf, String nombreArchivo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
