package com.tahs.infrastructure.cache.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.tahs.application.ports.InvertedIndexRepository;
import com.tahs.domain.IndexStats;

import java.util.Set;

public class CachedInvertedIndexRepository implements InvertedIndexRepository {
    private final InvertedIndexRepository repository;
    private final HazelcastInstance hazelcastInstance;
    private final IMap<String, Set<String>> invertedIndexCache;

    public CachedInvertedIndexRepository(InvertedIndexRepository repository, HazelcastInstance hazelcastInstance) {
        this.repository = repository;
        this.hazelcastInstance = hazelcastInstance;
        this.invertedIndexCache = hazelcastInstance.getMap("inverted-index");
    }

    @Override
    public boolean indexBook(String book_id, Set<String> terms) {
        boolean result = repository.indexBook(book_id, terms);

        for (String term : terms) {
            invertedIndexCache.executeOnKey(term, new AddBookEntryProcessor(book_id));
        }

        return result;
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
        invertedIndexCache.clear();
    }

    @Override
    public IndexStats getStats() {
        return repository.getStats();
    }
}
