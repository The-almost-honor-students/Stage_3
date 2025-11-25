package com.tahs.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.tahs.application.ports.InvertedIndexRepository;
import com.tahs.domain.IndexStats;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class HazelcastInvertedIndexRepository implements InvertedIndexRepository {

    private static final String INDEX_MAP = "inverted-index";
    private final IMap<String, Set<String>> index;

    public HazelcastInvertedIndexRepository(HazelcastInstance hazelcast) {
        this.index = hazelcast.getMap(INDEX_MAP);
    }

    @Override
    public boolean indexBook(String bookId, Set<String> terms) {
        try {
            // Limpiar el bookId (puede venir como "123-body")
            String cleanBookId = bookId.replace("-body", "").replace("-header", "");

            for (String term : terms) {
                index.compute(term, (key, existingBookIds) -> {
                    Set<String> bookIds = existingBookIds != null
                            ? new HashSet<>(existingBookIds)
                            : new HashSet<>();
                    bookIds.add(cleanBookId);
                    return bookIds;
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void deleteAll() {
        index.clear();
    }

    @Override
    public IndexStats getStats() {
        int totalTerms = index.size();
        // Estimación del tamaño en MB
        long estimatedBytes = index.entrySet().stream()
                .mapToLong(e -> e.getKey().length() + e.getValue().size() * 8L)
                .sum();
        double sizeMB = estimatedBytes / (1024.0 * 1024.0);
        return new IndexStats(sizeMB, Instant.now());
    }
}