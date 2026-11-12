package com.grupo14.hito2.services;

import com.grupo14.hito2.dtos.LiquidacionDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Recibo de haberes en PDF (A4): encabezado con la unidad de venta que paga, datos del empleador
 * (unidad y responsable) y del empleado, detalle de la liquidación, total y lugar para las firmas.
 */
@Service
public class PDFService {

    private static final PDFont REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont NEGRITA = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final Color PRIMARIO = new Color(85, 15, 237);
    private static final Color PRIMARIO_SUAVE = new Color(241, 235, 255);
    private static final Color TEXTO = new Color(29, 27, 46);
    private static final Color TEXTO_SUAVE = new Color(93, 90, 114);
    private static final Color BORDE = new Color(227, 225, 239);

    private static final float MARGEN = 48;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEDA =
            new DecimalFormat("$ #,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-AR")));

    public byte[] generarReciboPDF(LiquidacionDTO recibo) throws IOException {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            PDDocumentInformation info = documento.getDocumentInformation();
            info.setTitle("Recibo de haberes - " + recibo.getNombreEmpleado() + " " + recibo.getApellidoEmpleado());
            info.setAuthor("Epicentro Gourmet");

            try (PDPageContentStream stream = new PDPageContentStream(documento, pagina)) {
                Lienzo lienzo = new Lienzo(stream, pagina.getMediaBox().getWidth(), pagina.getMediaBox().getHeight());
                float y = encabezado(lienzo, recibo);
                y = partes(lienzo, recibo, y - 24);
                y = detalle(lienzo, recibo, y - 28);
                firmas(lienzo, y - 90);
                pie(lienzo, recibo);
            }

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        }
    }

    // Banda de color con el título y el número de la unidad de venta que identifica el recibo
    private float encabezado(Lienzo l, LiquidacionDTO r) throws IOException {
        float alto = 92;
        float y = l.alto - alto;
        l.rectangulo(0, y, l.ancho, alto, PRIMARIO);
        l.texto("EPICENTRO GOURMET", MARGEN, l.alto - 34, NEGRITA, 10, PRIMARIO_SUAVE);
        l.texto("Recibo de haberes", MARGEN, l.alto - 60, NEGRITA, 22, Color.WHITE);
        l.texto(r.getNombreFestival() + "  |  " + fecha(r.getFechaInicio()) + " al " + fecha(r.getFechaFin()),
                MARGEN, l.alto - 78, REGULAR, 10, PRIMARIO_SUAVE);

        float derecha = l.ancho - MARGEN;
        l.textoDerecha("UNIDAD DE VENTA", derecha, l.alto - 34, NEGRITA, 9, PRIMARIO_SUAVE);
        l.textoDerecha("N° " + r.getUnidadId(), derecha, l.alto - 60, NEGRITA, 22, Color.WHITE);
        l.textoDerecha("Código " + r.getUnidadCodigo(), derecha, l.alto - 78, REGULAR, 10, PRIMARIO_SUAVE);
        return y;
    }

    // Dos recuadros: el empleador (unidad y su responsable) y el empleado
    private float partes(Lienzo l, LiquidacionDTO r, float arriba) throws IOException {
        float separacion = 16;
        float anchoCaja = (l.ancho - 2 * MARGEN - separacion) / 2;
        float alto = 128;

        caja(l, MARGEN, arriba, anchoCaja, alto, "EMPLEADOR", new String[][]{
                {"Unidad", r.getUnidadNombre()},
                {"Tipo", r.getUnidadTipo()},
                {"Código", r.getUnidadCodigo() + "  (N° " + r.getUnidadId() + ")"},
                {"Responsable", nombre(r.getResponsableNombre(), r.getResponsableApellido())},
                {"DNI", r.getResponsableDni() == null ? "-" : String.valueOf(r.getResponsableDni())},
                {"Email", r.getResponsableEmail()},
        });
        caja(l, MARGEN + anchoCaja + separacion, arriba, anchoCaja, alto, "EMPLEADO", new String[][]{
                {"Nombre", nombre(r.getNombreEmpleado(), r.getApellidoEmpleado())},
                {"DNI", r.getDniParaMostrar()},
                {"Cargo", r.getCargo()},
                {"Especialidad / turno", r.getEspecialidadOTurno()},
                {"Ingreso", fecha(r.getFechaIngreso())},
                {"Email", r.getEmailParaMostrar()},
        });
        return arriba - alto;
    }

    private void caja(Lienzo l, float x, float arriba, float ancho, float alto, String titulo, String[][] filas) throws IOException {
        l.borde(x, arriba - alto, ancho, alto, BORDE);
        l.rectangulo(x, arriba - 22, ancho, 22, PRIMARIO_SUAVE);
        l.texto(titulo, x + 12, arriba - 15, NEGRITA, 9, PRIMARIO);
        float y = arriba - 40;
        for (String[] fila : filas) {
            l.texto(fila[0], x + 12, y, REGULAR, 8.5f, TEXTO_SUAVE);
            l.texto(recortar(fila[1], NEGRITA, 9, ancho - 120), x + 108, y, NEGRITA, 9, TEXTO);
            y -= 15;
        }
    }

    // Tabla de conceptos y total neto destacado
    private float detalle(Lienzo l, LiquidacionDTO r, float arriba) throws IOException {
        float ancho = l.ancho - 2 * MARGEN;
        float colImporte = MARGEN + ancho - 12;
        float colDetalle = MARGEN + 220;

        l.texto("DETALLE DE LA LIQUIDACIÓN", MARGEN, arriba, NEGRITA, 10, TEXTO);
        float y = arriba - 14;
        l.rectangulo(MARGEN, y - 20, ancho, 20, PRIMARIO_SUAVE);
        l.texto("Concepto", MARGEN + 12, y - 14, NEGRITA, 9, PRIMARIO);
        l.texto("Detalle", colDetalle, y - 14, NEGRITA, 9, PRIMARIO);
        l.textoDerecha("Importe", colImporte, y - 14, NEGRITA, 9, PRIMARIO);
        y -= 20;

        BigDecimal subtotal = r.getSueldoBase().add(r.getPlus());
        String[][] filas = {
                {"Sueldo base", "Fijado por el festival", moneda(r.getSueldoBase())},
                {r.getConceptoPlus(), r.getCargo(), moneda(r.getPlus())},
                {"Subtotal", "Sueldo base + plus", moneda(subtotal)},
                {"Desglose de liquidación", r.getDesglose(), ""},
        };
        for (String[] fila : filas) {
            y -= 24;
            l.texto(fila[0], MARGEN + 12, y + 8, REGULAR, 10, TEXTO);
            l.texto(recortar(fila[1], REGULAR, 9, colImporte - colDetalle - 90), colDetalle, y + 8, REGULAR, 9, TEXTO_SUAVE);
            l.textoDerecha(fila[2], colImporte, y + 8, REGULAR, 10, TEXTO);
            l.linea(MARGEN, y, MARGEN + ancho, y, BORDE);
        }

        y -= 44;
        l.rectangulo(MARGEN, y, ancho, 36, PRIMARIO);
        l.texto("TOTAL NETO A COBRAR", MARGEN + 12, y + 13, NEGRITA, 11, Color.WHITE);
        l.textoDerecha(moneda(r.getTotalNeto()), colImporte, y + 12, NEGRITA, 15, Color.WHITE);
        if (r.isDadoDeBaja()) {
            l.texto("El empleado fue dado de baja en la unidad: se liquida lo trabajado en el festival.",
                    MARGEN, y - 16, REGULAR, 8.5f, TEXTO_SUAVE);
        }
        return y;
    }

    private void firmas(Lienzo l, float y) throws IOException {
        float ancho = 190;
        float derecha = l.ancho - MARGEN - ancho;
        l.linea(MARGEN, y, MARGEN + ancho, y, TEXTO_SUAVE);
        l.linea(derecha, y, derecha + ancho, y, TEXTO_SUAVE);
        l.texto("Firma del responsable de la unidad", MARGEN, y - 13, REGULAR, 8.5f, TEXTO_SUAVE);
        l.texto("Firma del empleado", derecha, y - 13, REGULAR, 8.5f, TEXTO_SUAVE);
    }

    private void pie(Lienzo l, LiquidacionDTO r) throws IOException {
        l.linea(MARGEN, 46, l.ancho - MARGEN, 46, BORDE);
        l.texto("Generado el " + fecha(r.getFechaGeneracion()) + " por Epicentro Gourmet", MARGEN, 32, REGULAR, 8, TEXTO_SUAVE);
        l.textoDerecha("Recibo de la unidad de venta N° " + r.getUnidadId() + " - " + r.getUnidadCodigo(),
                l.ancho - MARGEN, 32, REGULAR, 8, TEXTO_SUAVE);
    }

    private static String nombre(String nombre, String apellido) {
        if (nombre == null && apellido == null) return "-";
        return (nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido);
    }

    private static String fecha(LocalDate fecha) {
        return fecha == null ? "-" : fecha.format(FECHA);
    }

    private static String moneda(BigDecimal monto) {
        return MONEDA.format(monto);
    }

    // Si el texto no entra en el ancho disponible se corta con "..."
    private static String recortar(String texto, PDFont fuente, float tamanio, float anchoMaximo) throws IOException {
        String limpio = Lienzo.encodable(texto, fuente);
        if (Lienzo.anchoDe(limpio, fuente, tamanio) <= anchoMaximo) return limpio;
        while (limpio.length() > 1 && Lienzo.anchoDe(limpio + "...", fuente, tamanio) > anchoMaximo) {
            limpio = limpio.substring(0, limpio.length() - 1);
        }
        return limpio + "...";
    }

    /** Primitivas de dibujo sobre la página, con coordenadas de PDF (el origen está abajo a la izquierda). */
    private static class Lienzo {
        private final PDPageContentStream stream;
        private final float ancho;
        private final float alto;

        Lienzo(PDPageContentStream stream, float ancho, float alto) {
            this.stream = stream;
            this.ancho = ancho;
            this.alto = alto;
        }

        void texto(String texto, float x, float y, PDFont fuente, float tamanio, Color color) throws IOException {
            stream.beginText();
            stream.setNonStrokingColor(color);
            stream.setFont(fuente, tamanio);
            stream.newLineAtOffset(x, y);
            stream.showText(encodable(texto, fuente));
            stream.endText();
        }

        void textoDerecha(String texto, float xDerecha, float y, PDFont fuente, float tamanio, Color color) throws IOException {
            String limpio = encodable(texto, fuente);
            texto(limpio, xDerecha - anchoDe(limpio, fuente, tamanio), y, fuente, tamanio, color);
        }

        void rectangulo(float x, float y, float w, float h, Color relleno) throws IOException {
            stream.setNonStrokingColor(relleno);
            stream.addRect(x, y, w, h);
            stream.fill();
        }

        void borde(float x, float y, float w, float h, Color color) throws IOException {
            stream.setStrokingColor(color);
            stream.setLineWidth(1);
            stream.addRect(x, y, w, h);
            stream.stroke();
        }

        void linea(float x1, float y1, float x2, float y2, Color color) throws IOException {
            stream.setStrokingColor(color);
            stream.setLineWidth(0.8f);
            stream.moveTo(x1, y1);
            stream.lineTo(x2, y2);
            stream.stroke();
        }

        static float anchoDe(String texto, PDFont fuente, float tamanio) throws IOException {
            return fuente.getStringWidth(texto) / 1000 * tamanio;
        }

        // Las fuentes estándar del PDF no tienen todos los caracteres: los que no están se reemplazan por "?"
        static String encodable(String texto, PDFont fuente) {
            if (texto == null || texto.isBlank()) return "-";
            StringBuilder resultado = new StringBuilder();
            texto.codePoints().forEach(c -> {
                String caracter = new String(Character.toChars(c));
                try {
                    fuente.encode(caracter);
                    resultado.append(caracter);
                } catch (IOException | IllegalArgumentException e) {
                    resultado.append('?');
                }
            });
            return resultado.toString();
        }
    }
}
