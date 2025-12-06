package com.tahs.application.ports;

public interface EventConsumer {
    void startListening();

    void close() throws Exception;
}
