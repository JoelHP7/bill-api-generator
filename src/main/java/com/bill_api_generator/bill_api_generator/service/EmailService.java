package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.EmailRequest;
import com.bill_api_generator.bill_api_generator.dto.FacturaDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final FacturaService facturaService;
    private final DocumentGeneratorService documentGeneratorService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "ES"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        MONEY_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    public void sendFacturaEmail(EmailRequest request) throws MessagingException, IOException {
        log.info("Enviando factura por email. FacturaID: {}, To: {}", request.getFacturaId(), request.getTo());

        // Obtener la factura
        FacturaDto factura = facturaService.findById(request.getFacturaId());

        // Generar el documento DOCX
        ByteArrayOutputStream document = documentGeneratorService.generateFacturaDocx(factura);

        // Preparar el email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Configurar destinatarios
        helper.setTo(request.getTo());
        
        if (request.getCc() != null && !request.getCc().isEmpty()) {
            helper.setCc(request.getCc().toArray(new String[0]));
        }

        // Configurar asunto
        String asunto = request.getAsunto() != null 
                ? request.getAsunto() 
                : "Factura " + factura.getNumeroFactura() + " - " + factura.getNombreCliente();
        helper.setSubject(asunto);

        // Generar cuerpo del email
        String cuerpoEmail = generarCuerpoEmail(factura, request.getMensaje());
        helper.setText(cuerpoEmail, true); // true = es HTML

        // Adjuntar el documento
        String filename = "Factura-" + factura.getNumeroFactura() + ".docx";
        ByteArrayResource attachment = new ByteArrayResource(document.toByteArray());
        helper.addAttachment(filename, attachment);

        // Enviar
        mailSender.send(message);
        log.info("Email enviado exitosamente a: {}", request.getTo());
    }

    private String generarCuerpoEmail(FacturaDto factura, String mensajePersonalizado) {
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
        html.append(".factura-info { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }");
        html.append(".factura-info h3 { margin-top: 0; color: #4CAF50; }");
        html.append(".detalle { margin: 10px 0; }");
        html.append(".detalle strong { display: inline-block; width: 150px; }");
        html.append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }");
        html.append(".total { font-size: 18px; font-weight: bold; color: #4CAF50; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>Factura Adjunta</h1>");
        html.append("</div>");
        
        // Content
        html.append("<div class='content'>");
        
        // Mensaje personalizado
        if (mensajePersonalizado != null && !mensajePersonalizado.isEmpty()) {
            html.append("<p>").append(mensajePersonalizado).append("</p>");
        } else {
            html.append("<p>Estimado/a ").append(factura.getNombreCliente()).append(",</p>");
            html.append("<p>Adjunto encontrará la factura correspondiente a los servicios prestados.</p>");
        }
        
        // Información de la factura
        html.append("<div class='factura-info'>");
        html.append("<h3>Detalles de la Factura</h3>");
        
        html.append("<div class='detalle'>");
        html.append("<strong>Número de Factura:</strong> ").append(factura.getNumeroFactura());
        html.append("</div>");
        
        html.append("<div class='detalle'>");
        html.append("<strong>Fecha:</strong> ").append(factura.getFecha().format(DATE_FORMATTER));
        html.append("</div>");
        
        html.append("<div class='detalle'>");
        html.append("<strong>Período:</strong> ").append(factura.getMesFact());
        html.append("</div>");
        
        html.append("<div class='detalle'>");
        html.append("<strong>Horas:</strong> ").append(factura.getHoras()).append(" horas");
        html.append("</div>");
        
        html.append("<div class='detalle'>");
        html.append("<strong>Base Imponible:</strong> ").append(formatMoney(factura.getImponible())).append(" €");
        html.append("</div>");
        
        html.append("<div class='detalle'>");
        html.append("<strong>IRPF (15%):</strong> ").append(formatMoney(factura.getIrpf())).append(" €");
        html.append("</div>");
        
        html.append("<div class='detalle'>");
        html.append("<strong>IVA (21%):</strong> ").append(formatMoney(factura.getIva())).append(" €");
        html.append("</div>");
        
        html.append("<div class='detalle total'>");
        html.append("<strong>TOTAL:</strong> ").append(formatMoney(factura.getTotal())).append(" €");
        html.append("</div>");
        
        html.append("</div>");
        
        html.append("<p>Si tiene alguna duda o consulta, no dude en contactarnos.</p>");
        html.append("<p>Saludos cordiales,<br><strong>").append(factura.getEmisorNombre()).append("</strong></p>");
        
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Este es un correo automático. Por favor, no responda a este mensaje.</p>");
        html.append("<p>").append(factura.getEmisorDireccion()).append(", ").append(factura.getEmisorCp()).append("</p>");
        html.append("<p>NIF: ").append(factura.getEmisorNif()).append("</p>");
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
}
