package com.tahs.infrastructure.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;

import java.util.Arrays;
import java.util.List;

public class HazelcastClientFactory {

    private static final List<String> CLUSTER_MEMBERS = Arrays.asList(
            "192.168.1.133",
            "192.168.1.135",
            "192.168.1.133");

            //"10.26.14.223:5701",
            //"10.26.14.222:5701",
            //"10.26.14.221:5701");

    public static HazelcastInstance create() {
        return create(CLUSTER_MEMBERS);
    }

    public static HazelcastInstance create(String host, int port) {
        return create(Arrays.asList(host + ":" + port));
    }

    public static HazelcastInstance create(List<String> members) {
        ClientConfig cfg = new ClientConfig();
        cfg.setClusterName("gutenberg-search-cluster");

        for (String member : members) {
            cfg.getNetworkConfig().addAddress(member);
        }

        cfg.getNetworkConfig().setSmartRouting(true);

        cfg.getConnectionStrategyConfig()
                .getConnectionRetryConfig()
                .setClusterConnectTimeoutMillis(20000);

        NearCacheConfig nearCache = new NearCacheConfig("inverted-index")
                .setCacheLocalEntries(true)
                .setInvalidateOnChange(true);

        cfg.addNearCacheConfig(nearCache);

        return HazelcastClient.newHazelcastClient(cfg);
    }
}
