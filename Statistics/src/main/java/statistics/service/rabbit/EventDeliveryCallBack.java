package statistics.service.rabbit;

import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import statistics.dto.EventDto;
import statistics.dto.SecuRTAreaOccupancyEnterEventImageDto;
import statistics.dto.SecuRTAreaOccupancyExitEventImageDto;
import statistics.enums.EventType;
import statistics.service.JsonParser;
import statistics.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 신건우
 * RabbitMQ Channel Consume이 취소 됬을때 호출되는 콜백
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventDeliveryCallBack implements DeliverCallback {
    private final JsonParser jsonParser;

    @Value("${event.image.path}")
    private String eventImagePath;

    @Override
    public void handle(String s, Delivery message) throws IOException {
        String routingKey = message.getEnvelope().getRoutingKey();
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        Integer alarmCheckSeconds = 60;
        String eventTime = "";
        String now = DateUtil.getTime();

        List<EventDto> eventDtoList = this.mapToEventDto(msg, routingKey);
    }

    private List<EventDto> mapToEventDto(final String msg, final String routingKey) {
        Object msgObject = null;
        int count = 0;
        List<EventDto> list = new ArrayList<>();

        try {
            msgObject = jsonParser.mapJson(msg);
        } catch (Exception e) {
            log.warn("Map to EventDto Exception : {}", e.getMessage());
            return list;
        }

        if (msgObject != null) {
            if (msgObject instanceof SecuRTAreaOccupancyEnterEventImageDto) {
                List<SecuRTAreaOccupancyEnterEventImageDto.Event> eventList = ((SecuRTAreaOccupancyEnterEventImageDto) msgObject).getEvents();

                for (SecuRTAreaOccupancyEnterEventImageDto.Event event : eventList) {
                    EventDto dto = new EventDto();
                    count = event.getExtra().getCurrentEntries();

                    dto.setPeopleCount(count);
                    dto.setImage(event.getImage());
                    dto.setInstanceName(this.extractInstanceName(event.getInstanceId()));
                    dto.setInstanceExtName(this.extractInstanceName(event.getInstanceId()));
                    dto.setEventTime(Long.parseLong(((SecuRTAreaOccupancyEnterEventImageDto) msgObject).getSystemTimestamp()));
                    dto.setEventType(EventType.SecuRTAreaOccupancyEnterEventImageDto);

                    list.add(dto);
                }
            } else if (msgObject instanceof SecuRTAreaOccupancyExitEventImageDto) {
                List<SecuRTAreaOccupancyExitEventImageDto.Event> eventList = ((SecuRTAreaOccupancyExitEventImageDto) msgObject).getEvents();

                for (SecuRTAreaOccupancyExitEventImageDto.Event event : eventList) {
                    EventDto dto = new EventDto();
                    count = event.getExtra().getCurrentEntries();

                    dto.setPeopleCount(count);
                    dto.setImage(event.getImage());
                    dto.setInstanceName(this.extractInstanceName(event.getInstanceId()));
                    dto.setInstanceExtName(this.extractInstanceName(event.getInstanceId()));
                    dto.setEventTime(Long.parseLong(((SecuRTAreaOccupancyExitEventImageDto) msgObject).getSystemTimestamp()));
                    dto.setEventType(EventType.SecuRTAreaOccupancyExitEventImageDto);

                    list.add(dto);
                }
            }
        }

        return list;
    }

    private String extractInstanceName(final String instanceName) {
        String result = null;

        if (StringUtils.hasText(instanceName)) {
            if (instanceName.contains(File.separator) && instanceName.contains(".json")) {
                String[] arr = instanceName.split(File.separator);

                result = arr[arr.length -1].split(".json")[0];
            } else {
                result = instanceName.trim();
            }
        }

        return result;
    }
}
