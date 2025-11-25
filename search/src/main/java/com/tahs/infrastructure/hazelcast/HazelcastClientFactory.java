package com.tahs.infrastructure.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastClientFactory {

    public static HazelcastInstance create(String host, int port) {
        ClientConfig cfg = new ClientConfig();

        cfg.getNetworkConfig().addAddress(host + ":" + port);

        NearCacheConfig nearCache = new NearCacheConfig("inverted-index")
                .setCacheLocalEntries(true);

        cfg.addNearCacheConfig(nearCache);

        return HazelcastClient.newHazelcastClient(cfg);
    }
}
