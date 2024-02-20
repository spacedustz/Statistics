package statistics.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class Props {
    /* Thread Pool */
    @Value("${thread.core-pool-size}")
    private int corePoolSize;

    @Value("${thread.max-pool-size}")
    private int maxPoolSize;

    @Value("${thread.queue-capacity}")
    private int queueCapacity;

    @Value("${thread.name-prefix}")
    private String namePrefix;

    /* RabbitMQ */
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

    @Value("${rabbit.queues}")
    private List<String> queues;
}
