package com.tahs.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class BookEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum EventType {
        BOOK_INDEXED,
        BOOK_UPDATED,
        BOOK_DELETED
    }

    private final EventType eventType;
    private final Book book;
    private final Instant timestamp;

    public BookEvent(EventType eventType, Book book) {
        this.eventType = eventType;
        this.book = book;
        this.timestamp = Instant.now();
    }

    public BookEvent(EventType eventType, Book book, Instant timestamp) {
        this.eventType = eventType;
        this.book = book;
        this.timestamp = timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Book getBook() {
        return book;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BookEvent bookEvent = (BookEvent) o;
        return eventType == bookEvent.eventType &&
                Objects.equals(book, bookEvent.book) &&
                Objects.equals(timestamp, bookEvent.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, book, timestamp);
    }

    @Override
    public String toString() {
        return "BookEvent{" +
                "eventType=" + eventType +
                ", book=" + book +
                ", timestamp=" + timestamp +
                '}';
    }
}
