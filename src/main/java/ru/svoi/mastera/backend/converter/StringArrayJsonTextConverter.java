package ru.svoi.mastera.backend.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

/**
 * Хранит массив URL/base64 в одной колонке TEXT как JSON-массив.
 * Надёжнее, чем PostgreSQL text[] через JDBC (на проде давало 500 при сохранении).
 */
@Converter
public class StringArrayJsonTextConverter implements AttributeConverter<String[], String> {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return "[]";
        }
        String[] cleaned = Arrays.stream(attribute)
                .filter(s -> s != null && !s.isBlank())
                .toArray(String[]::new);
        if (cleaned.length == 0) {
            return "[]";
        }
        try {
            return JSON.writeValueAsString(cleaned);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize listing photos", e);
        }
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank() || "[]".equals(dbData.trim())) {
            return new String[0];
        }
        try {
            String[] arr = JSON.readValue(dbData.trim(), String[].class);
            if (arr == null) {
                return new String[0];
            }
            return Arrays.stream(arr).filter(s -> s != null && !s.isBlank()).toArray(String[]::new);
        } catch (JsonProcessingException e) {
            return new String[0];
        }
    }
}
