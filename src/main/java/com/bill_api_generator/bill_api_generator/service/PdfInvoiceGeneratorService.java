package com.bill_api_generator.bill_api_generator.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfInvoiceGeneratorService {

    public byte[] generateInvoicePdf(InvoiceGeneratorService.InvoiceData invoiceData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdfDocument = new PdfDocument(writer);
             Document document = new Document(pdfDocument)) {

            // Configurar márgenes más pequeños para que coincida con el original
            document.setMargins(20, 20, 20, 20);

            // Configurar fuentes
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Crear encabezado con información del emisor y número de factura
            createPdfHeader(document, invoiceData, regularFont, boldFont);

            // Espacio reducido entre secciones
            document.add(new Paragraph(" ").setFontSize(4).setMargin(0).setPadding(0));

            // Crear sección del destinatario
            createPdfRecipientSection(document, invoiceData, regularFont, boldFont);

            // Espacio reducido entre secciones
            document.add(new Paragraph(" ").setFontSize(4).setMargin(0).setPadding(0));

            // Crear tabla de conceptos
            createPdfInvoiceTable(document, invoiceData, regularFont, boldFont);

        }

        return outputStream.toByteArray();
    }

    public void savePdfInvoiceToFile(InvoiceGeneratorService.InvoiceData invoiceData, String filePath) throws IOException {
        byte[] pdfBytes = generateInvoicePdf(invoiceData);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfBytes);
        }
    }

    private void createPdfHeader(Document document, InvoiceGeneratorService.InvoiceData invoiceData,
                                 PdfFont regularFont, PdfFont boldFont) throws IOException {

        // Crear tabla para el encabezado (2 columnas) - Sin márgenes ni padding
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{65, 35}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setBorder(Border.NO_BORDER);
        headerTable.setMargin(0);
        headerTable.setPadding(0);

        // Columna izquierda - Datos del emisor (con fondo gris claro)
        Cell leftCell = new Cell();
        leftCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        leftCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        leftCell.setVerticalAlignment(VerticalAlignment.TOP);
        leftCell.setPadding(3);
        leftCell.setMargin(0);

        // Nombre del emisor
        leftCell.add(new Paragraph(invoiceData.getIssuerName())
                .setFont(boldFont)
                .setFontSize(10)
                .setMargin(0)
                .setPadding(0)
                .setFixedLeading(12));

        // Dirección
        leftCell.add(new Paragraph("Dirección " + invoiceData.getIssuerAddress())
                .setFont(boldFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0)
                .setFixedLeading(10));

        // Código postal
        leftCell.add(new Paragraph("CP " + invoiceData.getIssuerPostalCode())
                .setFont(boldFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0)
                .setFixedLeading(10));

        // NIF
        leftCell.add(new Paragraph("NIF " + invoiceData.getIssuerNIF())
                .setFont(boldFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0)
                .setFixedLeading(10));

        // IBAN
        leftCell.add(new Paragraph("IBAN " + invoiceData.getIssuerIBAN())
                .setFont(boldFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0)
                .setFixedLeading(10));

        // Columna derecha - Número de factura y fecha (con fondo gris claro)
        Cell rightCell = new Cell();
        rightCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        rightCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        rightCell.setVerticalAlignment(VerticalAlignment.TOP);
        rightCell.setTextAlignment(TextAlignment.LEFT);
        rightCell.setPadding(3);
        rightCell.setMargin(0);

        // Número de factura
        rightCell.add(new Paragraph("Nº DE FACTURA: " + invoiceData.getInvoiceNumber())
                .setFont(boldFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0)
                .setFixedLeading(10));

        // Espacio
        rightCell.add(new Paragraph(" ")
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0)
                .setFixedLeading(8));

        // Fecha
        rightCell.add(new Paragraph("FECHA: " + invoiceData.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .setFont(boldFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0)
                .setFixedLeading(10));

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);

        document.add(headerTable);
    }

    private void createPdfRecipientSection(Document document, InvoiceGeneratorService.InvoiceData invoiceData,
                                           PdfFont regularFont, PdfFont boldFont) throws IOException {

        // Crear tabla para la sección del destinatario con bordes finos y sin espaciado
        Table recipientTable = new Table(UnitValue.createPercentArray(new float[]{100}));
        recipientTable.setWidth(UnitValue.createPercentValue(100));
        recipientTable.setMargin(0);
        recipientTable.setPadding(0);

        // Para
        Cell paraCell = new Cell();
        paraCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        paraCell.setHeight(18);
        paraCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        paraCell.setPadding(2);
        paraCell.setMargin(0);
        paraCell.add(new Paragraph("Para")
                .setFont(boldFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));
        recipientTable.addCell(paraCell);

        // Nombre del cliente
        Cell clientNameCell = new Cell();
        clientNameCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        clientNameCell.setHeight(18);
        clientNameCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        clientNameCell.setPadding(2);
        clientNameCell.setMargin(0);
        clientNameCell.add(new Paragraph(invoiceData.getClientName())
                .setFont(regularFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));
        recipientTable.addCell(clientNameCell);

        // Nombre completo del cliente
        Cell fullNameCell = new Cell();
        fullNameCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        fullNameCell.setHeight(18);
        fullNameCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        fullNameCell.setPadding(2);
        fullNameCell.setMargin(0);
        fullNameCell.add(new Paragraph(invoiceData.getClientFullName())
                .setFont(boldFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));
        recipientTable.addCell(fullNameCell);

        // Líneas con números (cada una en su celda con altura uniforme)
        String[] numbers = {"0", "8", "2", "0", "4"};
        for (String number : numbers) {
            Cell numberCell = new Cell();
            numberCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
            numberCell.setHeight(18);
            numberCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            numberCell.setPadding(2);
            numberCell.setMargin(0);
            numberCell.add(new Paragraph(number)
                    .setFont(regularFont)
                    .setFontSize(8)
                    .setMargin(0)
                    .setPadding(0));
            recipientTable.addCell(numberCell);
        }

        // CIF
        Cell cifCell = new Cell();
        cifCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        cifCell.setHeight(18);
        cifCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        cifCell.setPadding(2);
        cifCell.setMargin(0);
        cifCell.add(new Paragraph(invoiceData.getClientCIF())
                .setFont(regularFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));
        recipientTable.addCell(cifCell);

        // Dirección del cliente
        Cell clientAddressCell = new Cell();
        clientAddressCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        clientAddressCell.setHeight(18);
        clientAddressCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        clientAddressCell.setPadding(2);
        clientAddressCell.setMargin(0);
        clientAddressCell.add(new Paragraph(invoiceData.getClientAddress())
                .setFont(regularFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));
        recipientTable.addCell(clientAddressCell);

        document.add(recipientTable);
    }

    private void createPdfInvoiceTable(Document document, InvoiceGeneratorService.InvoiceData invoiceData,
                                       PdfFont regularFont, PdfFont boldFont) throws IOException {

        // Crear tabla de conceptos (2 columnas: Descripción e Importe) con bordes finos
        Table conceptsTable = new Table(UnitValue.createPercentArray(new float[]{75, 25}));
        conceptsTable.setWidth(UnitValue.createPercentValue(100));
        conceptsTable.setMargin(0);
        conceptsTable.setPadding(0);

        // Encabezados con altura uniforme
        Cell descriptionHeader = new Cell();
        descriptionHeader.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        descriptionHeader.setHeight(20);
        descriptionHeader.setVerticalAlignment(VerticalAlignment.MIDDLE);
        descriptionHeader.setPadding(3);
        descriptionHeader.setMargin(0);
        descriptionHeader.add(new Paragraph("DESCRIPCIÓN")
                .setFont(boldFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));

        Cell amountHeader = new Cell();
        amountHeader.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        amountHeader.setHeight(20);
        amountHeader.setVerticalAlignment(VerticalAlignment.MIDDLE);
        amountHeader.setPadding(3);
        amountHeader.setMargin(0);
        amountHeader.add(new Paragraph("IMPORTE")
                .setFont(boldFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMargin(0)
                .setPadding(0));

        conceptsTable.addCell(descriptionHeader);
        conceptsTable.addCell(amountHeader);

        // Fila vacía con altura uniforme
        Cell emptyDesc1 = new Cell();
        emptyDesc1.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        emptyDesc1.setHeight(16);
        emptyDesc1.setVerticalAlignment(VerticalAlignment.MIDDLE);
        emptyDesc1.setPadding(2);
        emptyDesc1.setMargin(0);
        emptyDesc1.add(new Paragraph(" ")
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));

        Cell emptyAmount1 = new Cell();
        emptyAmount1.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        emptyAmount1.setHeight(16);
        emptyAmount1.setVerticalAlignment(VerticalAlignment.MIDDLE);
        emptyAmount1.setPadding(2);
        emptyAmount1.setMargin(0);
        emptyAmount1.add(new Paragraph(" ")
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));

        conceptsTable.addCell(emptyDesc1);
        conceptsTable.addCell(emptyAmount1);

        // Descripción del servicio con altura uniforme
        Cell serviceCell = new Cell();
        serviceCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        serviceCell.setHeight(20);
        serviceCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        serviceCell.setPadding(3);
        serviceCell.setMargin(0);
        serviceCell.add(new Paragraph(invoiceData.getServiceDescription())
                .setFont(regularFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));

        Cell serviceAmountCell = new Cell();
        serviceAmountCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        serviceAmountCell.setHeight(20);
        serviceAmountCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        serviceAmountCell.setPadding(3);
        serviceAmountCell.setMargin(0);
        serviceAmountCell.add(new Paragraph(String.format("%.2f€", invoiceData.getSubtotal()))
                .setFont(regularFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMargin(0)
                .setPadding(0));

        conceptsTable.addCell(serviceCell);
        conceptsTable.addCell(serviceAmountCell);

        // Fila vacía
        Cell emptyDesc2 = new Cell();
        emptyDesc2.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        emptyDesc2.setHeight(16);
        emptyDesc2.setVerticalAlignment(VerticalAlignment.MIDDLE);
        emptyDesc2.setPadding(2);
        emptyDesc2.setMargin(0);
        emptyDesc2.add(new Paragraph(" ")
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));

        Cell emptyAmount2 = new Cell();
        emptyAmount2.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        emptyAmount2.setHeight(16);
        emptyAmount2.setVerticalAlignment(VerticalAlignment.MIDDLE);
        emptyAmount2.setPadding(2);
        emptyAmount2.setMargin(0);
        emptyAmount2.add(new Paragraph(" ")
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));

        conceptsTable.addCell(emptyDesc2);
        conceptsTable.addCell(emptyAmount2);

        // Subtotal
        Cell subtotalCell = new Cell();
        subtotalCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        subtotalCell.setHeight(18);
        subtotalCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        subtotalCell.setPadding(3);
        subtotalCell.setMargin(0);
        subtotalCell.add(new Paragraph("SUBTOTAL")
                .setFont(regularFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));

        Cell subtotalAmountCell = new Cell();
        subtotalAmountCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        subtotalAmountCell.setHeight(18);
        subtotalAmountCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        subtotalAmountCell.setPadding(3);
        subtotalAmountCell.setMargin(0);
        subtotalAmountCell.add(new Paragraph(String.format("%.2f€", invoiceData.getSubtotal()))
                .setFont(regularFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMargin(0)
                .setPadding(0));

        conceptsTable.addCell(subtotalCell);
        conceptsTable.addCell(subtotalAmountCell);

        // Retención IRPF
        Cell retentionCell = new Cell();
        retentionCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        retentionCell.setHeight(18);
        retentionCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        retentionCell.setPadding(3);
        retentionCell.setMargin(0);
        retentionCell.add(new Paragraph(String.format("RETENCION I.R.P.F. (%.0f%%)", invoiceData.getRetentionPercentage()))
                .setFont(regularFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));

        Cell retentionAmountCell = new Cell();
        retentionAmountCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        retentionAmountCell.setHeight(18);
        retentionAmountCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        retentionAmountCell.setPadding(3);
        retentionAmountCell.setMargin(0);
        retentionAmountCell.add(new Paragraph(String.format("%.2f€", invoiceData.getRetentionAmount()))
                .setFont(regularFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMargin(0)
                .setPadding(0));

        conceptsTable.addCell(retentionCell);
        conceptsTable.addCell(retentionAmountCell);

        // IVA
        Cell ivaCell = new Cell();
        ivaCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        ivaCell.setHeight(18);
        ivaCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        ivaCell.setPadding(3);
        ivaCell.setMargin(0);
        ivaCell.add(new Paragraph(String.format("IVA (%.0f%%)", invoiceData.getIvaPercentage()))
                .setFont(regularFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));

        Cell ivaAmountCell = new Cell();
        ivaAmountCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        ivaAmountCell.setHeight(18);
        ivaAmountCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        ivaAmountCell.setPadding(3);
        ivaAmountCell.setMargin(0);
        ivaAmountCell.add(new Paragraph(String.format("%.2f€", invoiceData.getIvaAmount()))
                .setFont(regularFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMargin(0)
                .setPadding(0));

        conceptsTable.addCell(ivaCell);
        conceptsTable.addCell(ivaAmountCell);

        // TOTAL con formato destacado
        Cell totalCell = new Cell();
        totalCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        totalCell.setHeight(20);
        totalCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        totalCell.setPadding(3);
        totalCell.setMargin(0);
        totalCell.add(new Paragraph("TOTAL")
                .setFont(boldFont)
                .setFontSize(8)
                .setMargin(0)
                .setPadding(0));

        Cell totalAmountCell = new Cell();
        totalAmountCell.setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f));
        totalAmountCell.setHeight(20);
        totalAmountCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        totalAmountCell.setPadding(3);
        totalAmountCell.setMargin(0);
        totalAmountCell.add(new Paragraph(String.format("%.2f€", invoiceData.getTotal()))
                .setFont(boldFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMargin(0)
                .setPadding(0));

        conceptsTable.addCell(totalCell);
        conceptsTable.addCell(totalAmountCell);

        document.add(conceptsTable);
    }
}