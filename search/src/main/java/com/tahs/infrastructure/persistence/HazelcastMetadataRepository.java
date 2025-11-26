package com.tahs.infrastructure.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.tahs.application.ports.MetadataRepository;
import com.tahs.domain.Book;
import com.tahs.domain.SharedBookData;

public class HazelcastMetadataRepository implements MetadataRepository {

    private static final String METADATA_MAP = "book-metadata";
    private final IMap<Integer, SharedBookData> metadata;

    public HazelcastMetadataRepository(HazelcastInstance hazelcast) {
        this.metadata = hazelcast.getMap(METADATA_MAP);
    }

    @Override
    public Book getById(String bookId) {
        try {
            Integer id = Integer.parseInt(bookId.replace("-body", "").replace("-header", ""));
            SharedBookData data = metadata.get(id);
            if (data == null) {
                return null;
            }
            return new Book(
                    data.bookId(),
                    data.title(),
                    data.author(),
                    data.language()
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int size() {
        return metadata.size();
    }
}