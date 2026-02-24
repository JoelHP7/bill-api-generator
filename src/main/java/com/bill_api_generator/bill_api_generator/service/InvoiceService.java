package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.InvoiceDto;
import com.bill_api_generator.bill_api_generator.model.Client;
import com.bill_api_generator.bill_api_generator.model.Invoice;
import com.bill_api_generator.bill_api_generator.repository.ClientRepository;
import com.bill_api_generator.bill_api_generator.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bill_api_generator.bill_api_generator.exception.ClientNotFoundException;
import com.bill_api_generator.bill_api_generator.exception.InvoiceNotFoundException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Service class for managing invoice operations.
 * Handles invoice generation, calculations, and document creation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final DocumentGeneratorService documentGeneratorService;

    // Tax rate constants
    private static final BigDecimal TAX_WITHHOLDING_RATE = new BigDecimal("0.15"); // 15% IRPF
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21"); // 21% IVA

    // Issuer data (fixed)
    private static final String ISSUER_NAME = "JOEL HERNANDEZ PLA";
    private static final String ISSUER_ADDRESS = "Carrer Mossen Ernest Mateu, 7";
    private static final String ISSUER_POSTAL_CODE = "08181";
    private static final String ISSUER_TAX_ID = "47818505X";
    private static final String ISSUER_IBAN = "ES62 1465 0180 71 1734028810";

    /**
     * Generates an invoice using the client's reference (main method).
     *
     * @param clientRef Client reference code
     * @param hours Number of hours to bill
     * @param date Invoice date
     * @return Map with generation status and invoice details
     */
    @Transactional
    public Map<String, Object> generateInvoiceByRef(String clientRef, Integer hours, LocalDate date) {
        Client client = clientRepository.findByRefAndDeletedAtIsNull(clientRef)
                .orElseThrow(() -> new ClientNotFoundException(clientRef));
        return generateInvoiceInternal(client, hours, date);
    }

    /**
     * Generates an invoice using the client ID (for backward compatibility).
     *
     * @param clientId Client ID
     * @param hours Number of hours to bill
     * @param date Invoice date
     * @return Map with generation status and invoice details
     */
    @Transactional
    public Map<String, Object> generateInvoice(Long clientId, Integer hours, LocalDate date) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        return generateInvoiceInternal(client, hours, date);
    }

    /**
     * Internal method that generates the invoice with duplicate verification.
     */
    private Map<String, Object> generateInvoiceInternal(Client client, Integer hours, LocalDate date) {
        Map<String, Object> response = new HashMap<>();

        if (date == null) {
            date = LocalDate.now();
        }

        // CHECK FOR DUPLICATES
        Optional<Invoice> existingInvoice = invoiceRepository.findByClientIdAndDateAndHours(
                client.getId(), date, hours);

        if (existingInvoice.isPresent()) {
            log.warn("Duplicate invoice detected for client: {}, date: {}, hours: {}",
                    client.getRef(), date, hours);

            response.put("status", "DUPLICATE");
            response.put("message", "An invoice with these parameters already exists");
            response.put("invoice", convertToDto(existingInvoice.get()));
            response.put("suggestions", Arrays.asList(
                    "Verify if the date is correct",
                    "Verify if the hours are correct",
                    "If you need to modify it, use PUT /api/invoices/{id}",
                    "If it's correct, you can download the existing document"
            ));
            return response;
        }

        // GENERATE NEW INVOICE
        log.info("Generating new invoice for client: {}, date: {}, hours: {}",
                client.getRef(), date, hours);

        String invoiceNumber = generateInvoiceNumber(date);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .hours(hours)
                .client(client)
                .date(date)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Update invoice number with real sequential number
        savedInvoice.setInvoiceNumber(generateInvoiceNumberWithSequential(savedInvoice.getSequentialNumber()));
        savedInvoice = invoiceRepository.save(savedInvoice);

        log.info("Invoice generated: {}", savedInvoice.getInvoiceNumber());

        response.put("status", "CREATED");
        response.put("message", "Invoice generated successfully");
        response.put("invoice", convertToDto(savedInvoice));
        return response;
    }

    /**
     * Generates invoice with document by client reference.
     */
    @Transactional
    public ByteArrayOutputStream generateInvoiceWithDocumentByRef(String clientRef, Integer hours, LocalDate date) {
        Map<String, Object> response = generateInvoiceByRef(clientRef, hours, date);
        InvoiceDto invoiceDto = (InvoiceDto) response.get("invoice");
        return documentGeneratorService.generateInvoiceDocx(invoiceDto);
    }

    /**
     * Generates invoice with document by client ID.
     */
    @Transactional
    public ByteArrayOutputStream generateInvoiceWithDocument(Long clientId, Integer hours, LocalDate date) {
        Map<String, Object> response = generateInvoice(clientId, hours, date);
        InvoiceDto invoiceDto = (InvoiceDto) response.get("invoice");
        return documentGeneratorService.generateInvoiceDocx(invoiceDto);
    }

    private String generateInvoiceNumber(LocalDate date) {
        return String.format("%d-TEMP", date.getYear());
    }

    private String generateInvoiceNumberWithSequential(Long sequential) {
        return sequential.toString();
    }

    private String getBillingMonth(LocalDate date) {
        String month = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        month = month.substring(0, 1).toUpperCase() + month.substring(1);
        return month + " " + date.getYear();
    }

    @Transactional(readOnly = true)
    public InvoiceDto findById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        return convertToDto(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> findByClientRef(String clientRef) {
        Client client = clientRepository.findByRefAndDeletedAtIsNull(clientRef)
                .orElseThrow(() -> new ClientNotFoundException(clientRef));
        return invoiceRepository.findByClientId(client.getId()).stream()
                .map(this::convertToDto)
                .toList();
    }

    private InvoiceDto convertToDto(Invoice invoice) {
        Client client = invoice.getClient();
        BigDecimal subtotal = client.getRate().multiply(new BigDecimal(invoice.getHours()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxWithholding = subtotal.multiply(TAX_WITHHOLDING_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal vat = subtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(taxWithholding).add(vat).setScale(2, RoundingMode.HALF_UP);

        return InvoiceDto.builder()
                .id(invoice.getSequentialNumber())
                .invoiceNumber(invoice.getInvoiceNumber())
                .hours(invoice.getHours())
                .date(invoice.getDate())
                .billingMonth(getBillingMonth(invoice.getDate()))
                .clientName(client.getName())
                .clientTaxId(client.getTaxId())
                .clientAddress(client.getAddress())
                .issuerName(ISSUER_NAME)
                .issuerAddress(ISSUER_ADDRESS)
                .issuerPostalCode(ISSUER_POSTAL_CODE)
                .issuerTaxId(ISSUER_TAX_ID)
                .issuerIban(ISSUER_IBAN)
                .subtotal(subtotal)
                .taxWithholding(taxWithholding)
                .vat(vat)
                .total(total)
                .build();
    }
}
