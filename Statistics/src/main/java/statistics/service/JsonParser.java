package statistics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import statistics.dto.SecuRTAreaOccupancyEnterEventImageDto;
import statistics.dto.SecuRTAreaOccupancyExitEventImageDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonParser {
    private final ObjectMapper objectMapper;

    public Object mapJson(String jsonString) throws Exception {
        if (jsonString == null || jsonString.isEmpty()) {
            throw new IllegalArgumentException("JSON is empty");
        }

        JsonNode rootNode = objectMapper.readTree(jsonString);
        JsonNode classNode = rootNode.path("events");

        if (!classNode.isMissingNode()) {
            JsonNode imgNode = classNode.get(0).path("image");
            JsonNode subClassNode = classNode.get(0).path("extra").path("class");

            if (!subClassNode.isMissingNode()) {
                JsonNode bestThumbnailNode = classNode.get(0).path("best_thumbnail");

                if (!bestThumbnailNode.isMissingNode()) {
                    JsonNode exitNode = classNode.get(0).path("extra").path("external_track_id_left");

                    if (!exitNode.isMissingNode()) {
                        return this.parsingAreaOccupancyExitImageEvent(jsonString);
                    } else {
                        return this.parsingAreaOccupancyEnterImageEvent(jsonString);
                    }
                }
            }
        }
        return null;
    }

    /**
     * SecuRT Area Occupancy Enter(Image) 이벤트 파싱
     *
     * @param jsonString
     * @return
     */
    private SecuRTAreaOccupancyEnterEventImageDto parsingAreaOccupancyEnterImageEvent(final String jsonString) {
        SecuRTAreaOccupancyEnterEventImageDto secuRTAreaOccupancyEnterEventImageDto = null;

        try {
            secuRTAreaOccupancyEnterEventImageDto = objectMapper.readValue(jsonString, SecuRTAreaOccupancyEnterEventImageDto.class);
        } catch (JsonProcessingException jsonProcessingException) {
            log.warn("SecuRTAreaOccupancyEnterEventImageDto - ParseException : {}", jsonProcessingException.getMessage());
        } catch (Exception exception) {
            log.warn("SecuRTAreaOccupancyEnterEventImageDto - ParseException : {}", exception.getMessage());
        }

        return secuRTAreaOccupancyEnterEventImageDto;
    }

    /**
     * SecuRT Area Occupancy Exit(Image) 이벤트 파싱
     *
     * @param jsonString
     * @return
     */
    private SecuRTAreaOccupancyExitEventImageDto parsingAreaOccupancyExitImageEvent(final String jsonString) {
        SecuRTAreaOccupancyExitEventImageDto secuRTAreaOccupancyExitEventImageDto = null;

        try {
            secuRTAreaOccupancyExitEventImageDto = objectMapper.readValue(jsonString, SecuRTAreaOccupancyExitEventImageDto.class);
        } catch (JsonProcessingException jsonProcessingException) {
            log.warn("SecuRTAreaOccupancyExitEventImageDto - ParseException : {}", jsonProcessingException.getMessage());
        } catch (Exception exception) {
            log.warn("SecuRTAreaOccupancyExitEventImageDto - ParseException : {}", exception.getMessage());
        }

        return secuRTAreaOccupancyExitEventImageDto;

    }
}
