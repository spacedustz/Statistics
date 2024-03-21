package statistics.dto.stats;

import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@ToString
public class Stats15SecDto {
    private String baseTime15Sec;           // 15초 통계 날짜 기준 데이터
    private String baseTime30Sec;           // 30초 통계 날짜 기준 데이터
    private String baseTime45Sec;           // 45초 통계 날짜 기준 데이터
    private String baseTime00Sec;           // 00초 통계 날짜 기준 데이터

    private int sum15;                      // 15 초 합계
    private int sum30;                      // 30 초 합계
    private int sum45;                      // 45 초 합계
    private int sum00;                      // 00 초 합계

    private int max15;                      // 15 초 max 값
    private int max30;                      // 30 초 max 값
    private int max45;                      // 45 초 max 값
    private int max00;                      // 00 초 max 값

    private int min15;                      // 15 초 min 값
    private int min30;                      // 30 초 min 값
    private int min45;                      // 45 초 min 값
    private int min00;                      // 00 초 min 값

    private int count15;                    // 15 초 count
    private int count30;                    // 30 초 count
    private int count45;                    // 45 초 count
    private int count00;                    // 00 초 count

    Set<String> hashKeysToDelete;

    public Stats15SecDto() {
        this.hashKeysToDelete = new HashSet<>();
    }

    public void addEntry(Map.Entry<String, Integer> entry) {
        String eventTime = entry.getKey();
        int eventSec = Integer.parseInt(eventTime.substring(12,14));

        // 통계 기준 시간 계산
        StatsBaseTimeResDto resDto = initStatsBaseTime(eventTime, eventSec);

        // 사람 수 계산
        calcValueAndIncreaseCount(entry.getValue(), resDto);

        // 통계 계산이 끝난 Redis Hash를 제거할 리스트에 추가
        this.hashKeysToDelete.add(entry.getKey());

    }

    /**
     * 이벤트 데이터의 시간을 받아 각 초 범위에 대한 기준 시간을 가지고 있는 DTO 반환
     * @param eventTime
     * @param eventSec
     * @return StatsBaseTimeResDto
     */
    private StatsBaseTimeResDto initStatsBaseTime(String eventTime, int eventSec) {
        StatsBaseTimeResDto resDto = new StatsBaseTimeResDto();
        String fixSec = "";
        boolean isFirst = false;

        /**
         * 46 ~ 59 초까지 1분 전 데이터를 가지고 있으니 분에 대한 데이터 보정 처리 필요
         * ex) 093946, 094000 -> 46초와 00초의 "분"이 다릐기 때문에 0초 분 기준으로 함
         */
        if (eventSec >= 1 && eventSec < 16) { // 1 ~ 16 초
            fixSec = "15";
            if (!StringUtils.hasLength(this.baseTime15Sec)) {
                this.baseTime15Sec = eventTime.substring(0, 12).concat(fixSec);
                isFirst = true;
            }
        }
        else if (eventSec >= 16 && eventSec < 31) { // 16 ~ 30초
            fixSec = "30";
            if (!StringUtils.hasLength(this.baseTime30Sec)) {
                this.baseTime30Sec = eventTime.substring(0, 12).concat(fixSec);
                isFirst = true;
            }
        }
        else if (eventSec >= 31 && eventSec < 46) { // 31 ~ 45초
            fixSec = "45";
            if (!StringUtils.hasLength(this.baseTime45Sec)) {
                this.baseTime45Sec = eventTime.substring(0, 12).concat(fixSec);
                isFirst = true;
            }
        }
        else if ((eventSec >= 46 && eventSec <= 59) || eventSec == 0) {
            fixSec = "00";
            if (eventSec != 0) {
                if (!StringUtils.hasLength(this.baseTime00Sec)) {
                    String min = eventTime.substring(10,12);
                    int minInt = Integer.parseInt(min);
                    minInt++;
                    min = (minInt > 10) ? String.valueOf(minInt) : "0".concat(String.valueOf(minInt));

                    this.baseTime00Sec = eventTime.substring(0,10).concat(min).concat(fixSec);
                    isFirst = true;
                }
                else {
                    if (!StringUtils.hasLength(this.baseTime00Sec)) {
                        this.baseTime00Sec = eventTime.substring(0,12).concat(fixSec);
                        isFirst = true;
                    }
                }
            }
        }

        resDto.setFirst(isFirst);
        resDto.setFixSec(fixSec);
        return resDto;
    }

    /**
     * 값을 계산해 Count 값을 증가시키는 함수
     * @param value
     * @param resDto
     */
    private void calcValueAndIncreaseCount(Integer value, StatsBaseTimeResDto resDto) {
        String fixSec = resDto.getFixSec();
        boolean isFirst = resDto.isFirst();

        if ("00".equals(fixSec)) {
            if (isFirst) initData(fixSec, value); else calcData(fixSec, value);
            this.count00++;
        }
        else if ("15".equals(fixSec)) {
            if (isFirst) initData(fixSec, value); else calcData(fixSec, value);
            this.count15++;
        }
        else if ("30".equals(fixSec)) {
            if (isFirst) initData(fixSec, value); else calcData(fixSec, value);
            this.count30++;
        }
        else if ("00".equals(fixSec)) {
            if (isFirst) initData(fixSec, value); else calcData(fixSec, value);
            this.count45++;
        }
    }

    /**
     * 통계 데이터 초기값 설정
     * @param fixSec
     * @param value
     */
    private void initData(String fixSec, int value) {
        if ("00".equals(fixSec)) {
            this.sum00 = value;
            this.max00 = value;
            this.min00 = value;
        }
        else if ("15".equals(fixSec)) {
            this.sum15 = value;
            this.max15 = value;
            this.min15 = value;
        }
        else if ("30".equals(fixSec)) {
            this.sum30 = value;
            this.max30 = value;
            this.min30 = value;
        }
        else if ("45".equals(fixSec)) {
            this.sum45 = value;
            this.max45 = value;
            this.min45 = value;
        }
    }

    private void calcData(String fixSec, int value) {
        if("00".equals(fixSec)){
            this.sum00 += value;
            this.max00 = Math.max(max00, value);
            this.min00 = Math.min(min00, value);
        }else if("15".equals(fixSec)){
            this.sum15 += value;
            this.max15 = Math.max(max15, value);
            this.min15 = Math.min(min15, value);
        }else if("30".equals(fixSec)){
            this.sum30 += value;
            this.max30 = Math.max(max30, value);
            this.min30 = Math.min(min30, value);
        }else if("45".equals(fixSec)) {
            this.sum45 += value;
            this.max45 = Math.max(max45, value);
            this.min45 = Math.min(min45, value);
        }
    }
}
