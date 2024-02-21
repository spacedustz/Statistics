package statistics.service.rabbit;

import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import statistics.constants.ApplicationConstants;
import statistics.constants.RedisConstants;
import statistics.dto.EventDto;
import statistics.dto.SecuRTAreaOccupancyEnterEventImageDto;
import statistics.dto.SecuRTAreaOccupancyExitEventImageDto;
import statistics.enums.EventType;
import statistics.manager.LockManager;
import statistics.service.JsonParser;
import statistics.util.DateUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author 신건우
 * RabbitMQ Channel Consume이 취소 됬을때 호출되는 콜백
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventDeliveryCallBack implements DeliverCallback {
    private final JsonParser jsonParser;
    private final LockManager lockManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void handle(String s, Delivery message) throws IOException {
        String routingKey = message.getEnvelope().getRoutingKey();
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        String now = DateUtil.getTime();

        List<EventDto> eventDtoList = this.mapToEventDto(msg, routingKey);

        if (eventDtoList == null || eventDtoList.isEmpty()) {
            log.warn("Basic Consume - Invalid Event : {}", msg);
            return;
        } else if (eventDtoList.size() == 1 && eventDtoList.get(0).getEventType() == EventType.UNKNOWN) {
            log.warn("Basic Consume - Invalid Event : {}, {}", EventType.UNKNOWN.toStr(), msg);
            return;
        }

        for (int i = 0; i < eventDtoList.size(); i++) {
            EventDto eventDto = eventDtoList.get(i);
            String eventTimeStr = String.valueOf(eventDto.getEventTime()).substring(0, 10);
            eventDto.setEventTime(Long.parseLong(DateUtil.timestampToDate(Long.parseLong(eventTimeStr), ApplicationConstants.SEOUL_TIMEZONE)));
            String eventTime = String.valueOf(eventDto.getEventTime());

            if (!StringUtils.hasText(eventDto.getInstanceExtName())) {
                log.warn("Basic Consume - Unknown Instance : {}", msg);
            }

            Lock lock = lockManager.getLock(eventDto.getInstanceName());

            try {
                if (lock.tryLock(3, TimeUnit.SECONDS)) {
                    redisTemplate.opsForValue().set(RedisConstants.INSTANCE + eventDto.getInstanceName(), eventTime);

                    // 통계 생성용 데이터 Redis Hash로 저장
                    if (eventDto.getPeopleCount() > 0) {
                        redisTemplate.opsForHash().put(RedisConstants.INSTANCE_COUNT + eventDto.getInstanceName(), eventTime, eventDto.getPeopleCount());
                    }
                }
            } catch (InterruptedException e) {
                log.warn("BasicConsume - Interrupted Exception : {}", e.getMessage());
            }
        }
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
                    dto.setInstanceName(routingKey);
                    dto.setInstanceExtName(routingKey);
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
                    dto.setInstanceName(routingKey);
                    dto.setInstanceExtName(routingKey);
                    dto.setEventTime(Long.parseLong(((SecuRTAreaOccupancyExitEventImageDto) msgObject).getSystemTimestamp()));
                    dto.setEventType(EventType.SecuRTAreaOccupancyExitEventImageDto);

                    list.add(dto);
                }
            }
        }

        return list;
    }
}
