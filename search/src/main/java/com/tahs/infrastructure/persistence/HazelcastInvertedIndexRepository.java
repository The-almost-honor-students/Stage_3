package com.tahs.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.tahs.application.ports.InvertedIndexRepository;
import com.tahs.domain.BooksTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HazelcastInvertedIndexRepository implements InvertedIndexRepository {

    private static final String INDEX_MAP = "inverted-index";
    private final IMap<String, Set<Integer>> index;

    public HazelcastInvertedIndexRepository(HazelcastInstance hazelcast) {
        this.index = hazelcast.getMap(INDEX_MAP);
    }

    @Override
    public BooksTerm getBooksByTerm(String term) {
        Set<Integer> bookIds = index.get(term.toLowerCase());
        if (bookIds == null || bookIds.isEmpty()) {
            return new BooksTerm(term, new ArrayList<>());
        }
        // Convertir Set<Integer> a List<String>
        List<String> bookIdStrings = bookIds.stream()
                .map(String::valueOf)
                .toList();
        return new BooksTerm(term, bookIdStrings);
    }

    public int size() {
        return index.size();
    }
}