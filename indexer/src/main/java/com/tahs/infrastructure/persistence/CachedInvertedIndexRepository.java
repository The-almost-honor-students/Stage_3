package com.tahs.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.tahs.application.ports.InvertedIndexRepository;
import com.tahs.domain.IndexStats;

import java.util.Set;

public class CachedInvertedIndexRepository implements InvertedIndexRepository {
    private final InvertedIndexRepository repository;
    private final HazelcastInstance hazelcastInstance;

    public CachedInvertedIndexRepository(InvertedIndexRepository repository, HazelcastInstance hazelcastInstance) {
        this.repository = repository;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public boolean indexBook(String book_id, Set<String> terms) {
        return repository.indexBook(book_id, terms);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public IndexStats getStats() {
        return repository.getStats();
    }
}
