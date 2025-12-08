package com.tahs.domain;

import java.io.Serializable;
import java.util.List;

public record BooksTerm(
        String term,
        List<String> booksId) implements Serializable {
}
