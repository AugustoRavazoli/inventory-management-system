package io.github.augustoravazoli.inventorymanagementsystem.util;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

@Component
public class CsvConverter {

    private final Validator validator;

    public CsvConverter(Validator validator) {
        this.validator = validator;
    }

    public <T> List<T> convert(MultipartFile file, Class<T> clazz) {
        try (var reader = getReader(file)) {
            var beans = getBean(reader, clazz).parse();
            beans.forEach(this::validate);
            return beans;
        } catch (IOException | RuntimeException e) {
            throw new CsvConversionException();
        }
    }

    private CSVReader getReader(MultipartFile file) throws IOException {
        if (!Objects.equals(file.getContentType(), "text/csv")) {
            throw new CsvConversionException();
        }
        return new CSVReaderBuilder(new InputStreamReader(file.getInputStream()))
                .withCSVParser(getParser())
                .withSkipLines(1)
                .build();
    }

    private CSVParser getParser() {
        return new CSVParserBuilder()
                .withSeparator(';')
                .withIgnoreLeadingWhiteSpace(true)
                .build();
    }

    private <T> CsvToBean<T> getBean(CSVReader reader, Class<T> clazz) {
        return new CsvToBeanBuilder<T>(reader)
                .withType(clazz)
                .withIgnoreEmptyLine(true)
                .build();
    }

    private void validate(Object object) {
        var violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

}
