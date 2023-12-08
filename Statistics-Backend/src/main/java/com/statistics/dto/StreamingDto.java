package com.statistics.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamingDto {
    private Integer cameraId; // DB에 저장된 CameraID

    private String instanceName; // RTSP Topic

    private String ip;

    private Integer port;

    private String command; // start & stop

    private String apiKey; // API 호출을 위한 키
}
