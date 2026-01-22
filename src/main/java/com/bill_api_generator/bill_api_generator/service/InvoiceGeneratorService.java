package com.bill_api_generator.bill_api_generator.service;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceGeneratorService {

    public byte[] generateInvoiceDocx(InvoiceData invoiceData) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            // Crear el encabezado con información del emisor y número de factura
            createHeader(document, invoiceData);

            // Espacio entre secciones
            document.createParagraph();

            // Crear sección del destinatario
            createRecipientSection(document, invoiceData);

            // Espacio entre secciones
            document.createParagraph();

            // Crear tabla de conceptos
            createInvoiceTable(document, invoiceData);

            // Convertir a bytes para devolver
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public void saveInvoiceToFile(InvoiceData invoiceData, String filePath) throws IOException {
        byte[] docxBytes = generateInvoiceDocx(invoiceData);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(docxBytes);
        }
    }

    private void createHeader(XWPFDocument document, InvoiceData invoiceData) {
        // Crear tabla para el encabezado
        XWPFTable headerTable = document.createTable(1, 2);
        headerTable.setWidth("100%");

        // Configurar ancho de columnas
        headerTable.getRow(0).getCell(0).getCTTc().addNewTcPr().addNewTcW().setW(BigDecimal.valueOf(5000));
        headerTable.getRow(0).getCell(1).getCTTc().addNewTcPr().addNewTcW().setW(BigDecimal.valueOf(3000));

        // Columna izquierda - Datos del emisor
        XWPFTableCell leftCell = headerTable.getRow(0).getCell(0);
        leftCell.removeParagraph(0);

        // Nombre del emisor
        XWPFParagraph nameParagraph = leftCell.addParagraph();
        XWPFRun nameRun = nameParagraph.createRun();
        nameRun.setText(invoiceData.getIssuerName());
        nameRun.setBold(true);
        nameRun.setFontSize(12);

        // Dirección
        XWPFParagraph addressParagraph = leftCell.addParagraph();
        XWPFRun addressRun = addressParagraph.createRun();
        addressRun.setText("Dirección " + invoiceData.getIssuerAddress());
        addressRun.setBold(true);

        // Código postal
        XWPFParagraph postalParagraph = leftCell.addParagraph();
        XWPFRun postalRun = postalParagraph.createRun();
        postalRun.setText("CP " + invoiceData.getIssuerPostalCode());
        postalRun.setBold(true);

        // NIF
        XWPFParagraph nifParagraph = leftCell.addParagraph();
        XWPFRun nifRun = nifParagraph.createRun();
        nifRun.setText("NIF " + invoiceData.getIssuerNIF());
        nifRun.setBold(true);

        // IBAN
        XWPFParagraph ibanParagraph = leftCell.addParagraph();
        XWPFRun ibanRun = ibanParagraph.createRun();
        ibanRun.setText("IBAN " + invoiceData.getIssuerIBAN());
        ibanRun.setBold(true);

        // Columna derecha - Número de factura y fecha
        XWPFTableCell rightCell = headerTable.getRow(0).getCell(1);
        rightCell.removeParagraph(0);

        // Número de factura
        XWPFParagraph invoiceNumberParagraph = rightCell.addParagraph();
        invoiceNumberParagraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun invoiceNumberRun = invoiceNumberParagraph.createRun();
        invoiceNumberRun.setText("Nº DE FACTURA: " + invoiceData.getInvoiceNumber());
        invoiceNumberRun.setBold(true);

        // Espacio
        rightCell.addParagraph();

        // Fecha
        XWPFParagraph dateParagraph = rightCell.addParagraph();
        dateParagraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun dateRun = dateParagraph.createRun();
        dateRun.setText("FECHA: " + invoiceData.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        dateRun.setBold(true);

        // Quitar bordes de la tabla
        headerTable.setInsideHBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "");
        headerTable.setInsideVBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "");
        headerTable.setTopBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "");
        headerTable.setBottomBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "");
        headerTable.setLeftBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "");
        headerTable.setRightBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "");
    }

    private void createRecipientSection(XWPFDocument document, InvoiceData invoiceData) {
        // Crear tabla para la sección del destinatario
        XWPFTable recipientTable = document.createTable(6, 1);
        recipientTable.setWidth("100%");

        // Para
        XWPFTableCell paraCell = recipientTable.getRow(0).getCell(0);
        paraCell.removeParagraph(0);
        XWPFParagraph paraParagraph = paraCell.addParagraph();
        XWPFRun paraRun = paraParagraph.createRun();
        paraRun.setText("Para");
        paraRun.setBold(true);

        // Nombre del cliente
        XWPFTableCell clientNameCell = recipientTable.getRow(1).getCell(0);
        clientNameCell.removeParagraph(0);
        XWPFParagraph clientNameParagraph = clientNameCell.addParagraph();
        XWPFRun clientNameRun = clientNameParagraph.createRun();
        clientNameRun.setText(invoiceData.getClientName());

        // Nombre completo del cliente
        XWPFTableCell fullNameCell = recipientTable.getRow(2).getCell(0);
        fullNameCell.removeParagraph(0);
        XWPFParagraph fullNameParagraph = fullNameCell.addParagraph();
        XWPFRun fullNameRun = fullNameParagraph.createRun();
        fullNameRun.setText(invoiceData.getClientFullName());
        fullNameRun.setBold(true);

        // CIF
        XWPFTableCell cifCell = recipientTable.getRow(3).getCell(0);
        cifCell.removeParagraph(0);
        XWPFParagraph cifParagraph = cifCell.addParagraph();
        XWPFRun cifRun = cifParagraph.createRun();
        cifRun.setText(invoiceData.getClientCIF());

        // Dirección del cliente
        XWPFTableCell clientAddressCell = recipientTable.getRow(4).getCell(0);
        clientAddressCell.removeParagraph(0);
        XWPFParagraph clientAddressParagraph = clientAddressCell.addParagraph();
        XWPFRun clientAddressRun = clientAddressParagraph.createRun();
        clientAddressRun.setText(invoiceData.getClientAddress());

        // Celda vacía
        recipientTable.getRow(5).getCell(0).removeParagraph(0);
        recipientTable.getRow(5).getCell(0).addParagraph();

        // Configurar bordes
        for (XWPFTableRow row : recipientTable.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                cell.getCTTc().addNewTcPr().addNewTcBorders().addNewLeft().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
                cell.getCTTc().getTcPr().getTcBorders().addNewTop().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
                cell.getCTTc().getTcPr().getTcBorders().addNewRight().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
                cell.getCTTc().getTcPr().getTcBorders().addNewBottom().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
            }
        }
    }

    private void createInvoiceTable(XWPFDocument document, InvoiceData invoiceData) {
        // Crear tabla de conceptos
        XWPFTable conceptsTable = document.createTable(7, 2);
        conceptsTable.setWidth("100%");

        // Configurar ancho de columnas
        conceptsTable.getRow(0).getCell(0).getCTTc().addNewTcPr().addNewTcW().setW(BigDecimal.valueOf(6000));
        conceptsTable.getRow(0).getCell(1).getCTTc().addNewTcPr().addNewTcW().setW(BigDecimal.valueOf(2000));

        // Encabezados
        XWPFTableCell descriptionHeader = conceptsTable.getRow(0).getCell(0);
        descriptionHeader.removeParagraph(0);
        XWPFParagraph descParagraph = descriptionHeader.addParagraph();
        XWPFRun descRun = descParagraph.createRun();
        descRun.setText("DESCRIPCIÓN");
        descRun.setBold(true);

        XWPFTableCell amountHeader = conceptsTable.getRow(0).getCell(1);
        amountHeader.removeParagraph(0);
        XWPFParagraph amountHeaderParagraph = amountHeader.addParagraph();
        amountHeaderParagraph.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun amountRun = amountHeaderParagraph.createRun();
        amountRun.setText("IMPORTE");
        amountRun.setBold(true);

        // Fila vacía
        conceptsTable.getRow(1).getCell(0).removeParagraph(0);
        conceptsTable.getRow(1).getCell(0).addParagraph();
        conceptsTable.getRow(1).getCell(1).removeParagraph(0);
        conceptsTable.getRow(1).getCell(1).addParagraph();

        // Descripción del servicio
        XWPFTableCell serviceCell = conceptsTable.getRow(2).getCell(0);
        serviceCell.removeParagraph(0);
        XWPFParagraph serviceParagraph = serviceCell.addParagraph();
        XWPFRun serviceRun = serviceParagraph.createRun();
        serviceRun.setText(invoiceData.getServiceDescription());

        XWPFTableCell serviceAmountCell = conceptsTable.getRow(2).getCell(1);
        serviceAmountCell.removeParagraph(0);
        XWPFParagraph serviceAmountParagraph = serviceAmountCell.addParagraph();
        serviceAmountParagraph.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun serviceAmountRun = serviceAmountParagraph.createRun();
        serviceAmountRun.setText(String.format("%.2f€", invoiceData.getSubtotal()));

        // Fila vacía
        conceptsTable.getRow(3).getCell(0).removeParagraph(0);
        conceptsTable.getRow(3).getCell(0).addParagraph();
        conceptsTable.getRow(3).getCell(1).removeParagraph(0);
        conceptsTable.getRow(3).getCell(1).addParagraph();

        // Subtotal
        XWPFTableCell subtotalCell = conceptsTable.getRow(4).getCell(0);
        subtotalCell.removeParagraph(0);
        XWPFParagraph subtotalParagraph = subtotalCell.addParagraph();
        XWPFRun subtotalRun = subtotalParagraph.createRun();
        subtotalRun.setText("SUBTOTAL");

        XWPFTableCell subtotalAmountCell = conceptsTable.getRow(4).getCell(1);
        subtotalAmountCell.removeParagraph(0);
        XWPFParagraph subtotalAmountParagraph = subtotalAmountCell.addParagraph();
        subtotalAmountParagraph.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun subtotalAmountRun = subtotalAmountParagraph.createRun();
        subtotalAmountRun.setText(String.format("%.2f€", invoiceData.getSubtotal()));

        // Retención IRPF
        XWPFTableCell retentionCell = conceptsTable.getRow(5).getCell(0);
        retentionCell.removeParagraph(0);
        XWPFParagraph retentionParagraph = retentionCell.addParagraph();
        XWPFRun retentionRun = retentionParagraph.createRun();
        retentionRun.setText(String.format("RETENCION I.R.P.F. (%.0f%%)", invoiceData.getRetentionPercentage()));

        XWPFTableCell retentionAmountCell = conceptsTable.getRow(5).getCell(1);
        retentionAmountCell.removeParagraph(0);
        XWPFParagraph retentionAmountParagraph = retentionAmountCell.addParagraph();
        retentionAmountParagraph.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun retentionAmountRun = retentionAmountParagraph.createRun();
        retentionAmountRun.setText(String.format("%.2f€", invoiceData.getRetentionAmount()));

        // IVA
        XWPFTableCell ivaCell = conceptsTable.getRow(6).getCell(0);
        ivaCell.removeParagraph(0);
        XWPFParagraph ivaParagraph = ivaCell.addParagraph();
        XWPFRun ivaRun = ivaParagraph.createRun();
        ivaRun.setText(String.format("IVA (%.0f%%)", invoiceData.getIvaPercentage()));

        XWPFTableCell ivaAmountCell = conceptsTable.getRow(6).getCell(1);
        ivaAmountCell.removeParagraph(0);
        XWPFParagraph ivaAmountParagraph = ivaAmountCell.addParagraph();
        ivaAmountParagraph.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun ivaAmountRun = ivaAmountParagraph.createRun();
        ivaAmountRun.setText(String.format("%.2f€", invoiceData.getIvaAmount()));

        // Configurar bordes para toda la tabla
        for (XWPFTableRow row : conceptsTable.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                // Asegurar que las propiedades de la celda existan
                if (cell.getCTTc().getTcPr() == null) {
                    cell.getCTTc().addNewTcPr();
                }

                // Asegurar que las propiedades de bordes existan
                if (cell.getCTTc().getTcPr().getTcBorders() == null) {
                    cell.getCTTc().getTcPr().addNewTcBorders();
                }

                // Configurar bordes
                cell.getCTTc().getTcPr().getTcBorders().addNewLeft().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
                cell.getCTTc().getTcPr().getTcBorders().addNewTop().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
                cell.getCTTc().getTcPr().getTcBorders().addNewRight().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
                cell.getCTTc().getTcPr().getTcBorders().addNewBottom().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
            }
        }

        // Crear tabla separada para el total
        XWPFTable totalTable = document.createTable(1, 2);
        totalTable.setWidth("100%");

        // Configurar ancho de columnas
        totalTable.getRow(0).getCell(0).getCTTc().addNewTcPr().addNewTcW().setW(BigDecimal.valueOf(6000));
        totalTable.getRow(0).getCell(1).getCTTc().addNewTcPr().addNewTcW().setW(BigDecimal.valueOf(2000));

        // Total
        XWPFTableCell totalCell = totalTable.getRow(0).getCell(0);
        totalCell.removeParagraph(0);
        XWPFParagraph totalParagraph = totalCell.addParagraph();
        XWPFRun totalRun = totalParagraph.createRun();
        totalRun.setText("TOTAL");
        totalRun.setBold(true);

        XWPFTableCell totalAmountCell = totalTable.getRow(0).getCell(1);
        totalAmountCell.removeParagraph(0);
        XWPFParagraph totalAmountParagraph = totalAmountCell.addParagraph();
        totalAmountParagraph.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun totalAmountRun = totalAmountParagraph.createRun();
        totalAmountRun.setText(String.format("%.2f€", invoiceData.getTotal()));
        totalAmountRun.setBold(true);

        // Configurar bordes para la tabla total
        for (XWPFTableRow row : totalTable.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                // Asegurar que las propiedades de la celda existan
                if (cell.getCTTc().getTcPr() == null) {
                    cell.getCTTc().addNewTcPr();
                }

                // Asegurar que las propiedades de bordes existan
                if (cell.getCTTc().getTcPr().getTcBorders() == null) {
                    cell.getCTTc().getTcPr().addNewTcBorders();
                }

                // Configurar bordes
                cell.getCTTc().getTcPr().getTcBorders().addNewLeft().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
                cell.getCTTc().getTcPr().getTcBorders().addNewTop().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
                cell.getCTTc().getTcPr().getTcBorders().addNewRight().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
                cell.getCTTc().getTcPr().getTcBorders().addNewBottom().setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
            }
        }
    }

    // Clase de datos para la factura
    public static class InvoiceData {
        private String issuerName;
        private String issuerAddress;
        private String issuerPostalCode;
        private String issuerNIF;
        private String issuerIBAN;
        private String invoiceNumber;
        private LocalDate invoiceDate;
        private String clientName;
        private String clientFullName;
        private String clientCIF;
        private String clientAddress;
        private String serviceDescription;
        private Double subtotal;
        private Double retentionPercentage;
        private Double retentionAmount;
        private Double ivaPercentage;
        private Double ivaAmount;
        private Double total;

        // Constructor con valores por defecto basados en tu factura
        public InvoiceData() {
            this.issuerName = "JOEL HERNANDEZ PLA";
            this.issuerAddress = "Carrer Mossen Ernest Mateu, 7";
            this.issuerPostalCode = "08181";
            this.issuerNIF = "47818505X";
            this.issuerIBAN = "ES62 1465 0180 71 1734028810";
            this.invoiceNumber = "52";
            this.invoiceDate = LocalDate.of(2025, 9, 25);
            this.clientName = "AXPE CONSULTING, S.L.";
            this.clientFullName = "AKKODIS TECHNOLOGIES SPAIN S.L.U";
            this.clientCIF = "B84184548";
            this.clientAddress = "Calle Arturo Soria 122, Madrid";
            this.serviceDescription = "Trabajos profesionales Septiembre 2025 -- 152 horas";
            this.subtotal = 6080.00;
            this.retentionPercentage = 7.0;
            this.retentionAmount = 425.60;
            this.ivaPercentage = 21.0;
            this.ivaAmount = 1276.80;
            this.total = 6931.20;
        }

        // Getters y setters
        public String getIssuerName() { return issuerName; }
        public void setIssuerName(String issuerName) { this.issuerName = issuerName; }

        public String getIssuerAddress() { return issuerAddress; }
        public void setIssuerAddress(String issuerAddress) { this.issuerAddress = issuerAddress; }

        public String getIssuerPostalCode() { return issuerPostalCode; }
        public void setIssuerPostalCode(String issuerPostalCode) { this.issuerPostalCode = issuerPostalCode; }

        public String getIssuerNIF() { return issuerNIF; }
        public void setIssuerNIF(String issuerNIF) { this.issuerNIF = issuerNIF; }

        public String getIssuerIBAN() { return issuerIBAN; }
        public void setIssuerIBAN(String issuerIBAN) { this.issuerIBAN = issuerIBAN; }

        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

        public LocalDate getInvoiceDate() { return invoiceDate; }
        public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }

        public String getClientFullName() { return clientFullName; }
        public void setClientFullName(String clientFullName) { this.clientFullName = clientFullName; }

        public String getClientCIF() { return clientCIF; }
        public void setClientCIF(String clientCIF) { this.clientCIF = clientCIF; }

        public String getClientAddress() { return clientAddress; }
        public void setClientAddress(String clientAddress) { this.clientAddress = clientAddress; }

        public String getServiceDescription() { return serviceDescription; }
        public void setServiceDescription(String serviceDescription) { this.serviceDescription = serviceDescription; }

        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

        public Double getRetentionPercentage() { return retentionPercentage; }
        public void setRetentionPercentage(Double retentionPercentage) { this.retentionPercentage = retentionPercentage; }

        public Double getRetentionAmount() { return retentionAmount; }
        public void setRetentionAmount(Double retentionAmount) { this.retentionAmount = retentionAmount; }

        public Double getIvaPercentage() { return ivaPercentage; }
        public void setIvaPercentage(Double ivaPercentage) { this.ivaPercentage = ivaPercentage; }

        public Double getIvaAmount() { return ivaAmount; }
        public void setIvaAmount(Double ivaAmount) { this.ivaAmount = ivaAmount; }

        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
    }
}