package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.EmailRequest;
import com.bill_api_generator.bill_api_generator.dto.InvoiceDto;
import com.bill_api_generator.bill_api_generator.exception.ClientNotFoundException;
import com.bill_api_generator.bill_api_generator.model.Client;
import com.bill_api_generator.bill_api_generator.model.Contact;
import com.bill_api_generator.bill_api_generator.repository.ClientRepository;
import com.bill_api_generator.bill_api_generator.repository.ContactRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Service class for managing email operations.
 * Handles sending invoices via email with attachments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final InvoiceService invoiceService;
    private final DocumentGeneratorService documentGeneratorService;
    private final ClientRepository clientRepository;
    private final ContactRepository contactRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "ES"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        MONEY_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    /**
     * Sends an invoice via email with document attachment.
     *
     * @param request Email request with recipient details and invoice ID
     * @throws MessagingException if there's an error sending the email
     */
    public void sendInvoiceEmail(EmailRequest request) throws MessagingException {
        log.info("Sending invoice via email. InvoiceID: {}, To: {}", request.getInvoiceId(), request.getTo());

        // Get the invoice
        InvoiceDto invoice = invoiceService.findById(request.getInvoiceId());

        // Generate the DOCX document
        ByteArrayOutputStream document = documentGeneratorService.generateInvoiceDocx(invoice);

        // Prepare the email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Configure recipients
        helper.setTo(request.getTo());
        
        if (request.getCc() != null && !request.getCc().isEmpty()) {
            helper.setCc(request.getCc().toArray(new String[0]));
        }

        // Configure subject
        String subject = request.getSubject() != null 
                ? request.getSubject() 
                : "Invoice " + invoice.getInvoiceNumber() + " - " + invoice.getClientName();
        helper.setSubject(subject);

        // Generate email body
        String emailBody = generateEmailBody(invoice, request.getMessage());
        helper.setText(emailBody, true); // true = HTML

        // Attach the document
        String filename = "Invoice-" + invoice.getInvoiceNumber() + ".docx";
        ByteArrayResource attachment = new ByteArrayResource(document.toByteArray());
        helper.addAttachment(filename, attachment);

        // Send
        mailSender.send(message);
        log.info("Email sent successfully to: {}", request.getTo());
    }

    private String generateEmailBody(InvoiceDto invoice, String customMessage) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 20px; background-color: #f9f9f9; }");
        html.append(".invoice-info { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }");
        html.append(".invoice-info h3 { margin-top: 0; color: #4CAF50; }");
        html.append(".detail { margin: 10px 0; }");
        html.append(".detail strong { display: inline-block; width: 150px; }");
        html.append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }");
        html.append(".total { font-size: 18px; font-weight: bold; color: #4CAF50; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>Invoice Attached</h1>");
        html.append("</div>");
        
        // Content
        html.append("<div class='content'>");
        
        // Custom message
        if (customMessage != null && !customMessage.isEmpty()) {
            html.append("<p>").append(customMessage).append("</p>");
        } else {
            html.append("<p>Dear ").append(invoice.getClientName()).append(",</p>");
            html.append("<p>Please find attached the invoice for the services provided.</p>");
        }
        
        // Invoice information
        html.append("<div class='invoice-info'>");
        html.append("<h3>Invoice Details</h3>");
        
        html.append("<div class='detail'>");
        html.append("<strong>Invoice Number:</strong> ").append(invoice.getInvoiceNumber());
        html.append("</div>");
        
        html.append("<div class='detail'>");
        html.append("<strong>Date:</strong> ").append(invoice.getDate().format(DATE_FORMATTER));
        html.append("</div>");
        
        html.append("<div class='detail'>");
        html.append("<strong>Period:</strong> ").append(invoice.getBillingMonth());
        html.append("</div>");
        
        html.append("<div class='detail'>");
        html.append("<strong>Hours:</strong> ").append(invoice.getHours()).append(" hours");
        html.append("</div>");
        
        html.append("<div class='detail'>");
        html.append("<strong>Subtotal:</strong> ").append(formatMoney(invoice.getSubtotal())).append(" €");
        html.append("</div>");
        
        html.append("<div class='detail'>");
        html.append("<strong>Tax Withholding (15%):</strong> ").append(formatMoney(invoice.getTaxWithholding())).append(" €");
        html.append("</div>");
        
        html.append("<div class='detail'>");
        html.append("<strong>VAT (21%):</strong> ").append(formatMoney(invoice.getVat())).append(" €");
        html.append("</div>");
        
        html.append("<div class='detail total'>");
        html.append("<strong>TOTAL:</strong> ").append(formatMoney(invoice.getTotal())).append(" €");
        html.append("</div>");
        
        html.append("</div>");
        
        html.append("<p>If you have any questions or concerns, please don't hesitate to contact us.</p>");
        html.append("<p>Best regards,<br><strong>").append(invoice.getIssuerName()).append("</strong></p>");
        
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>This is an automated email. Please do not reply to this message.</p>");
        html.append("<p>").append(invoice.getIssuerAddress()).append(", ").append(invoice.getIssuerPostalCode()).append("</p>");
        html.append("<p>Tax ID: ").append(invoice.getIssuerTaxId()).append("</p>");
        html.append("</div>");
        
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    private String formatMoney(java.math.BigDecimal amount) {
        if (amount == null) {
            return "0,00";
        }
        return MONEY_FORMAT.format(amount);
    }

    /**
     * Sends a PDF invoice using client's primary contact configuration.
     *
     * @param clientRef Client reference code
     * @param pdfFile PDF file to send
     * @throws MessagingException if there's an error sending the email
     * @throws IOException if there's an error reading the PDF
     */
    public void sendPdfByClientRef(String clientRef, MultipartFile pdfFile) 
            throws MessagingException, IOException {
        
        log.info("Sending PDF invoice by client ref: {}", clientRef);
        
        // 1. Find the client
        Client client = clientRepository.findByRefAndDeletedAtIsNull(clientRef)
                .orElseThrow(() -> new ClientNotFoundException(clientRef));
        
        // 2. Find the primary contact
        Contact primaryContact = contactRepository
                .findByClientIdAndIsPrimaryAndDeletedAtIsNull(client.getId(), true)
                .orElseThrow(() -> new RuntimeException(
                    "No primary contact configured for client: " + clientRef));
        
        // 3. Validate that contact has email configured
        if (primaryContact.getEmail() == null || primaryContact.getEmail().isBlank()) {
            throw new RuntimeException(
                "Primary contact for client " + clientRef + " has no email configured");
        }
        
        // 4. Prepare email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        // Set recipient
        helper.setTo(primaryContact.getEmail());
        
        // Set CC if configured
        List<String> ccEmails = primaryContact.getEmailCcList();
        if (!ccEmails.isEmpty()) {
            helper.setCc(ccEmails.toArray(new String[0]));
        }
        
        // Set subject (use template or default)
        String subject = primaryContact.getEmailSubject();
        if (subject == null || subject.isBlank()) {
            subject = "Invoice - " + client.getName();
        }
        // Replace placeholders
        subject = replacePlaceholders(subject, client, pdfFile.getOriginalFilename());
        helper.setSubject(subject);
        
        // Set message body (use template or default)
        String bodyText = primaryContact.getEmailMessage();
        if (bodyText == null || bodyText.isBlank()) {
            bodyText = "Dear " + client.getName() + ",\n\n" +
                       "Please find attached your invoice.\n\n" +
                       "Best regards";
        }
        // Replace placeholders and convert to HTML
        bodyText = replacePlaceholders(bodyText, client, pdfFile.getOriginalFilename());
        String htmlBody = convertToHtml(bodyText, client);
        helper.setText(htmlBody, true);
        
        // Attach PDF
        String filename = pdfFile.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            filename = "invoice-" + client.getRef() + ".pdf";
        }
        ByteArrayResource attachment = new ByteArrayResource(pdfFile.getBytes());
        helper.addAttachment(filename, attachment);
        
        // 5. Send email
        mailSender.send(message);
        log.info("PDF invoice sent successfully to: {} for client: {}", 
                 primaryContact.getEmail(), clientRef);
    }

    /**
     * Replace placeholders in text templates.
     * Supported placeholders: {clientName}, {invoiceNumber}, {date}
     */
    private String replacePlaceholders(String text, Client client, String filename) {
        if (text == null) {
            return "";
        }
        
        String result = text;
        result = result.replace("{clientName}", client.getName());
        result = result.replace("{date}", LocalDate.now().format(DATE_FORMATTER));
        
        // Try to extract invoice number from filename
        if (filename != null && filename.contains("-")) {
            String invoiceNumber = filename.substring(0, filename.lastIndexOf("."));
            result = result.replace("{invoiceNumber}", invoiceNumber);
        }
        
        return result;
    }

    /**
     * Convert plain text message to HTML with basic formatting.
     */
    private String convertToHtml(String text, Client client) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 20px; background-color: #f9f9f9; white-space: pre-wrap; }");
        html.append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");
        html.append("<div class='header'><h1>Invoice</h1></div>");
        html.append("<div class='content'>").append(text).append("</div>");
        html.append("<div class='footer'>");
        html.append("<p>This is an automated email. Please do not reply to this message.</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
}
