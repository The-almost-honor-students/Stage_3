package com.tahs.infrastructure.serialization.books;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TextTokenizerTest {

    @Test
    void testExtractTermsIncludesNonStopWords() {
        String input = "Hello world";
        Set<String> terms = TextTokenizer.extractTerms(input);

        assertTrue(terms.contains("hello"), "Should contain 'hello'");
        assertTrue(terms.contains("world"), "Should contain 'world'");
        assertEquals(2, terms.size());
    }

    @Test
    void testExtractTermsExcludesStopWords() {
        String input = "The quick brown fox is fast";
        Set<String> terms = TextTokenizer.extractTerms(input);

        assertFalse(terms.contains("the"), "Should not contain stop word 'the'");
        assertFalse(terms.contains("is"), "Should not contain stop word 'is'");
        assertTrue(terms.contains("quick"));
        assertTrue(terms.contains("brown"));
        assertTrue(terms.contains("fox"));
        assertTrue(terms.contains("fast"));
    }

    @Test
    void testEmptyInput() {
        assertTrue(TextTokenizer.extractTerms("").isEmpty());
        assertTrue(TextTokenizer.extractTerms(null).isEmpty());
    }
}
