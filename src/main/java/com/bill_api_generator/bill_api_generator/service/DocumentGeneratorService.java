package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.InvoiceDto;
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

/**
 * Service for generating documents from templates.
 * Handles DOCX generation using templates with placeholder replacement.
 */
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
     * Generates a DOCX document from an InvoiceDto.
     *
     * @param invoiceDto DTO with all invoice data
     * @return ByteArrayOutputStream with the generated document
     * @throws IOException if there's an error reading the template or generating the document
     */
    public ByteArrayOutputStream generateInvoiceDocx(InvoiceDto invoiceDto) throws IOException {
        log.info("Generating DOCX document for invoice: {}", invoiceDto.getInvoiceNumber());

        // 1. Load template from resources
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);

        try (InputStream templateStream = resource.getInputStream();
             XWPFDocument doc = new XWPFDocument(templateStream)) {

            // 2. Create replacement values map
            Map<String, String> values = buildReplacementMap(invoiceDto);

            // 3. Replace in paragraphs
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                replaceInParagraph(paragraph, values);
            }

            // 4. Replace in tables
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replaceInParagraph(paragraph, values);
                        }
                    }
                }
            }

            // 5. Write document to ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            doc.write(outputStream);

            log.info("DOCX document generated successfully for invoice: {}", invoiceDto.getInvoiceNumber());
            return outputStream;

        } catch (IOException e) {
            log.error("Error generating DOCX document for invoice: {}", invoiceDto.getInvoiceNumber(), e);
            throw e;
        }
    }

    /**
     * Builds the replacement map from the InvoiceDto.
     */
    private Map<String, String> buildReplacementMap(InvoiceDto invoiceDto) {
        Map<String, String> values = new HashMap<>();

        // Issuer data
        values.put("EMISOR_NOMBRE", invoiceDto.getIssuerName());

        // Invoice data
        values.put("NUMERO_FACTURA", invoiceDto.getInvoiceNumber());
        values.put("FECHA_FACTURA", invoiceDto.getDate().format(DATE_FORMATTER));

        // Client data
        values.put("NOMBRE_CLIENTE", invoiceDto.getClientName());
        values.put("CIF_CLIENTE", invoiceDto.getClientTaxId());
        values.put("DIRECCION_CLIENTE", invoiceDto.getClientAddress());

        // Description and hours
        values.put("MES_FACTURA", invoiceDto.getBillingMonth());
        values.put("HORAS_FACTURA", String.valueOf(invoiceDto.getHours()));

        // Formatted amounts
        values.put("IMPONIBLE_FACTURA", formatMoney(invoiceDto.getSubtotal()));
        values.put("IRPF_FACTURA", formatMoney(invoiceDto.getTaxWithholding()));
        values.put("IVA_FACTURA", formatMoney(invoiceDto.getVat()));
        values.put("TOTAL_FACTURA", formatMoney(invoiceDto.getTotal()));

        return values;
    }

    /**
     * Formats a number as Spanish currency (1.234,56€).
     */
    private String formatMoney(java.math.BigDecimal amount) {
        if (amount == null) {
            return "0,00€";
        }
        return MONEY_FORMAT.format(amount) + "€";
    }

    /**
     * Replaces placeholders in a paragraph while maintaining formatting.
     */
    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> values) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        // Get all text from the paragraph
        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String runText = run.getText(0);
            if (runText != null) {
                fullText.append(runText);
            }
        }

        String text = fullText.toString();
        if (text.isEmpty()) return;

        // Check if there's anything to replace
        boolean hasReplacement = false;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            if (text.contains(placeholder)) {
                text = text.replace(placeholder, entry.getValue());
                hasReplacement = true;
            }
        }

        if (!hasReplacement) return;

        // Copy format from first run BEFORE deleting
        XWPFRun firstRun = runs.get(0);
        RunProperties savedFormat = copyRunFormat(firstRun);

        // Delete all runs
        for (int i = runs.size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }

        // Create new run with replaced text and preserved format
        XWPFRun newRun = paragraph.createRun();
        newRun.setText(text, 0);
        applyRunFormat(newRun, savedFormat);
    }

    /**
     * Copies format properties from a run.
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
     * Applies format properties to a run.
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
     * Inner class to store run format properties.
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
