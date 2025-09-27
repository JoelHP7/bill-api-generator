package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.model.Cliente;
import com.bill_api_generator.bill_api_generator.model.InvoiceRequest;
import com.bill_api_generator.bill_api_generator.model.InvoiceTotals;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00€");

    public InvoiceTotals calcularTotales(InvoiceRequest req) {
        double subtotal = req.getImporteLinea();
        double ret = round2(subtotal * (req.getPorcentajeIrpf() / 100.0));
        double iva = round2(subtotal * (req.getPorcentajeIva() / 100.0));
        double total = round2(subtotal - ret + iva);
        return new InvoiceTotals(round2(subtotal), ret, iva, total);
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    /* ===========================
       Generación DOCX (Apache POI)
       =========================== */
    public byte[] generarDocx(InvoiceRequest req) throws IOException {
        var totals = calcularTotales(req);

        try (XWPFDocument doc = new XWPFDocument()) {

            // Título "Factura" (opcional)
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun rTitle = title.createRun();
            rTitle.setText("Factura");
            rTitle.setBold(true);
            rTitle.setFontSize(14);

            // Bloque emisor
            addLines(doc,
                    req.getEmisorNombre(),
                    "Dirección " + req.getEmisorDireccion(),
                    "CP " + req.getEmisorCp(),
                    "NIF " + req.getEmisorNif()
            );
            if (req.isMostrarIban() && req.getIban() != null && !req.getIban().isBlank()) {
                addLines(doc, "IBAN " + req.getIban());
            }
            addBlank(doc);

            // Nº factura y fecha
            addLines(doc,
                    "Nº DE FACTURA: " + req.getNumeroFactura(),
                    "FECHA: " + DF.format(req.getFecha())
            );
            addBlank(doc);

            // Para (clientes)
            XWPFParagraph pPara = doc.createParagraph();
            XWPFRun rPara = pPara.createRun();
            rPara.setBold(true);
            rPara.setText("Para");
            for (Cliente c : req.getPara()) {
                addLines(doc,
                        c.getNombre(),
                        c.getCp(),
                        c.getCif(),
                        c.getDireccion()
                );
            }
            addBlank(doc);

            // Tabla Descripción / Importe
            XWPFParagraph pDescTitle = doc.createParagraph();
            XWPFRun rDescTitle = pDescTitle.createRun();
            rDescTitle.setBold(true);
            rDescTitle.setText("DESCRIPCIÓN");

            XWPFParagraph pImpTitle = doc.createParagraph();
            pImpTitle.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun rImpTitle = pImpTitle.createRun();
            rImpTitle.setBold(true);
            rImpTitle.setText("IMPORTE");

            XWPFTable table = doc.createTable(1, 2);
            table.setWidth("100%");
            // Fila línea única
            setCellText(table.getRow(0).getCell(0), req.getDescripcionLinea());
            setCellTextRight(table.getRow(0).getCell(1), MONEY.format(req.getImporteLinea()));

            addBlank(doc);

            // Pie (Subtotal, IRPF, IVA, Total)
            addKeyValueRight(doc, "SUBTOTAL", MONEY.format(totals.getSubtotal()));
            addKeyValueRight(doc, "RETENCION I.R.P.F. (" + (int) req.getPorcentajeIrpf() + "%)", MONEY.format(totals.getRetencionIrpf()));
            addKeyValueRight(doc, "IVA (" + (int) req.getPorcentajeIva() + "%)", MONEY.format(totals.getIva()));
            addKeyValueRightBold(doc, "TOTAL", MONEY.format(totals.getTotal()));

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                doc.write(baos);
                return baos.toByteArray();
            }
        }
    }

    private void addLines(XWPFDocument doc, String... lines) {
        for (String line : lines) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            r.setText(line);
        }
    }
    private void addBlank(XWPFDocument doc) { addLines(doc, ""); }

    private void setCellText(XWPFTableCell cell, String text) {
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
    }
    private void setCellTextRight(XWPFTableCell cell, String text) {
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun r = p.createRun();
        r.setText(text);
    }
    private void addKeyValueRight(XWPFDocument doc, String key, String value) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun r = p.createRun();
        r.setText(key + " " + value);
    }
    private void addKeyValueRightBold(XWPFDocument doc, String key, String value) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setText(key + " " + value);
    }

    /* =======================
       Generación PDF (OpenPDF)
       ======================= */
    public byte[] generarPdf(InvoiceRequest req) throws IOException, DocumentException {
        var totals = calcularTotales(req);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            var fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            var fontBold  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            var fontNorm  = FontFactory.getFont(FontFactory.HELVETICA, 11);

            doc.add(new Paragraph("Factura", fontTitle));
            doc.add(Chunk.NEWLINE);

            // Emisor
            doc.add(new Paragraph(req.getEmisorNombre(), fontBold));
            doc.add(new Paragraph("Dirección " + req.getEmisorDireccion(), fontNorm));
            doc.add(new Paragraph("CP " + req.getEmisorCp(), fontNorm));
            doc.add(new Paragraph("NIF " + req.getEmisorNif(), fontNorm));
            if (req.isMostrarIban() && req.getIban() != null && !req.getIban().isBlank()) {
                doc.add(new Paragraph("IBAN " + req.getIban(), fontNorm));
            }
            doc.add(Chunk.NEWLINE);

            // Nº / Fecha
            doc.add(new Paragraph("Nº DE FACTURA: " + req.getNumeroFactura(), fontNorm));
            doc.add(new Paragraph("FECHA: " + DF.format(req.getFecha()), fontNorm));
            doc.add(Chunk.NEWLINE);

            // Para
            doc.add(new Paragraph("Para", fontBold));
            for (Cliente c : req.getPara()) {
                doc.add(new Paragraph(c.getNombre(), fontNorm));
                if (c.getCp() != null) doc.add(new Paragraph(c.getCp(), fontNorm));
                if (c.getCif() != null) doc.add(new Paragraph(c.getCif(), fontNorm));
                if (c.getDireccion() != null) doc.add(new Paragraph(c.getDireccion(), fontNorm));
                doc.add(Chunk.NEWLINE);
            }

            // Tabla Descripción/Importe (2 columnas)
            PdfPTable t = new PdfPTable(new float[]{4f, 2f});
            t.setWidthPercentage(100);

            PdfPCell c1 = new PdfPCell(new Phrase("DESCRIPCIÓN", fontBold));
            c1.setBorder(Rectangle.NO_BORDER);
            t.addCell(c1);
            PdfPCell c2 = new PdfPCell(new Phrase("IMPORTE", fontBold));
            c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c2.setBorder(Rectangle.NO_BORDER);
            t.addCell(c2);

            PdfPCell d1 = new PdfPCell(new Phrase(req.getDescripcionLinea(), fontNorm));
            d1.setBorder(Rectangle.NO_BORDER);
            t.addCell(d1);
            PdfPCell d2 = new PdfPCell(new Phrase(MONEY.format(req.getImporteLinea()), fontNorm));
            d2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            d2.setBorder(Rectangle.NO_BORDER);
            t.addCell(d2);

            doc.add(t);
            doc.add(Chunk.NEWLINE);

            // Totales (alineados a la derecha)
            doc.add(rowRight("SUBTOTAL", totals.getSubtotal(), fontNorm, fontNorm));
            doc.add(rowRight("RETENCION I.R.P.F. (" + (int) req.getPorcentajeIrpf() + "%)", totals.getRetencionIrpf(), fontNorm, fontNorm));
            doc.add(rowRight("IVA (" + (int) req.getPorcentajeIva() + "%)", totals.getIva(), fontNorm, fontNorm));
            doc.add(rowRightBold("TOTAL", totals.getTotal(), fontBold, fontBold));

            doc.close();
            return baos.toByteArray();
        }
    }

    private Paragraph rowRight(String key, double value, Font fk, Font fv) {
        Paragraph p = new Paragraph(key + " " + MONEY.format(value), fv);
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }
    private Paragraph rowRightBold(String key, double value, Font fk, Font fv) {
        Paragraph p = new Paragraph(key + " " + MONEY.format(value), fv);
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }
}