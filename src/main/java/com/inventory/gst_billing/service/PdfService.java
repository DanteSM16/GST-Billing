package com.inventory.gst_billing.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfService {
    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) { this.templateEngine = templateEngine; }

    public byte[] generatePdf(String templateName, Map<String, Object> variables) throws Exception {
        Context context = new Context();
        context.setVariables(variables);

        // 1. Parse HTML
        String html = templateEngine.process(templateName, context);

        // 2. Convert HTML String to PDF byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(outputStream);
        builder.run();

        return outputStream.toByteArray();
    }
}