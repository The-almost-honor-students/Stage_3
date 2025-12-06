package com.tahs.domain;

public record IngestionEvent(String bookId, String timestamp, String eventType, String filePath) {
}
