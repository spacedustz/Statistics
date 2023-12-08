package com.statistics.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EventSubscriber implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("Redis Data Subscriber 호출");

        String receivedMessage = new String(message.getBody(), StandardCharsets.UTF_8);

        log.info("[Redis] Received Event : {}", receivedMessage);
    }
}
