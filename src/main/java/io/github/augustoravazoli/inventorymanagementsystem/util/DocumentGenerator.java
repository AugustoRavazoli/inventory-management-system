package io.github.augustoravazoli.inventorymanagementsystem.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Map;

@Component
public class DocumentGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DocumentGenerator.class);

    private final TemplateEngine templateEngine;

    public DocumentGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public Document generate(String filename, String template, Map<String, Object> variables, Locale locale) {
        var html = templateEngine.process(template, contextFromMap(variables, locale));
        var content = new ByteArrayOutputStream();
        var textRenderer = new ITextRenderer();
        textRenderer.setDocumentFromString(html);
        textRenderer.layout();
        textRenderer.createPDF(content);
        logger.info("Generating document");
        return new Document(filename, content, content.size());
    }

    private Context contextFromMap(Map<String, Object> variables, Locale locale) {
        var context = new Context(locale);
        context.setVariables(variables);
        return context;
    }

}
