package com.tahs.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.tahs.application.ports.MetadataRepository;
import com.tahs.domain.Book;

import java.util.List;

public class CachedMetadataRepository implements MetadataRepository {

    private final MetadataRepository delegate;
    private final IMap<String, Book> cache;

    public CachedMetadataRepository(MetadataRepository delegate, HazelcastInstance hazelcastInstance) {
        this.delegate = delegate;
        this.cache = hazelcastInstance.getMap("books");
    }

    @Override
    public void save(Book book) {
        delegate.save(book);
        cache.put(String.valueOf(book.getBookId()), book);
    }

    @Override
    public List<Book> getAll() {
        // For getAll, we might still want to go to DB to be sure, or if cache is full
        // replica we could use it.
        // Assuming cache might be partial or eviction policy exists, we delegate to DB.
        // If we wanted to use cache, we would need to ensure it's fully loaded.
        return delegate.getAll();
    }

    @Override
    public void deleteAll() {
        delegate.deleteAll();
        cache.clear();
    }
}
