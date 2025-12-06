package com.tahs.domain;

public record BookEvent(String bookId, String timestamp, String eventType, String filePath) {
}
