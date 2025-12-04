package com.tahs.config;

public class ActiveMQClusterConfig {

    private static final String[] BROKER_URLS = {
            "tcp://10.26.14.223:61616",
            "tcp://10.26.14.222:61616",
            "tcp://10.26.14.221:61616"
    };

    public static String getFailoverUrl() {
        return "failover:(" + String.join(",", BROKER_URLS) + ")" +
                "?randomize=true" +
                "&maxReconnectAttempts=-1" +
                "&initialReconnectDelay=1000" +
                "&maxReconnectDelay=30000";
    }

    public static String getBrokerUrlForNode(String nodeIp) {
        for (String brokerUrl : BROKER_URLS) {
            if (brokerUrl.contains(nodeIp)) {
                return brokerUrl;
            }
        }
        return getFailoverUrl();
    }

    public static String[] getAllBrokerUrls() {
        return BROKER_URLS.clone();
    }
}
