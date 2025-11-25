package com.tahs.config;

public record AppConfig(
        String hazelcastHost,
        int hazelcastPort,
        int port
) {}