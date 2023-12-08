package com.statistics.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class Props {
    /* Redis */
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.topic.event:event}")
    private String eventTopicName;

    /* Thread Pool */
    @Value("${task.executor.core.pool.size}")
    private int corePoolSize;

    @Value("${task.executor.max.pool.size}")
    private int maxPoolSize;

    @Value("${task.executor.queue.capacity}")
    private int queueCapacity;

    /* RabbitMQ */
    @Value("${rabbit.queues}")
    private List<String> queues;

    @Value("${rabbit.host}")
    private String host;

    @Value("${rabbit.port}")
    private int port;

    @Value("${rabbit.username}")
    private String username;

    @Value("${rabbit.password}")
    private String password;

    @Value("${rabbit.channels}")
    private int channelCount;
}
