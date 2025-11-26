package com.tahs.domain;

import java.io.Serializable;

/**
 * Record compartido para almacenar metadatos de libros en Hazelcast.
 * Debe ser serializable para poder almacenarse en el IMap distribuido.
 */
public record SharedBookData(
        int bookId,
        String title,
        String author,
        String language
) implements Serializable {}
