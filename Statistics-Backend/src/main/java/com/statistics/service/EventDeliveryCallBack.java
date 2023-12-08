package com.statistics.service;

import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.statistics.dto.TripwireDto;
import com.statistics.entity.Count;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 신건우
 * RabbitMQ Channel에서 받은 Event를 변환 후 Wisedigm Tomcat 서버로 HTTP API 요청
 * 이벤트 Label을 차량 (V), 사람 (P), 자전거 (B), 사람/자전거 (A)로 분류
 * <p>
 * RabbitMQ Queue Name = Instance Name과 동일함 -> Cam01
 * Table Column Name = "B", "V", "P" 를 이름 뒤에 붙임
 * Instance Wire Name = ex) Cam01-P01 , Cam01-B01, Cam01-V01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventDeliveryCallBack implements DeliverCallback {
    private final RestApiService restApiService;
    private final JsonParser jsonParser;
    private final CountService countService;
    private static final String IN = "IN";

    @Override
    public void handle(String consumerTag, Delivery message) throws IOException {
        String routingKey = message.getEnvelope().getRoutingKey(); // RabbitMQ Topic과 동일함, Cvedia Instance 이름과 동일하게 설정
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        List<Object> eventList = this.mapToDto(msg);

        if (eventList == null || eventList.size() == 0) {
            log.warn("[RabbitMQ] - Consumed Invalid Event : {}", msg);
            return;
        }

        /**
         * TODO 1: Event Data 변환 & API 전송 & H2 DB 저장
         *     TODO 1-1: Event의 TimeStamp를 Asia/Seoul이 아닌 UTC로 변환
         *     TODO 1-2: WireClass의 종류, Wire 이름 구하기
         *     TODO 1-3: wireName에 맞는 Count 객체를 가져와 Count값을 증가시키기 위함
         *     TODO 1-4: API를 요청할 때 RoutingKey 뒤에 각각 다른 문자 할당 + Count 수치 증가
         * TODO 2: 받은 Event를 용도에 맞는 DTO로 매핑
         * TODO 3: 시간을 iso8601 형식의 UTC로 변환 - 반환값 형식 : yyyy-mm-ddTHH:mm:ssZ
         * TODO 4: WireClass에 따라 Routing Key에 다른 이니셜 붙임
         * TODO 5: Cvedia에서 나온 수치들을 Request API를 위한 메세지에 매핑
         * TODO 6: 이벤트 메시지 변환이 끝나고 마지막 API 요청으로 보낼 Body
         */

        for (Object receivedEvent : eventList) {
            if (receivedEvent instanceof TripwireDto event) {
                String inOut = "Counter_01";

                // TODO 1-1: Event의 TimeStamp를 Asia/Seoul이 아닌 UTC로 변환
                String eventTime = convertEventTime(event.getSystem_timestamp()); // EventTime -> UTC Time

                // TODO 1-2: WireClass의 종류, Wire 이름 구하기
                String objectClass = event.getEvents().get(0).getExtra().getWireClass(); // Person, Vehicle(Bike, Car)
                String lineName = event.getEvents().get(0).getExtra().getTripwire().getName(); // LineName = Cam01-A01, Cam01-A02  |  Cam02-V01, Cam02-V02
                String lineLabel = lineName.substring(6, 7); // P or V or B or A
                String newCameraNameForSaveH2DB = ""; // H2 DB & 뷰어에 저장될 새로운 카메라 이름

                // TODO 1-3: wireName에 맞는 Count 객체를 가져와 Count값을 증가시키기 위함
                Count count = null;
                int i = Integer.parseInt(lineName.substring(7));
                String num = "";

                // TODO 1-4: API를 요청할 때 RoutingKey 뒤에 각각 다른 문자 할당 + Count 수치 증가
                //  1번 조건문 : Bike + Person (A) Line에 "Person"이 카운팅 됐을 경우
                //  2번 조건문 : Bike + Person (A) Line에 "Bike"가 카운팅 됐을 경우
                //  3번 조건문 : Car Line에 "Car"가 카운팅 됐을 경우
                //  4번 조건문 : "Person"만 카운팅하는 카메라의 경우
                // ex) name|Counter_01/count|0/event|2023-11-30T11:11:11Z/CAMERA ID|Cam01-P01
                if (lineLabel.equals("A") && objectClass.equals("Person")) {
                    if (i < 10) {
                        num = "0" + i;
                        newCameraNameForSaveH2DB = routingKey + num;
                    } else {
                        newCameraNameForSaveH2DB = routingKey + i;
                    }
                    count = countService.getOne(newCameraNameForSaveH2DB);
                    count.setCount(count.getCount() + 1);

                    // ex) name|Counter_01/count|0/event|2023-11-30T11:11:11Z/CAMERA ID|Cam01-B01 //
                } else if (lineLabel.equals("A") && objectClass.equals("Vehicle")) {
                    if (i < 10) {
                        num = "0" + (i + 1);
                        newCameraNameForSaveH2DB = routingKey + num;
                    } else {
                        newCameraNameForSaveH2DB = routingKey + (i + 1);
                    }
                    count = countService.getOne(newCameraNameForSaveH2DB);
                    count.setCount(count.getCount() + 1);
                }

                // ex) name|Counter_01/count|1/event|2023-11-30T11:11:11Z/CAMERA ID|Cam01-V01
                else if (lineLabel.equals("V") && objectClass.equals("Vehicle")) {
                    if (i < 10) {
                        num = "0" + i;
                        newCameraNameForSaveH2DB = routingKey + num;
                    } else {
                        newCameraNameForSaveH2DB = routingKey + i;
                    }
                    count = countService.getOne(newCameraNameForSaveH2DB);
                    count.setCount(count.getCount() + 1);
                }

                // ex) name|Counter_01/count|0/event|2023-11-30T11:11:11Z/CAMERA ID|Cam01-P01
                else if (lineLabel.equals("P") && objectClass.equals("Person")) {
                    if (i < 10) {
                        num = "0" + i;
                        newCameraNameForSaveH2DB = routingKey + num;
                    } else {
                        newCameraNameForSaveH2DB = routingKey + i;
                    }
                    count = countService.getOne(newCameraNameForSaveH2DB);
                    count.setCount(count.getCount() + 1);
                }

                countService.updateCount(count);
//                requestResultToApi(inOut, count.getCount(), eventTime, newCameraNameForSaveH2DB);
                log.info("\uD83D\uDE2F\uD83D\uDE2F\uD83D\uDE2F\uD83D\uDE2F\uD83D\uDE2F : {}", newCameraNameForSaveH2DB);
            }
        }
    }

    /* -------------------- Util -------------------- */
    // TODO 2: 받은 Event를 용도에 맞는 DTO로 매핑
    private List<Object> mapToDto(final String msg) {
        List<Object> objList = new ArrayList<>();
        Object msgObject = null;

        try {
            msgObject = jsonParser.mapJson(msg);
            objList.add(msgObject);
        } catch (Exception e) {
            log.error("[RabbitMQ Delivery] DTO Mapping 실패 - {}", e.getMessage());
        }

        return objList;
    }

    // TODO 3: 시간 반환값 형식 : yyyy-mm-ddTHH:mm:ssZ
    private String convertEventTime(long time) {
        return Instant.ofEpochSecond(time).atZone(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    }

    // TODO 4: WireClass에 따라 Routing Key에 다른 이니셜 붙임
//    private void requestResultToApi(String inOut, int count, String eventTime, String cameraName) {
//        String convertedMessage = convertResult(inOut, count, eventTime, cameraName);
//        String result = getBodyResult(convertedMessage);
//
//        restApiService.request(result);
//        log.info("[{}] 데이터 전송 - 방항: {}, 시간: {}", cameraName, IN, eventTime);
//    }

    // TODO 5: Cvedia에서 나온 수치들을 Request API를 위한 메세지에 매핑
    //  ex) name|Counter_01/count|0/event|2023-11-30T11:11:11Z/CAMERA ID|Cam01-B
//    private String convertResult(String inOut,
//                                 int count,
//                                 String eventTime,
//                                 String cameraName) {
//        return "name|" + inOut + "/" + "count|" + count + "/event|" + eventTime + "/" + "CAMERA ID|" + cameraName;
//    }

    // TODO 6: 이벤트 메시지 변환이 끝나고 마지막 Async API 요청으로 보낼 Body
//    private String getBodyResult(String convertedMessage) {
//        return "--------------------------fc94942040fa9be1\n" +
//                "Content-Disposition: form-data; name=\"eventinfo\"\n" +
//                "Content-Type: text/plain\n\n" +
//                convertedMessage + "\n" +
//                "--------------------------fc94942040fa9be1--";
//    }
}