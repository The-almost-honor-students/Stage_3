package com.tahs.application.ports;

import com.tahs.domain.BookEvent;

public interface EventPublisher {
    void publish(BookEvent event);

    void close() throws Exception;
}
