package com.tahs.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.tahs.application.ports.InvertedIndexRepository;
import com.tahs.domain.BooksTerm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class HazelcastInvertedIndexRepository implements InvertedIndexRepository {
    private final IMap<String, Set<String>> map;

    public HazelcastInvertedIndexRepository(HazelcastInstance hz) {
        this.map = hz.getMap("inverted-index");
    }

    @Override
    public BooksTerm getBooksByTerm(String term) {
        Set<String> ids = map.getOrDefault(term, Collections.emptySet());
        return new BooksTerm(term, new ArrayList<>(ids));
    }
}
