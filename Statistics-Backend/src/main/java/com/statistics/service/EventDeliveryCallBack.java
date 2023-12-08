package com.statistics.service;

import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author 신건우
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventDeliveryCallBack implements DeliverCallback {

    @Override
    public void handle(String consumerTag, Delivery message) throws IOException {

    }
}