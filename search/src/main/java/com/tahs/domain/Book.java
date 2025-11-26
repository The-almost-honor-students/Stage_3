package com.tahs.domain;

import java.io.Serializable;

public record Book(
        int bookId,
        String title,
        String author,
        String language
) implements Serializable {}