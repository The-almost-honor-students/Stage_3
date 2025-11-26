package com.tahs.application.ports;

import com.tahs.domain.Book;

public interface MetadataRepository {
    Book getById(String bookId);
}
