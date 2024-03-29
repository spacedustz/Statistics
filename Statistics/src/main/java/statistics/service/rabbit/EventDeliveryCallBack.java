package statistics.service.rabbit;

import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import statistics.service.redis.RedisService;
import statistics.util.DateUtil;
import statistics.util.Props;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    private final RedisService redisService;
    private final Props props;

    @Override
    public void handle(String s, Delivery message) throws IOException {
        String routingKey = message.getEnvelope().getRoutingKey();
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        String currentAlarmLevel = "";
        String newAlarmLevel = ApplicationConstants.ALARM_LEVEL_1;
        int alarmLevel = 1;
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
                    // TODO 1: 현재 알람 레벨을 Redis에서 조회
                    currentAlarmLevel = redisService.getCurrentAlarmLevel(RedisConstants.INSTANCE_CURRENT_ALARM_LEVEL + eventDto.getInstanceName());

                    // TODO 2: Redis에 현재 알람 레벨이 없으면 MariaDB에서 조회
                    if (!StringUtils.hasLength(currentAlarmLevel)) {
                        redisService.setCurrentAlarmLevel(RedisConstants.INSTANCE_CURRENT_ALARM_LEVEL + eventDto.getInstanceName(), ApplicationConstants.ALARM_LEVEL_1);
                    }

                    redisService.setInstanceEventTime(RedisConstants.INSTANCE + eventDto.getInstanceName(), eventTime);

                    /* ===== 통계 생성용 데이터 Redis Hash에 저장 ===== */
                    if (eventDto.getPeopleCount() > 0) {
                        redisService.setPeopleCount(RedisConstants.INSTANCE_COUNT + eventDto.getInstanceName(), eventTime, eventDto.getPeopleCount());
                    }

                    // 현재 알람 레벨과 새로운 알람 레빌이 다를때
                    if (!newAlarmLevel.equals(currentAlarmLevel)) {
                        Set keys = redisService.getHashKeys(RedisConstants.INSTANCE_LATEST_ALARM_LEVEL + eventDto.getInstanceName());

                        // 새로운 알람레벨 이외의 다른 Hash 필드 삭제
                        if (keys != null && keys.size() > 0) {
                            for (Object key : keys) {
                                String hashKey = (String) key;
                                if (!hashKey.equals(newAlarmLevel)) {
                                    redisService.deleteHashKey(RedisConstants.INSTANCE_LATEST_ALARM_LEVEL + eventDto.getInstanceName(), hashKey);
                                }
                            }

                            // 새로운 알람 레벨의 이벤트를 수신한 시간 조회
                            String alarmLatestTimeStamp = (String) redisService.getHashValue(RedisConstants.INSTANCE_LATEST_ALARM_LEVEL + eventDto.getInstanceName(), newAlarmLevel);

                            // 이미 알람 레벨 변경 처리를 한 경우 continue
                            if (StringUtils.hasText(alarmLatestTimeStamp)) {
                                if (!alarmLatestTimeStamp.equals("-1")) {
                                    continue;
                                }

                                // 새로운 알람 레벨 이벤트를 수신한 시간이 현재 시간과 ApplicationConstants.CHECK_ALARM_INTERVAL의 시간만큼 차이가 난다면 새로운 알림으로 변경
                                // 알람 변경 처리를 완료했다면 Hash 필드의 값을 -1로 설정

                                if (DateUtil.getSecondsDifference(alarmLatestTimeStamp, now) > ApplicationConstants.CHECK_ALARM_INTERVAL) {
                                    redisService.setCurrentAlarmLevel(RedisConstants.INSTANCE_CURRENT_ALARM_LEVEL + eventDto.getInstanceName(), newAlarmLevel);
                                    redisService.setHashValue(RedisConstants.INSTANCE_LATEST_ALARM_LEVEL + eventDto.getInstanceName(), newAlarmLevel, "-1");

                                    // MariaDB도 업데이트
                                    // TODO ...

                                    // 이미지 저장
                                    if (StringUtils.hasLength(eventDto.getImage())) {
                                        this.saveImageFile(eventDto.getImage(), routingKey);
                                    }
                                }
                            } else {
                                // 알람 레벨 시간이 없을 경우 최초 알람 레벨 시간 저장 + Update MariaDB Alarm Level
                                redisService.setHashValue(RedisConstants.INSTANCE_LATEST_ALARM_LEVEL + eventDto.getInstanceName(), newAlarmLevel, now);
                            }
                        } else {
                            // Redis Hash Key가 존재 하지 않을 경우 최초 알람 레벨 시간 저장 + Update MariaDB Alarm Level
                            redisService.setHashValue(RedisConstants.INSTANCE_LATEST_ALARM_LEVEL + eventDto.getInstanceName(), newAlarmLevel, now);
                        }
                    } else {
                        // Update MariaDB Alarm Level
                    }
                } else {
                    // Try Lock, 현재 알람 레벨과 수신한 알람 레벨이 같으면 Update MariaDB Alarm Level
                }
            } catch (InterruptedException e) {
                log.warn("BasicConsume - Interrupted Exception : {}", e.getMessage());
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
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
                    dto.setTotalSquareMeter(new Random().nextInt(21) + 30);

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
                    dto.setTotalSquareMeter(new Random().nextInt(21) + 30);

                    list.add(dto);
                }
            }
        }

        return list;
    }

    private String saveImageFile(final String base64EncodedImage, final String instanceName) {
        String fullPath = "";
        String imageUri = "";

        if (!StringUtils.hasLength(base64EncodedImage)) return "";

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedImage);
            String directory = props.getEventImagePath();

            if (!directory.endsWith(File.separator)) {
                directory = directory + File.separator;
            }

            String time = DateUtil.getTimeMilli();
            String yyyymmdd = time.substring(0, 8);

            // 이미지를 저장할 하위 디렉터리 경로 생성 : "2024/02/22/"
            StringBuilder imagePathBuilder = new StringBuilder();
            String imagePath = imagePathBuilder
                    .append(yyyymmdd, 0, 4)
                    .append(File.separator)
                    .append(yyyymmdd, 4, 6)
                    .append(File.separator)
                    .append(yyyymmdd, 6, 8)
                    .append(File.separator)
                    .toString();

            // 전체 디렉터리 경로에 이미지 하위디렉터리 경로 추가 : "/data/img/2024/02/22/"
            directory += imagePath;

            // 이미지 파일명 생성 : "routingKey_2024022213131333.png
            String imageFileName = instanceName + "_" + time + ".png";

            File file = new File(directory);

            if (!file.exists()) {
                file.mkdirs();
            }

            // 이미지 URI 생성 : "/2024/02/22/routingKey_2024022213131333.png"
            imageUri = File.separator + imagePath + imageFileName;

            // 전체 파일 경로 생성 : "/data/img/2024/02/22/routingKey_2024022213131333.png"
            fullPath = directory + imageFileName;

            // Full Path를 Paths 객체로 변환
            Path outputPath = Paths.get(fullPath);

            // 디코딩된 이미지 데이터를 파일로 저장
            Files.write(outputPath, decodedBytes);
        } catch (Exception e) {
            log.warn("Save Image Exception : {}", e.getMessage());
        }

        return imageUri;
    }
}
