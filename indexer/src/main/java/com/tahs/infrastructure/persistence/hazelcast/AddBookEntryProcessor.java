package com.tahs.infrastructure.persistence.hazelcast;

import com.hazelcast.map.EntryProcessor;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddBookEntryProcessor implements EntryProcessor<String, Set<String>, Object> {

    private final String bookId;

    public AddBookEntryProcessor(String bookId) {
        this.bookId = bookId;
    }

    @Override
    public Object process(Map.Entry<String, Set<String>> entry) {
        Set<String> books = entry.getValue();
        if (books == null) {
            books = new HashSet<>();
        }
        books.add(bookId);
        entry.setValue(books);
        return null;
    }
}
