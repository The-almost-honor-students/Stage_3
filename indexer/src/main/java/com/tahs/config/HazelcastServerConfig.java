package com.tahs.config;

import com.hazelcast.config.*;

import java.util.Arrays;
import java.util.List;

public class HazelcastServerConfig {

    private static final String CLUSTER_NAME = "gutenberg-search-cluster";
    private static final List<String> CLUSTER_MEMBERS = Arrays.asList(
            "192.168.1.133:5701",
            "192.168.1.135:5701",
            "192.168.1.143:5701");

    public static Config createConfig(String hazelcastMembersStr) {
        Config config = new Config();
        List<String> members = Arrays.asList(hazelcastMembersStr.split(","));

        config.setClusterName(CLUSTER_NAME);

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(5701);
        networkConfig.setPortAutoIncrement(true);
        networkConfig.setPortCount(10);
        networkConfig.getRestApiConfig().setEnabled(true);

        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);

        TcpIpConfig tcpIpConfig = joinConfig.getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.setMembers(members);
        tcpIpConfig.setConnectionTimeoutSeconds(10);

        MapConfig invertedIndexMapConfig = new MapConfig("inverted-index");
        invertedIndexMapConfig.setBackupCount(3);
        invertedIndexMapConfig.setAsyncBackupCount(0);
        invertedIndexMapConfig.setReadBackupData(true);

        invertedIndexMapConfig.setInMemoryFormat(InMemoryFormat.BINARY);

        EvictionConfig evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU);
        evictionConfig.setMaxSizePolicy(MaxSizePolicy.PER_NODE);
        evictionConfig.setSize(10000);
        invertedIndexMapConfig.setEvictionConfig(evictionConfig);

        config.addMapConfig(invertedIndexMapConfig);

        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
        managementCenterConfig.setScriptingEnabled(true);
        config.setManagementCenterConfig(managementCenterConfig);

        return config;
    }

    public static Config createConfigForNode(String nodeIp, AppConfig appConfig) {
        Config config = createConfig(appConfig.hazelcastMembers());

        config.getNetworkConfig().setPublicAddress(nodeIp);

        return config;
    }
}
