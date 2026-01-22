package com.bill_api_generator.bill_api_generator;

import org.apache.poi.xwpf.usermodel.*;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.*;
import java.util.*;

public class DocxPlaceholdersExample {

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

            String path = "C:/workspace/factura-52-v2.docx";
            // 5. Guardar resultado en fichero nuevo
            try (FileOutputStream fos = new FileOutputStream(path)) {
                doc.write(fos);
            }

//            topdf(path);
        }
    }

    private static void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> values) {
        String text = paragraph.getText();
        if (text == null) return;

        for (Map.Entry<String, String> e : values.entrySet()) {
            String key = "{{" + e.getKey() + "}}";
            if (text.contains(key)) {
                text = text.replace(key, e.getValue());
            }
        }

        // eliminar runs antiguos y dejar uno nuevo
        int runs = paragraph.getRuns().size();
        for (int i = runs - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }
        XWPFRun run = paragraph.createRun();
        run.setText(text, 0);
    }

    private static void topdf(String path) throws Exception {
        // Cargar el DOCX ya generado
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
                .load(new File(path));

        // Exportar a PDF
        Docx4J.toPDF(wordMLPackage, new FileOutputStream(path.replace(".docx", ".pdf")));
    }
}