package com.tahs.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.tahs.application.ports.MetadataRepository;
import com.tahs.domain.Book;
import com.tahs.domain.SharedBookData;

import java.util.List;

public class HazelcastMetadataRepository implements MetadataRepository {

    private static final String METADATA_MAP = "book-metadata";
    private final IMap<Integer, SharedBookData> metadata;

    public HazelcastMetadataRepository(HazelcastInstance hazelcast) {
        this.metadata = hazelcast.getMap(METADATA_MAP);
    }

    @Override
    public void save(Book book) {
        SharedBookData data = new SharedBookData(
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getLanguage()
        );
        metadata.put(book.getBookId(), data);
    }

    @Override
    public void deleteAll() {
        metadata.clear();
    }

    @Override
    public List<Book> getAll() {
        return metadata.values().stream()
                .map(d -> new Book(d.bookId(), d.title(), d.author(), d.language()))
                .toList();
    }

    public int size() {
        return metadata.size();
    }
}