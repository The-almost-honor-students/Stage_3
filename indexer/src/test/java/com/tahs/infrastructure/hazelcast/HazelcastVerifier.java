package com.tahs.infrastructure.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class HazelcastVerifier {
    public static void main(String[] args) {
        System.out.println("Starting Hazelcast Verification...");

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName("gutenberg-search-cluster");

        clientConfig.getNetworkConfig().addAddress("192.168.1.135:5701");

        try {
            HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
            System.out.println("Connected to cluster!");

            IMap<String, String> testMap = client.getMap("verification-map");
            System.out.println("Putting value into map...");
            testMap.put("key1", "value1");

            System.out.println("Getting value from map...");
            String value = testMap.get("key1");

            if ("value1".equals(value)) {
                System.out.println("VERIFICATION SUCCESS: Retrieved value matches expected.");
            } else {
                System.out.println("VERIFICATION FAILURE: Retrieved value '" + value + "' does not match.");
            }
            client.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(
                    "VERIFICATION ERROR: Could not connect or perform operations. Make sure the Hazelcast cluster is running (docker-compose up).");
        }
    }
}
