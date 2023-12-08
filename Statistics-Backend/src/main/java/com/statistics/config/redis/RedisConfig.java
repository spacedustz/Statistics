package com.statistics.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Conditional : yml의 spring.data.redis.enabled 의 값이 TRUE/T/YES/Y 일 경우에만 Spring Bean을 ㅗ등록
 */
@Configuration
@RequiredArgsConstructor
@Conditional(RedisPropertyBeanCondition.class)
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.topic.event:event}")
    private String eventTopicName;

    /* Redis Connection Factory */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    /* Event Channel Topic */
    @Bean
    public ChannelTopic eventTopic() {
        return new ChannelTopic(eventTopicName);
    }

    @Bean
    public EventSubscriber eventSubscriber() {
        return new EventSubscriber();
    }

    /* Redis Message Listener Adapter : RedisSubscriber의 onMessage()를 호출 */
    @Bean
    public MessageListenerAdapter listenerAdapter(EventSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    /* Redis Message Listener Container : Event Topic을 구독하고 onMessage()를 호출 */
    @Bean
    public RedisMessageListenerContainer redisEventListener(MessageListenerAdapter adapter,
                                                       ChannelTopic eventTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(adapter, eventTopic);

        return container;
    }

    /* Redis Template */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Key Serializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Value Serializer
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }
}
