package com.tahs.infrastructure.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastClientFactory {

    private static final String INVERTED_INDEX_MAP = "inverted-index";
    private static final String METADATA_MAP = "book-metadata";

    public static HazelcastInstance create(String host, int port) {
        ClientConfig config = new ClientConfig();
        config.setClusterName("books-cluster");
        config.getNetworkConfig()
              .addAddress(host + ":" + port)
              .setConnectionTimeout(10000);

        config.getConnectionStrategyConfig()
              .getConnectionRetryConfig()
              .setInitialBackoffMillis(1000)
              .setMaxBackoffMillis(30000)
              .setClusterConnectTimeoutMillis(120000);

        // Configurar Near-Cache para inverted-index (mejora lecturas repetidas)
        EvictionConfig evictionConfigIndex = new EvictionConfig()
                .setSize(10000)
                .setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT)
                .setEvictionPolicy(EvictionPolicy.LRU);

        NearCacheConfig invertedIndexNearCache = new NearCacheConfig(INVERTED_INDEX_MAP)
                .setInMemoryFormat(InMemoryFormat.BINARY)
                .setInvalidateOnChange(true)
                .setTimeToLiveSeconds(300)
                .setMaxIdleSeconds(60)
                .setEvictionConfig(evictionConfigIndex);
        config.addNearCacheConfig(invertedIndexNearCache);

        // Configurar Near-Cache para book-metadata
        EvictionConfig evictionConfigMeta = new EvictionConfig()
                .setSize(10000)
                .setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT)
                .setEvictionPolicy(EvictionPolicy.LRU);

        NearCacheConfig metadataNearCache = new NearCacheConfig(METADATA_MAP)
                .setInMemoryFormat(InMemoryFormat.BINARY)
                .setInvalidateOnChange(true)
                .setTimeToLiveSeconds(300)
                .setMaxIdleSeconds(60)
                .setEvictionConfig(evictionConfigMeta);
        config.addNearCacheConfig(metadataNearCache);

        System.out.println("Connecting to Hazelcast at " + host + ":" + port + "...");
        HazelcastInstance instance = HazelcastClient.newHazelcastClient(config);
        System.out.println("Connected to Hazelcast cluster");
        System.out.println("Near-Cache configured for maps: " + INVERTED_INDEX_MAP + ", " + METADATA_MAP);

        return instance;
    }
}