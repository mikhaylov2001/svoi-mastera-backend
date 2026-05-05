package ru.svoi.mastera.backend.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                .map(String::trim)
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
        String trim = dbData.trim();

        String[] fromJson = tryParseJsonArray(trim);
        if (fromJson != null) {
            return fromJson;
        }

        String[] fromPg = tryParsePostgresArrayLiteral(trim);
        if (fromPg != null) {
            return fromPg;
        }

        if (isSinglePhotoToken(trim)) {
            return new String[]{trim};
        }

        return new String[0];
    }

    private static String[] tryParseJsonArray(String trim) {
        if (!trim.startsWith("[")) {
            return null;
        }
        try {
            String[] arr = JSON.readValue(trim, String[].class);
            if (arr == null) {
                return new String[0];
            }
            return Arrays.stream(arr).filter(s -> s != null && !s.isBlank()).map(String::trim).toArray(String[]::new);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * Литерал PostgreSQL text[] в текстовом виде: {a,b} или {"a,b",c}
     */
    private static String[] tryParsePostgresArrayLiteral(String trim) {
        if (!trim.startsWith("{") || !trim.endsWith("}")) {
            return null;
        }
        String inner = trim.substring(1, trim.length() - 1).trim();
        if (inner.isEmpty()) {
            return new String[0];
        }
        if (inner.indexOf('"') >= 0) {
            return splitPgArrayQuoted(inner);
        }
        return Arrays.stream(inner.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    private static String[] splitPgArrayQuoted(String inner) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"') {
                if (inQuote && i + 1 < inner.length() && inner.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuote = !inQuote;
                }
                continue;
            }
            if (c == ',' && !inQuote) {
                String p = cur.toString().trim();
                if (!p.isBlank()) {
                    parts.add(p);
                }
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        String tail = cur.toString().trim();
        if (!tail.isBlank()) {
            parts.add(tail);
        }
        return parts.toArray(new String[0]);
    }

    private static boolean isSinglePhotoToken(String trim) {
        return trim.startsWith("http://") || trim.startsWith("https://") || trim.startsWith("data:image/");
    }
}
