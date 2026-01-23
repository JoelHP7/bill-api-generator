package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.FacturaDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class DocumentGeneratorService {

    private static final String TEMPLATE_PATH = "templates/plantilla_v2.docx";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DecimalFormat MONEY_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "ES"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        MONEY_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    /**
     * Genera un documento DOCX a partir de una FacturaDto
     * @param facturaDto DTO con todos los datos de la factura
     * @return ByteArrayOutputStream con el documento generado
     * @throws IOException si hay error al leer la plantilla o generar el documento
     */
    public ByteArrayOutputStream generateFacturaDocx(FacturaDto facturaDto) throws IOException {
        log.info("Generando documento DOCX para factura: {}", facturaDto.getNumeroFactura());

        // 1. Cargar la plantilla desde resources
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);

        try (InputStream templateStream = resource.getInputStream();
             XWPFDocument doc = new XWPFDocument(templateStream)) {

            // 2. Crear mapa de valores para reemplazar
            Map<String, String> values = buildReplacementMap(facturaDto);

            // 3. Reemplazar en párrafos
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                replaceInParagraph(paragraph, values);
            }

            // 4. Reemplazar en tablas
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replaceInParagraph(paragraph, values);
                        }
                    }
                }
            }

            // 5. Escribir el documento a un ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            doc.write(outputStream);

            log.info("Documento DOCX generado exitosamente para factura: {}", facturaDto.getNumeroFactura());
            return outputStream;

        } catch (IOException e) {
            log.error("Error al generar documento DOCX para factura: {}", facturaDto.getNumeroFactura(), e);
            throw e;
        }
    }

    /**
     * Construye el mapa de reemplazos a partir del FacturaDto
     */
    private Map<String, String> buildReplacementMap(FacturaDto facturaDto) {
        Map<String, String> values = new HashMap<>();

        // Datos del emisor
        values.put("EMISOR_NOMBRE", facturaDto.getEmisorNombre());

        // Datos de la factura
        values.put("NUMERO_FACTURA", facturaDto.getNumeroFactura());
        values.put("FECHA_FACTURA", facturaDto.getFecha().format(DATE_FORMATTER));

        // Datos del cliente
        values.put("NOMBRE_CLIENTE", facturaDto.getNombreCliente());
        values.put("CIF_CLIENTE", facturaDto.getCifCliente());
        values.put("DIRECCION_CLIENTE", facturaDto.getDireccionCliente());

        // Descripción y horas
        values.put("MES_FACTURA", facturaDto.getMesFact());
        values.put("HORAS_FACTURA", String.valueOf(facturaDto.getHoras()));

        // Importes formateados
        values.put("IMPONIBLE_FACTURA", formatMoney(facturaDto.getImponible()));
        values.put("IRPF_FACTURA", formatMoney(facturaDto.getIrpf()));
        values.put("IVA_FACTURA", formatMoney(facturaDto.getIva()));
        values.put("TOTAL_FACTURA", formatMoney(facturaDto.getTotal()));

        return values;
    }

    /**
     * Formatea un número como moneda española (1.234,56€)
     */
    private String formatMoney(java.math.BigDecimal amount) {
        if (amount == null) {
            return "0,00€";
        }
        return MONEY_FORMAT.format(amount) + "€";
    }

    /**
     * Reemplaza los placeholders en un párrafo manteniendo el formato
     */
    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> values) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        // Obtener todo el texto del párrafo
        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String runText = run.getText(0);
            if (runText != null) {
                fullText.append(runText);
            }
        }

        String text = fullText.toString();
        if (text.isEmpty()) return;

        // Verificar si hay algo que reemplazar
        boolean hasReplacement = false;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            if (text.contains(placeholder)) {
                text = text.replace(placeholder, entry.getValue());
                hasReplacement = true;
            }
        }

        if (!hasReplacement) return;

        // Copiar formato del primer run ANTES de eliminar
        XWPFRun firstRun = runs.get(0);
        RunProperties savedFormat = copyRunFormat(firstRun);

        // Eliminar todos los runs
        for (int i = runs.size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }

        // Crear nuevo run con el texto reemplazado y formato preservado
        XWPFRun newRun = paragraph.createRun();
        newRun.setText(text, 0);
        applyRunFormat(newRun, savedFormat);
    }

    /**
     * Copia las propiedades de formato de un run
     */
    private RunProperties copyRunFormat(XWPFRun source) {
        RunProperties props = new RunProperties();

        try {
            props.fontFamily = source.getFontFamily();
        } catch (Exception e) {
            props.fontFamily = null;
        }

        try {
            props.fontSize = source.getFontSize();
        } catch (Exception e) {
            props.fontSize = -1;
        }

        try {
            props.bold = source.isBold();
        } catch (Exception e) {
            props.bold = false;
        }

        try {
            props.italic = source.isItalic();
        } catch (Exception e) {
            props.italic = false;
        }

        try {
            props.strike = source.isStrikeThrough();
        } catch (Exception e) {
            props.strike = false;
        }

        try {
            props.underline = source.getUnderline();
        } catch (Exception e) {
            props.underline = UnderlinePatterns.NONE;
        }

        try {
            props.color = source.getColor();
        } catch (Exception e) {
            props.color = null;
        }

        return props;
    }

    /**
     * Aplica las propiedades de formato a un run
     */
    private void applyRunFormat(XWPFRun target, RunProperties props) {
        if (props.fontFamily != null) {
            target.setFontFamily(props.fontFamily);
        }

        if (props.fontSize > 0) {
            target.setFontSize(props.fontSize);
        }

        target.setBold(props.bold);
        target.setItalic(props.italic);
        target.setStrike(props.strike);

        if (props.underline != null && props.underline != UnderlinePatterns.NONE) {
            target.setUnderline(props.underline);
        }

        if (props.color != null) {
            target.setColor(props.color);
        }
    }

    /**
     * Clase interna para almacenar propiedades de formato de un run
     */
    private static class RunProperties {
        String fontFamily;
        int fontSize;
        boolean bold;
        boolean italic;
        boolean strike;
        UnderlinePatterns underline;
        String color;
    }
}
