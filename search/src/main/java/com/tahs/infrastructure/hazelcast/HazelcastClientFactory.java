package com.tahs.infrastructure.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;

import java.util.Arrays;
import java.util.List;

public class HazelcastClientFactory {

    public static HazelcastInstance create(String hazelcastMembersStr) {
        return create(hazelcastMembersStr, null);
    }

    public static HazelcastInstance create(String hazelcastMembersStr, String nodeIp) {
        List<String> members = Arrays.asList(hazelcastMembersStr.split(","));
        return create(members, nodeIp);
    }

    public static HazelcastInstance create(String host, int port) {
        return create(Arrays.asList(host + ":" + port));
    }

    public static HazelcastInstance create(List<String> members) {
        return create(members, null);
    }

    public static HazelcastInstance create(List<String> members, String nodeIp) {

        ClientConfig cfg = new ClientConfig();
        cfg.setClusterName("gutenberg-search-cluster");

        if (nodeIp != null) {
            cfg.setProperty("hazelcast.socket.bind.any", "false");
            cfg.setProperty("hazelcast.local.localAddress", nodeIp);
        }

        ClientNetworkConfig network = cfg.getNetworkConfig();
        members.forEach(network::addAddress);

        network.setSmartRouting(true);

        cfg.getConnectionStrategyConfig()
                .getConnectionRetryConfig()
                .setClusterConnectTimeoutMillis(20000);

        NearCacheConfig nearCache = new NearCacheConfig("inverted-index")
                .setInvalidateOnChange(true)
                .setTimeToLiveSeconds(0)
                .setMaxIdleSeconds(0);

        cfg.addNearCacheConfig(nearCache);

        return HazelcastClient.newHazelcastClient(cfg);
    }
}
