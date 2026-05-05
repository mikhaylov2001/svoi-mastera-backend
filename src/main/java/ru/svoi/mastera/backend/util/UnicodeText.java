package ru.svoi.mastera.backend.util;

import java.text.Normalizer;

/**
 * Единообразное представление текста: NFKC убирает «совместимые» дубликаты символов
 * и выравнивает старые формы эмодзи/совместимые символы.
 */
public final class UnicodeText {

    private UnicodeText() {
    }

    public static String nfkc(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Normalizer.normalize(s, Normalizer.Form.NFKC);
    }
}
