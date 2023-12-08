package com.statistics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.statistics.dto.AreaCrowdDto;
import com.statistics.dto.AreaCrowdImageDto;
import com.statistics.dto.EstimationDto;
import com.statistics.dto.TripwireDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author 신건우
 * Json Parsing Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JsonParser {
    private final ObjectMapper mapper;

    public Object mapJson(String data) throws Exception {
        if (data == null || data.isEmpty()) {
            throw new IllegalAccessException("Json is Empty");
        }

        JsonNode rootNode = mapper.readTree(data);
        JsonNode classNode = rootNode.path("events");

        // Event 노드가 존재하면 SecuRT Event
        if (!classNode.isMissingNode()) {
            JsonNode imgNode = classNode.get(0).path("image");
            JsonNode subClassNode = classNode.get(0).path("extra").path("class");

            // Event 노드 중 class가 있으면 SecuRT - Tripwire Event
            if (!subClassNode.isMissingNode()) {
                return this.parseTripwireEvent(data);
            }
            // Event 노드 중 class가 없으면 SecuRT - Area Crowd Event
            else {
                // class가 없는 데이터 중 image 필드가 존재하면 SecuRT - Area Crowd (Image) Event
                if (!imgNode.isMissingNode()) {
                    return this.parseCrowdImageEvent(data);
                } else {
                    return this.parseCrowdEvent(data);
                }
            }
        }
        // Event 노드가 없으면 Estimation Event
        else {
            return this.parseEstimationEvent(data);
        }
    }

    /* ------------------------------ 각 Event별 Parsing ------------------------------ */
    private TripwireDto parseTripwireEvent(final String data) {
        TripwireDto tripwireDto = null;

        try {
            tripwireDto = mapper.readValue(data, TripwireDto.class);
        } catch (JsonMappingException e) {
            log.error("[Json] Tripwire 파싱 실패 with JsonMappingException - {}", e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("[Json] Tripwire 파싱 실패 with JsonProcessingException - {}", e.getMessage());
        }

        return tripwireDto;
    }

    private AreaCrowdDto parseCrowdEvent(final String data) {
        AreaCrowdDto areaCrowdDto = null;

        try {
            areaCrowdDto = mapper.readValue(data, AreaCrowdDto.class);
        } catch (JsonMappingException e) {
            log.error("[Json] AreaCrowd 파싱 실패 with JsonMappingException - {}", e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("[Json] AreaCrowd 파싱 실패 with JsonProcessingException - {}", e.getMessage());
        }

        return areaCrowdDto;
    }

    private AreaCrowdImageDto parseCrowdImageEvent(final String data) {
        AreaCrowdImageDto areaCrowdImageDto = null;

        try {
            areaCrowdImageDto = mapper.readValue(data, AreaCrowdImageDto.class);
        } catch (JsonMappingException e) {
            log.error("[Json] AreaCrowdImage 파싱 실패 with JsonMappingException - {}", e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("[Json] AreaCrowdImage 파싱 실패 with JsonProcessingException - {}", e.getMessage());
        }

        return areaCrowdImageDto;
    }

    private EstimationDto parseEstimationEvent(final String data) {
        EstimationDto estimationDto = null;

        try {
            estimationDto = mapper.readValue(data, EstimationDto.class);
        } catch (JsonMappingException e) {
            log.error("[Json] Estimation 파싱 실패 with JsonMappingException - {}", e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("[Json] Estimation 파싱 실패 with JsonProcessingException - {}", e.getMessage());
        }

        return estimationDto;
    }
}
