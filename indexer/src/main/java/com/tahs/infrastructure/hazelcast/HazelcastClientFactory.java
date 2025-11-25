package com.tahs.infrastructure.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastClientFactory {

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

        System.out.println("Connecting to Hazelcast at " + host + ":" + port + "...");
        HazelcastInstance instance = HazelcastClient.newHazelcastClient(config);
        System.out.println("Connected to Hazelcast cluster");

        return instance;
    }
}