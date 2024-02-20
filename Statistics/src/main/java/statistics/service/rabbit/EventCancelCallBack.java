package statistics.service.rabbit;

import com.rabbitmq.client.CancelCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author 신건우
 * RabbitMQ Channel Consume이 취소 됬을때 호출되는 콜백
 */
@Slf4j
@Service
public class EventCancelCallBack implements CancelCallback {
    @Override
    public void handle(String consumerTag) throws IOException {
        log.warn("RabbitMQ Consumer Canceled - {}", consumerTag);
    }
}
