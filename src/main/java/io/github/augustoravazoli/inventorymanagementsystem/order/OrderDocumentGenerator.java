package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.util.Document;
import io.github.augustoravazoli.inventorymanagementsystem.util.DocumentGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderDocumentGenerator {

    private static final Logger logger = LoggerFactory.getLogger(OrderDocumentGenerator.class);

    private final DocumentGenerator documentGenerator;
    private final MessageSource messageSource;

    public OrderDocumentGenerator(DocumentGenerator documentGenerator, MessageSource messageSource) {
        this.documentGenerator = documentGenerator;
        this.messageSource = messageSource;
    }

    public Document generateOrderDocument(Order order) {
        var prefix = messageSource.getMessage("order-document.file-prefix", null, LocaleContextHolder.getLocale());
        var filename = String.format("%s_N%d_%s.pdf", prefix, order.getNumber(), order.getDate());
        var template = "order/order-document";
        var variables = Map.<String, Object>of("order", order);
        var locale = LocaleContextHolder.getLocale();
        logger.info("Generating order document");
        return documentGenerator.generate(filename, template, variables, locale);
    }

}
