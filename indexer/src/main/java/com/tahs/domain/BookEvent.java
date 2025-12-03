package com.tahs.domain;

import java.io.Serializable;
import java.time.Instant;

public class BookEvent implements Serializable {
    private final String eventId;
    private final Instant timestamp;
    private final String type;
    private final Book payload;

    public BookEvent(String eventId, String type, Book payload) {
        this.eventId = eventId;
        this.timestamp = Instant.now();
        this.type = type;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public Book getPayload() {
        return payload;
    }
}
