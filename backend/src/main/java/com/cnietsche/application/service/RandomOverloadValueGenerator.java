package com.cnietsche.application.service;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomOverloadValueGenerator {

    private static final String[] WORDS = {
            "server", "latency", "cache", "pipeline", "request", "timeout", "cluster",
            "database", "replica", "firewall", "loadbalancer", "throughput", "bandwidth",
            "container", "kubernetes", "docker", "microservice", "gateway", "broker",
            "queue", "stream", "shard", "partition", "replication", "failover", "backup",
            "snapshot", "metrics", "alert", "monitoring", "deployment", "rollback",
            "scaling", "autoscaling", "endpoint", "protocol", "socket", "thread",
            "memory", "cpu", "disk", "network", "packet", "routing", "subnet"
    };

    private static final int MIN_WORDS = 8;
    private static final int MAX_WORDS = 20;
    private static final int MAX_LENGTH = 255;

    private RandomOverloadValueGenerator() {
    }

    public static String generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int wordCount = random.nextInt(MIN_WORDS, MAX_WORDS + 1);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(WORDS[random.nextInt(WORDS.length)]);
        }
        String result = builder.toString();
        if (result.length() > MAX_LENGTH) {
            return result.substring(0, MAX_LENGTH);
        }
        return result;
    }
}
