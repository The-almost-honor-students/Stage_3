package com.tahs.infrastructure.serialization.books;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextTokenizer {
    private static final Pattern WORD = Pattern.compile("\\p{L}+(?:[’']\\p{L}+)*");

    private static final Set<String> STOP_WORDS = new HashSet<>();

    static {
        loadStopWords();
    }

    private static void loadStopWords() {
        try (java.io.InputStream inputStream = TextTokenizer.class.getClassLoader()
                .getResourceAsStream("stopwords.csv")) {
            if (inputStream == null) {
                throw new RuntimeException("stopwords.csv not found in resources");
            }
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(java.util.stream.Collectors.joining("\n"));
                for (String word : content.split(",")) {
                    if (!word.isBlank()) {
                        STOP_WORDS.add(word.trim());
                    }
                }
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load stop words", e);
        }
    }

    private TextTokenizer() {
    }

    public static Set<String> extractTerms(String text) {
        if (text == null || text.isBlank())
            return Collections.emptySet();

        String cleaned = normalize(text);
        Matcher m = WORD.matcher(cleaned);

        Set<String> terms = new HashSet<>();
        while (m.find()) {
            String w = m.group();
            if (!w.isBlank() && !isStopWord(w)) {
                terms.add(w);
            }
        }
        return terms;
    }

    private static boolean isStopWord(String w) {
        return STOP_WORDS.contains(w);
    }

    private static String normalize(String s) {
        String unified = s
                .replace('\u2019', '\'') // ’
                .replace('\u2018', '\'') // ‘
                .replace('\u201B', '\'') // ‛
                .replace('\u2032', '\'') // ′
                .replace('\u00B4', '\'') // ´
                .replace('\u201C', '"') // “
                .replace('\u201D', '"'); // ”
        String nfd = Normalizer.normalize(unified, Normalizer.Form.NFD);
        String noMarks = nfd.replaceAll("\\p{M}", "");
        return noMarks.toLowerCase(Locale.ROOT);
    }
}
