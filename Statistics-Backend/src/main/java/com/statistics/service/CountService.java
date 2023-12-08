package com.statistics.service;

import com.statistics.entity.Count;
import com.statistics.repository.CountRepository;
import com.statistics.util.Props;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 신건우
 * Count Service
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CountService {
    private final CountRepository countRepository;
    private final Props props;

    /**
     * Bean 초기화 함수
     */
    @PostConstruct
    public void init() {
        initEntities();
    }

    /**
     * Repository가 비어있으면 application.yml에 있는 wireName을 ID로 Count 객체들 전부 H2 DB에 등록
     */
    public void initEntities() {
        List<Count> counts = new ArrayList<>();

        if (countRepository.count() == 0) {
            for (String instanceName : props.getWireNames()) {

                Count entity = Count.createOf(instanceName, 0);
                counts.add(entity);
            }
        }

        countRepository.saveAll(counts);
        log.info("Count Entity 초기화 완료 - Count Entity 수 : {}", countRepository.count());
    }

    /**
     * Count 개별 조회
     */
    @Transactional(readOnly = true)
    public Count getOne(String id) {
        Count count = null;

        try {
            count = countRepository.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("[Count getOne()] Count 개별 조회 실패 - {}", e.getMessage());
        }

        return count;
    }

    /**
     * Count 전체 조회
     */
    @Cacheable
    @Transactional(readOnly = true)
    public List<Count> getAllCount() {
        List<Count> counts = new ArrayList<>();

        try {
            counts = countRepository.findAll();
        } catch (Exception e) {
            log.error("[Get All Counts] Count 전체 조회 실패 - {}", e.getMessage());
        }

        return counts;
    }

    /**
     * 카운트 값 업데이트
     */
    public void updateCount(Count count) {
        try {
            countRepository.save(count);
        } catch (Exception e) {
            log.error("[Update Count] Count 값 업데이트 에러 - {}", e.getMessage());
        }
    }

    /**
     * 매일 00시 01분마다 DB의 모든 컬럼 Count 값 초기화
     */
//    @Scheduled(cron = "0 0/15 * * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "0 1 * * * * *", zone = "Asia/Seoul")
    public void resetCount() {
        List<Count> counts = getAllCount();

        for (Count count : counts) {
            count.setCount(0);
            countRepository.save(count);
        }
    }
}
