package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.model.Cliente;
import com.bill_api_generator.bill_api_generator.model.InvoiceRequest;
import com.bill_api_generator.bill_api_generator.model.InvoiceTotals;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
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
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabStop;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabs;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabJc;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static com.bill_api_generator.bill_api_generator.constants.InvoiceStyle.COLOR_BG_EMISOR;
import static com.bill_api_generator.bill_api_generator.constants.InvoiceStyle.COLOR_BG_PARA;
import static com.bill_api_generator.bill_api_generator.constants.InvoiceStyle.COLOR_BG_TH;
import static com.bill_api_generator.bill_api_generator.constants.InvoiceStyle.FONT;
import static com.bill_api_generator.bill_api_generator.constants.InvoiceStyle.FONT_SIZE;
import static com.bill_api_generator.bill_api_generator.constants.InvoiceStyle.FONT_SIZE_TITLE;
import static com.bill_api_generator.bill_api_generator.constants.InvoiceStyle.PAGE_LEFT_TWIPS;
import static com.bill_api_generator.bill_api_generator.constants.InvoiceStyle.PAGE_RIGHT_TWIPS;
import static com.bill_api_generator.bill_api_generator.constants.InvoiceStyle.TAB_RIGHT_TWIPS;

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
    private void setDocMargins(XWPFDocument doc) {
        CTSectPr sectPr = doc.getDocument().getBody().isSetSectPr()
                ? doc.getDocument().getBody().getSectPr()
                : doc.getDocument().getBody().addNewSectPr();
        CTPageMar mar = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();
        mar.setLeft(BigInteger.valueOf(PAGE_LEFT_TWIPS));
        mar.setRight(BigInteger.valueOf(PAGE_RIGHT_TWIPS));
    }

    // Crea un párrafo con alineación y estilo
    private XWPFParagraph para(XWPFDocument d, ParagraphAlignment al, boolean bold, Integer fontSize, String... lines) {
        XWPFParagraph p = d.createParagraph();
        p.setAlignment(al);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] == null) continue;
            XWPFRun r = p.createRun();
            r.setFontFamily(FONT);
            r.setFontSize(fontSize != null ? fontSize : FONT_SIZE);
            r.setBold(bold);
            r.setText(lines[i]);
            if (i < lines.length - 1) r.addBreak();
        }
        return p;
    }

    // Bloque sombreado mediante tabla de 1 celda (forma robusta en POI 5.2.5)
    private void shadedBlockBox(XWPFDocument doc, String bgHex, ArrayList<String> lines) {
        XWPFTable box = doc.createTable(1, 1);
        box.setWidth("100%");
        XWPFTableCell c = box.getRow(0).getCell(0);
        c.setColor(bgHex);                // color de fondo del bloque
        c.removeParagraph(0);
        XWPFParagraph p = c.addParagraph();
        p.setAlignment(ParagraphAlignment.LEFT);

        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i);
            if (text == null) continue;
            XWPFRun r = p.createRun();
            r.setFontFamily(FONT);
            r.setFontSize(FONT_SIZE);
            r.setText(text);
            if (i < lines.size() - 1) r.addBreak();
        }
    }

    // Cabecera de tabla con fondo
    private void addTableHeader(XWPFTable t, String bgHex, String left, String right) {
        XWPFTableRow header = t.getRow(0);
        styleHeaderCell(header.getCell(0), bgHex, left, false);
        styleHeaderCell(header.getCell(1), bgHex, right, true);
    }
    private void styleHeaderCell(XWPFTableCell cell, String bgHex, String text, boolean alignRight) {
        cell.setColor(bgHex);
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(alignRight ? ParagraphAlignment.RIGHT : ParagraphAlignment.LEFT);
        XWPFRun r = p.createRun();
        r.setFontFamily(FONT);
        r.setFontSize(FONT_SIZE);
        r.setBold(true);
        r.setText(text);
    }

    private void setCellText(XWPFTableCell cell, String text, boolean alignRight, boolean bold) {
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(alignRight ? ParagraphAlignment.RIGHT : ParagraphAlignment.LEFT);
        XWPFRun r = p.createRun();
        r.setFontFamily(FONT);
        r.setFontSize(FONT_SIZE);
        r.setBold(bold);
        r.setText(text);
    }

    // Totales: clave ...[TAB]... valor (tabulación derecha)
    private void addKeyValueRightWithTab(XWPFDocument doc, String key, String value, boolean bold) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.LEFT);

        CTPPr ppr = p.getCTP().isSetPPr() ? p.getCTP().getPPr() : p.getCTP().addNewPPr();
        CTTabs tabs = ppr.isSetTabs() ? ppr.getTabs() : ppr.addNewTabs();
        CTTabStop tab = tabs.addNewTab();
        tab.setVal(STTabJc.RIGHT);
        tab.setPos(BigInteger.valueOf(TAB_RIGHT_TWIPS));

        XWPFRun r1 = p.createRun();
        r1.setFontFamily(FONT);
        r1.setFontSize(FONT_SIZE);
        r1.setBold(bold);
        r1.setText(key);

        XWPFRun r2 = p.createRun();
        r2.addTab(); // salta a la tabulación derecha
        r2.setFontFamily(FONT);
        r2.setFontSize(FONT_SIZE);
        r2.setBold(bold);
        r2.setText(value);
    }

    // Método principal DOCX
    public Path generarDocx(InvoiceRequest req) throws IOException {
        var totals = calcularTotales(req);

        try (XWPFDocument doc = new XWPFDocument()) {
            setDocMargins(doc);

            // Título
            para(doc, ParagraphAlignment.LEFT, true, FONT_SIZE_TITLE, "Factura");

            // Bloque Emisor (sombreado)
            ArrayList<String> emisorLines = new ArrayList<>();
            emisorLines.add(req.getEmisorNombre());
            emisorLines.add("Dirección " + req.getEmisorDireccion());
            emisorLines.add("CP " + req.getEmisorCp());
            emisorLines.add("NIF " + req.getEmisorNif());
            if (req.isMostrarIban() && req.getIban() != null && !req.getIban().isBlank()) {
                emisorLines.add("IBAN " + req.getIban());
            }
            shadedBlockBox(doc, COLOR_BG_EMISOR, emisorLines);

            para(doc, ParagraphAlignment.LEFT, false, null, ""); // espacio

            // Nº y Fecha
            para(doc, ParagraphAlignment.LEFT, false, null,
                    "Nº DE FACTURA: " + req.getNumeroFactura(),
                    "FECHA: " + DF.format(req.getFecha())
            );

            para(doc, ParagraphAlignment.LEFT, false, null, "");

            // Bloque “Para” (sombreado)
            ArrayList<String> paraLines = new ArrayList<>();
            paraLines.add("Para");
            for (Cliente c : req.getPara()) paraLines.add(c.getNombre());
            for (Cliente c : req.getPara()) {
                if (c.getCp() != null)        paraLines.add(c.getCp());
                if (c.getCif() != null)       paraLines.add(c.getCif());
                if (c.getDireccion() != null) paraLines.add(c.getDireccion());
            }
            shadedBlockBox(doc, COLOR_BG_PARA, paraLines);

            para(doc, ParagraphAlignment.LEFT, false, null, "");

            // Tabla Descripción / Importe
            XWPFTable table = doc.createTable(1, 2);
            table.setWidth("100%");
            addTableHeader(table, COLOR_BG_TH, "DESCRIPCIÓN", "IMPORTE");

            XWPFTableRow row = table.createRow();
            setCellText(row.getCell(0), req.getDescripcionLinea(), false, false);
            setCellText(row.getCell(1), MONEY.format(req.getImporteLinea()), true, false);

            para(doc, ParagraphAlignment.LEFT, false, null, "");

            // Totales (tabulación derecha)
            addKeyValueRightWithTab(doc, "SUBTOTAL", MONEY.format(totals.getSubtotal()), false);
            addKeyValueRightWithTab(doc, "RETENCION I.R.P.F. (" + (int) req.getPorcentajeIrpf() + "%)", MONEY.format(totals.getRetencionIrpf()), false);
            addKeyValueRightWithTab(doc, "IVA (" + (int) req.getPorcentajeIva() + "%)", MONEY.format(totals.getIva()), false);
            addKeyValueRightWithTab(doc, "TOTAL", MONEY.format(totals.getTotal()), true);

            Path outPath = Paths.get("C:/workspace/factura-" + req.getNumeroFactura() + ".docx");
            try (FileOutputStream fos = new FileOutputStream(outPath.toFile())) {
                doc.write(fos);
            }
            return outPath;
        }
    }

    /* =======================
       Generación PDF (OpenPDF)
       ======================= */
    private static Color hex(String h) {
        return new Color(
                Integer.valueOf(h.substring(0,2),16),
                Integer.valueOf(h.substring(2,4),16),
                Integer.valueOf(h.substring(4,6),16)
        );
    }

    public Path generarPdf(InvoiceRequest req) throws Exception {
        var totals = calcularTotales(req);
        Path outPath = Paths.get("C:/workspace/factura-" + req.getNumeroFactura() + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(outPath.toFile())) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, fos);
            doc.open();

            Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, FONT_SIZE_TITLE);
            Font fBold  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, FONT_SIZE);
            Font fNorm  = FontFactory.getFont(FontFactory.HELVETICA, FONT_SIZE);

            doc.add(new Paragraph("Factura", fTitle));
            doc.add(Chunk.NEWLINE);

            // Bloque Emisor sombreado
            PdfPTable tEmisor = new PdfPTable(1); tEmisor.setWidthPercentage(100);
            PdfPCell cEm = new PdfPCell();
            cEm.setBackgroundColor(hex(COLOR_BG_EMISOR));
            cEm.setPadding(8f);
            Phrase ph = new Phrase();
            ph.add(new Chunk(req.getEmisorNombre() + "\n", fBold));
            ph.add(new Chunk("Dirección " + req.getEmisorDireccion() + "\n", fNorm));
            ph.add(new Chunk("CP " + req.getEmisorCp() + "\n", fNorm));
            ph.add(new Chunk("NIF " + req.getEmisorNif() + "\n", fNorm));
            if (req.isMostrarIban() && req.getIban()!=null && !req.getIban().isBlank())
                ph.add(new Chunk("IBAN " + req.getIban(), fNorm));
            cEm.addElement(new Paragraph(ph));
            cEm.setBorder(Rectangle.NO_BORDER);
            tEmisor.addCell(cEm);
            doc.add(tEmisor);

            doc.add(Chunk.NEWLINE);

            // Nº y fecha
            doc.add(new Paragraph("Nº DE FACTURA: " + req.getNumeroFactura(), fNorm));
            doc.add(new Paragraph("FECHA: " + DF.format(req.getFecha()), fNorm));
            doc.add(Chunk.NEWLINE);

            // Bloque PARA sombreado
            PdfPTable tPara = new PdfPTable(1); tPara.setWidthPercentage(100);
            PdfPCell cPara = new PdfPCell();
            cPara.setBackgroundColor(hex(COLOR_BG_PARA));
            cPara.setBorder(Rectangle.NO_BORDER);
            cPara.setPadding(8f);
            Paragraph pPara = new Paragraph("Para", fBold);
            cPara.addElement(pPara);
            for (Cliente c : req.getPara()) {
                cPara.addElement(new Paragraph(c.getNombre(), fNorm));
            }
            for (Cliente c : req.getPara()) { // info adicional, como en tu doc
                if (c.getCp()!=null)        cPara.addElement(new Paragraph(c.getCp(), fNorm));
                if (c.getCif()!=null)       cPara.addElement(new Paragraph(c.getCif(), fNorm));
                if (c.getDireccion()!=null) cPara.addElement(new Paragraph(c.getDireccion(), fNorm));
            }
            tPara.addCell(cPara);
            doc.add(tPara);

            doc.add(Chunk.NEWLINE);

            // Tabla DESCRIPCIÓN / IMPORTE
            PdfPTable t = new PdfPTable(new float[]{4f, 2f});
            t.setWidthPercentage(100);

            PdfPCell h1 = new PdfPCell(new Phrase("DESCRIPCIÓN", fBold));
            h1.setBackgroundColor(hex(COLOR_BG_TH));
            h1.setBorder(Rectangle.NO_BORDER);
            h1.setPadding(6f);
            t.addCell(h1);

            PdfPCell h2 = new PdfPCell(new Phrase("IMPORTE", fBold));
            h2.setBackgroundColor(hex(COLOR_BG_TH));
            h2.setBorder(Rectangle.NO_BORDER);
            h2.setPadding(6f);
            h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            t.addCell(h2);

            PdfPCell d1 = new PdfPCell(new Phrase(req.getDescripcionLinea(), fNorm));
            d1.setBorder(Rectangle.NO_BORDER);
            d1.setPadding(6f);
            t.addCell(d1);

            PdfPCell d2 = new PdfPCell(new Phrase(MONEY.format(req.getImporteLinea()), fNorm));
            d2.setBorder(Rectangle.NO_BORDER);
            d2.setPadding(6f);
            d2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            t.addCell(d2);

            doc.add(t);
            doc.add(Chunk.NEWLINE);

            // Totales (dos columnas, derecha alineada)
            PdfPTable tot = new PdfPTable(new float[]{4f, 2f});
            tot.setWidthPercentage(100);
            tot.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            addTotalRow(tot, "SUBTOTAL", totals.getSubtotal(), fNorm, false);
            addTotalRow(tot, "RETENCION I.R.P.F. (" + (int)req.getPorcentajeIrpf() + "%)", totals.getRetencionIrpf(), fNorm, false);
            addTotalRow(tot, "IVA (" + (int)req.getPorcentajeIva() + "%)", totals.getIva(), fNorm, false);
            addTotalRow(tot, "TOTAL", totals.getTotal(), fBold, true);

            doc.add(tot);
            doc.close();

            return outPath;
        }
    }

    private void addTotalRow(PdfPTable tot, String key, double val, Font f, boolean strong) {
        PdfPCell k = new PdfPCell(new Phrase(key, f));
        k.setBorder(Rectangle.NO_BORDER);
        k.setHorizontalAlignment(Element.ALIGN_RIGHT);
        k.setPadding(2f);
        tot.addCell(k);

        PdfPCell v = new PdfPCell(new Phrase(MONEY.format(val), f));
        v.setBorder(Rectangle.NO_BORDER);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setPadding(2f);
        tot.addCell(v);
    }
}