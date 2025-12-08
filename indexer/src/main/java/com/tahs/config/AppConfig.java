package com.tahs.config;

public record AppConfig(
                String dbUrl,
                String collectionMetadataName,
                String collectionIndexName,
                String databaseName,
                int port,
                String activeMqUrl,
                String activeMqUsername,
                String activeMqPassword,
                String hazelcastMembers) {
}
