package com.bill_api_generator.bill_api_generator;

import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocxPlaceholdersExampleV2 {

    public static void main(String[] args) throws Exception {
        // 1. Abrir plantilla
        try (FileInputStream fis = new FileInputStream("C:/workspace/plantilla_v2.docx");
             XWPFDocument doc = new XWPFDocument(fis)) {

            // 2. Mapa de reemplazos
            Map<String, String> values = new HashMap<>();
            values.put("EMISOR_NOMBRE", "JOEL HERNANDEZ PLA");
            values.put("NUMERO_FACTURA", "52");
            values.put("FECHA_FACTURA", "28-09-2025");
            values.put("NOMBRE_CLIENTE", "AXPE CONSULTING, S.L.");
            values.put("CIF_CLIENTE", "B84184548");
            values.put("DIRECCION_CLIENTE", "Calle Arturo Soria 122, Madrid");
            values.put("MES_FACTURA", "Septiembre 2025");
            values.put("HORAS_FACTURA", "152");
            values.put("IMPONIBLE_FACTURA", "6.080,00€");
            values.put("IRPF_FACTURA", "425,60€");
            values.put("IVA_FACTURA", "1.276,80€");
            values.put("TOTAL_FACTURA", "7.056,00€");

            // 3. Reemplazar en párrafos
            for (XWPFParagraph p : doc.getParagraphs()) {
                replaceInParagraph(p, values);
            }

            // 4. Reemplazar en tablas
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            replaceInParagraph(p, values);
                        }
                    }
                }
            }

            String path = "C:/workspace/factura-52-v3.docx";
            // 5. Guardar resultado en fichero nuevo
            try (FileOutputStream fos = new FileOutputStream(path)) {
                doc.write(fos);
            }

        }
    }


    private static void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> values) {
        // Primero, reconstruir el párrafo completo en un solo run
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        // Obtener todo el texto
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
        for (Map.Entry<String, String> e : values.entrySet()) {
            String key = "{{" + e.getKey() + "}}";
            if (text.contains(key)) {
                text = text.replace(key, e.getValue());
                hasReplacement = true;
            }
        }

        if (!hasReplacement) return;

        // 🔑 CLAVE: Copiar formato ANTES de eliminar los runs
        XWPFRun firstRun = runs.get(0);
        RunProperties savedFormat = copyRunFormatToProperties(firstRun);

        // Ahora sí, eliminar todos los runs
        for (int i = runs.size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }

        // Crear nuevo run con formato preservado
        XWPFRun newRun = paragraph.createRun();
        newRun.setText(text, 0);
        applyRunFormat(newRun, savedFormat);
    }

    /**
     * Clase auxiliar para guardar propiedades de formato
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

    /**
     * Copia el formato de un run a un objeto de propiedades
     * IMPORTANTE: Hace esto ANTES de que el run sea eliminado
     */
    private static RunProperties copyRunFormatToProperties(XWPFRun source) {
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
     * Aplica el formato guardado a un run nuevo
     */
    private static void applyRunFormat(XWPFRun target, RunProperties props) {
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

}