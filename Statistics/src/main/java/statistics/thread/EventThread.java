package statistics.thread;


import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import statistics.service.rabbit.RabbitService;

/**
 * @author 신건우
 * Consume RabbitMQ Channel
 */
@Slf4j
@AllArgsConstructor
public class EventThread extends Thread {
    private RabbitService rabbitService;
    private Channel channel;
    private String queueName;

    @Override
    public void run() {
        rabbitService.listen(channel, queueName);
        log.info("RabbitMQ Channel {} Thread Start", channel.getChannelNumber());
    }
}