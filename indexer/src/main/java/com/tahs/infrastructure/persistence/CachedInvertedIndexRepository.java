package com.tahs.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.tahs.application.ports.InvertedIndexRepository;
import com.tahs.domain.IndexStats;
import com.tahs.infrastructure.persistence.hazelcast.AddBookEntryProcessor;

import java.util.Set;

public class CachedInvertedIndexRepository implements InvertedIndexRepository {

    private final InvertedIndexRepository delegate;
    private final IMap<String, Set<String>> invertedIndex;

    public CachedInvertedIndexRepository(InvertedIndexRepository delegate, HazelcastInstance hazelcastInstance) {
        this.delegate = delegate;
        this.invertedIndex = hazelcastInstance.getMap("inverted-index");
    }

    @Override
    public boolean indexBook(String bookId, Set<String> terms) {
        for (String term : terms) {
            invertedIndex.executeOnKey(term, new AddBookEntryProcessor(bookId));
        }
        return delegate.indexBook(bookId, terms);
    }

    @Override
    public void deleteAll() {
        invertedIndex.clear();
        delegate.deleteAll();
    }

    @Override
    public IndexStats getStats() {
        return delegate.getStats();
    }
}
