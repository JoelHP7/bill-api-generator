package com.bill_api_generator.bill_api_generator;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rellenador de facturas con plantillas PDF usando marcadores {{VARIABLE}}
 *
 * Esta clase busca los marcadores en el PDF original y los reemplaza
 * dibujando rectángulos blancos sobre ellos y escribiendo el nuevo texto.
 */
public class PdfTemplateFiller {

    /**
     * Clase para almacenar la posición de un marcador en el PDF
     */
    static class MarkerPosition {
        String marker;
        int pageNumber;
        float x;
        float y;
        float width;
        float height;

        public MarkerPosition(String marker, int pageNumber, float x, float y, float width, float height) {
            this.marker = marker;
            this.pageNumber = pageNumber;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * TextStripper personalizado para encontrar las posiciones de los marcadores
     */
    static class MarkerLocator extends PDFTextStripper {
        private List<MarkerPosition> positions = new ArrayList<>();
        private int currentPage = 0;

        public MarkerLocator() throws IOException {
            super();
        }

        @Override
        protected void startPage(PDPage page) throws IOException {
            currentPage++;
            super.startPage(page);
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            // Buscar marcadores en el texto
            Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                String marker = matcher.group(0); // Incluye las llaves {{}}
                int startIndex = matcher.start();
                int endIndex = matcher.end();

                if (startIndex < textPositions.size() && endIndex <= textPositions.size()) {
                    TextPosition first = textPositions.get(startIndex);
                    TextPosition last = textPositions.get(endIndex - 1);

                    float x = first.getX();
                    float y = first.getY();
                    float width = last.getX() + last.getWidth() - x;
                    float height = first.getHeight();

                    positions.add(new MarkerPosition(marker, currentPage, x, y, width, height));
                }
            }

            super.writeString(text, textPositions);
        }

        public List<MarkerPosition> getPositions() {
            return positions;
        }
    }

    /**
     * Rellena una plantilla PDF con los valores proporcionados
     *
     * @param templatePath Ruta al archivo PDF plantilla
     * @param outputPath Ruta donde guardar el PDF rellenado
     * @param values Mapa con los valores para cada marcador (sin las llaves {{}})
     * @throws IOException Si hay error al procesar el PDF
     */
    public static void fillTemplate(String templatePath, String outputPath, Map<String, String> values) throws IOException {

        // 1. Cargar el documento original
        PDDocument document = Loader.loadPDF(new File(templatePath));

        // 2. Encontrar las posiciones de todos los marcadores
        MarkerLocator locator = new MarkerLocator();
        locator.setSortByPosition(true);
        locator.getText(document);
        List<MarkerPosition> positions = locator.getPositions();

        System.out.println("Marcadores encontrados: " + positions.size());

        // 3. Agrupar marcadores por página
        Map<Integer, List<MarkerPosition>> markersByPage = new HashMap<>();
        for (MarkerPosition pos : positions) {
            markersByPage.computeIfAbsent(pos.pageNumber, k -> new ArrayList<>()).add(pos);
        }

        // 4. Para cada página, cubrir los marcadores y escribir los nuevos valores
        for (Map.Entry<Integer, List<MarkerPosition>> entry : markersByPage.entrySet()) {
            int pageNum = entry.getKey();
            List<MarkerPosition> pageMarkers = entry.getValue();

            PDPage page = document.getPage(pageNum - 1); // PDFBox usa índice base 0
            PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true, true
            );

            for (MarkerPosition pos : pageMarkers) {
                // Extraer el nombre del marcador sin llaves
                String markerName = pos.marker.replaceAll("[{}]", "");
                String value = values.getOrDefault(markerName, pos.marker);

                System.out.println("Reemplazando " + pos.marker + " con: " + value);

                // Cubrir el marcador original con un rectángulo blanco
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.addRect(pos.x - 2, pos.y - 2, pos.width + 4, pos.height + 4);
                contentStream.fill();

                // Escribir el nuevo valor
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.newLineAtOffset(pos.x, pos.y);
                contentStream.showText(value);
                contentStream.endText();
            }

            contentStream.close();
        }

        // 5. Guardar el documento modificado
        document.save(outputPath);
        document.close();

        System.out.println("PDF generado exitosamente: " + outputPath);
    }

    /**
     * Ejemplo de uso
     */
    public static void main(String[] args) {
        try {
            // Preparar los datos de la factura
            Map<String, String> facturaData = new HashMap<>();
            facturaData.put("EMISOR_NOMBRE", "Juan Pérez García");
            facturaData.put("NUMERO_FACTURA", "F2024-001");
            facturaData.put("FECHA_FACTURA", "22/01/2024");
            facturaData.put("NOMBRE_CLIENTE", "AKKODIS TECHNOLOGIES SPAIN S.L.U");
            facturaData.put("CIF_CLIENTE", "B82040824");
            facturaData.put("DIRECCION_CLIENTE", "Calle Ejemplo, 123, 28001 Madrid");
            facturaData.put("MES_FACTURA", "Enero 2024");
            facturaData.put("HORAS_FACTURA", "160");
            facturaData.put("IMPONIBLE_FACTURA", "8.000,00 EUR");
            facturaData.put("IRPF_FACTURA", "560,00 EUR");
            facturaData.put("IVA_FACTURA", "1.680,00 EUR");
            facturaData.put("TOTAL_FACTURA", "9.120,00 EUR");

            // Rellenar la plantilla
            fillTemplate(
                    "C:/workspace/plantilla_v2.pdf",
                    "C:/workspace/factura_rellenada.pdf",
                    facturaData
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}