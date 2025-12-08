package com.tahs.application.usecase;

import com.hazelcast.map.IMap;
import com.tahs.application.dto.SearchDto;
import com.tahs.application.ports.InvertedIndexRepository;
import com.tahs.application.ports.MetadataRepository;
import com.tahs.domain.BookMetadata;
import com.tahs.domain.BooksTerm;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class QueryBooksUseCase {

    private final InvertedIndexRepository invertedIndexRepository;
    private final MetadataRepository metadataRepository;
    private final com.hazelcast.core.HazelcastInstance hazelcastInstance;

    public QueryBooksUseCase(InvertedIndexRepository invertedIndexRepository,
                             MetadataRepository metadataRepository,
                             com.hazelcast.core.HazelcastInstance hazelcastInstance) {
        this.invertedIndexRepository = invertedIndexRepository;
        this.metadataRepository = metadataRepository;
        this.hazelcastInstance = hazelcastInstance;
    }

    public SearchDto execute(Map<String, List<String>> params) {
        var term = getTermValue(params);

        if (term == null || term.isBlank()) {
            return new SearchDto(null, params, 0, List.of());
        }

        IMap<String, Set<String>> cache = hazelcastInstance.getMap("inverted-index");

        Set<String> bookIds = cache.get(term);

        if (bookIds == null) {
            BooksTerm booksTerm = invertedIndexRepository.getBooksByTerm(term);
            if (booksTerm != null && booksTerm.booksId() != null) {
                bookIds = new HashSet<>(booksTerm.booksId());
                cache.put(term, bookIds);
            }
        }

        List<BookMetadata> books = new ArrayList<>();
        if (bookIds != null) {
            for (String bookId : bookIds) {
                BookMetadata metadata = metadataRepository. getById(bookId);
                if (metadata != null) {
                    books.add(metadata);
                }
            }
        }

        List<BookMetadata> filtered = books.stream()
                .filter(book -> matches(book, params))
                .collect(Collectors.toList());

        return new SearchDto(
                term,
                params,
                filtered.size(),
                filtered
        );
    }

    private boolean matches(BookMetadata book, Map<String, List<String>> params) {
        if (params.containsKey("author")) {
            String author = getAuthorValue(params);
            if (author != null && !author.equals(book.author())) {
                return false;
            }
        }

        if (params.containsKey("language")) {
            String language = getLanguageValue(params);
            if (language != null && !language.equals(book.language())) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    private static String getTermValue(Map<String, List<String>> params) {
        return params.getOrDefault("q", List.of())
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Nullable
    private String getAuthorValue(Map<String, List<String>> params) {
        return params.getOrDefault("author", List.of())
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Nullable
    private String getLanguageValue(Map<String, List<String>> params) {
        return params.getOrDefault("language", List.of())
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Nullable
    private String getYearValue(Map<String, List<String>> params) {
        return params.getOrDefault("year", List.of())
                .stream()
                .findFirst()
                .orElse(null);
    }
}
