package com.tahs.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.tahs.application.ports.MetadataRepository;
import com.tahs.domain.Book;

import java.util.List;

public class CachedMetadataRepository implements MetadataRepository {
    private final MetadataRepository repository;
    private final HazelcastInstance hazelcastInstance;

    public CachedMetadataRepository(MetadataRepository repository, HazelcastInstance hazelcastInstance) {
        this.repository = repository;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void save(Book book) {
        repository.save(book);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public List<Book> getAll() {
        return repository.getAll();
    }
}
