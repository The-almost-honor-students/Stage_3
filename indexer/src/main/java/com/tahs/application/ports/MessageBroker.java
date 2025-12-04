package com.tahs.application.ports;

import com.tahs.domain.BookEvent;

public interface MessageBroker extends AutoCloseable {

    void publish(BookEvent event);

    @Override
    void close() throws Exception;
}
