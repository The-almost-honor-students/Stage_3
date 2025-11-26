package com.tahs.infrastructure.serialization;

import java.text.Normalizer;
import java.util.Locale;

public class TextNormalizer {

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        // Convertir a minúsculas
        String normalized = text.toLowerCase(Locale.ROOT);
        // Eliminar acentos
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        // Eliminar caracteres especiales, mantener solo alfanuméricos y espacios
        normalized = normalized.replaceAll("[^a-z0-9\\s]", "");
        // Eliminar espacios múltiples
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }
}