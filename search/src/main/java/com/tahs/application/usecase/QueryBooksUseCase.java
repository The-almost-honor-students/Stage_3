package com.tahs.application.usecase;

import com.tahs.application.dto.SearchDto;
import com.tahs.application.ports.InvertedIndexRepository;
import com.tahs.application.ports.MetadataRepository;
import com.tahs.domain.BookMetadata;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryBooksUseCase {

    private final InvertedIndexRepository invertedIndexRepository;
    private final MetadataRepository metadataRepository;
    private final com.hazelcast.core.HazelcastInstance hazelcastInstance;

    public QueryBooksUseCase(InvertedIndexRepository invertedIndexRepository, MetadataRepository metadataRepository,
            com.hazelcast.core.HazelcastInstance hazelcastInstance) {
        this.invertedIndexRepository = invertedIndexRepository;
        this.metadataRepository = metadataRepository;
        this.hazelcastInstance = hazelcastInstance;
    }

    public SearchDto execute(Map<String, List<String>> params) {
        var term = getTermValue(params);

        com.hazelcast.map.IMap<String, com.tahs.domain.BooksTerm> cache = hazelcastInstance.getMap("inverted-index");

        var booksTerm = cache.get(term);
        if (booksTerm == null) {
            booksTerm = invertedIndexRepository.getBooksByTerm(term);
            if (booksTerm != null) {
                cache.put(term, booksTerm);
            }
        }

        List<BookMetadata> books = new ArrayList<>();
        if (booksTerm != null && booksTerm.booksId() != null) {
            for (String bookId : booksTerm.booksId()) {
                books.add(metadataRepository.getById(bookId));
            }
        }
        var bookMetadata = books.stream().filter(book -> matches(book, params)).toList();
        return new SearchDto(
                term,
                params,
                bookMetadata.size(),
                bookMetadata);
    }

    private boolean matches(BookMetadata book, Map<String, List<String>> params) {
        if (params.containsKey("author")) {
            return book.author().equals(getAuthorValue(params));
        }
        if (params.containsKey("language")) {
            return book.language().equals(getLanguageValue(params));
        }
        return true;
    }

    @Nullable
    private static String getTermValue(Map<String, List<String>> params) {
        return params.get("q").stream().findFirst().orElse(null);
    }

    @Nullable
    private String getAuthorValue(Map<String, List<String>> params) {
        return params.get("author").stream().findFirst().orElse(null);
    }

    @Nullable
    private String getLanguageValue(Map<String, List<String>> params) {
        return params.get("language").stream().findFirst().orElse(null);
    }

    @Nullable
    private String getYearValue(Map<String, List<String>> params) {
        return params.get("year").stream().findFirst().orElse(null);
    }

}
