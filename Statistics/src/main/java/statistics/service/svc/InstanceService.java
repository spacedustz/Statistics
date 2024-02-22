//package statistics.service.svc;
//
//import com.dains.common.condition.SvcInstanceSearchCondition;
//import com.dains.common.dto.svc.SvcInstanceCameraServerDto;
//import com.dains.common.dto.svc.SvcInstanceZoneCameraAreaDto;
//import com.dains.common.enums.DataStatus;
//import com.dains.common.repository.svc.SvcInstanceRepository;
//import com.dains.common.repository.svc.SvcInstanceZoneRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class InstanceService {
//    private final SvcInstanceRepository svcInstanceRepository;
//    private final SvcInstanceZoneRepository svcInstanceZoneRepository;
//
//    //@Cacheable(cacheNames= CacheConstants.CACHE_INSTANCE_NAME, key = "{#serverId,#shouldSortByCameraIdAsc,#shouldSortByInstanceIdAsc,#shouldSortByServerIdAsc}", unless = "#result == null")
//    public Optional<List<SvcInstanceCameraServerDto>> findSvcInstanceCameraArea(final Integer serverId, final boolean shouldSortByCameraIdAsc, final boolean shouldSortByInstanceIdAsc, final boolean shouldSortByServerIdAsc) {
//        SvcInstanceSearchCondition svcInstanceSearchCondition = SvcInstanceSearchCondition.builder()
//                .serverId(serverId)
//                .cameraDataStatus(DataStatus.ENABLE)
//                .instanceDataStatus(DataStatus.ENABLE)
//                .serverDataStatus(DataStatus.ENABLE)
//                .build();
//
//        if (shouldSortByCameraIdAsc) svcInstanceSearchCondition.setShouldSortByCameraIdAsc(true);
//        if (shouldSortByInstanceIdAsc) svcInstanceSearchCondition.setShouldSortByInstanceIdAsc(true);
//        if (shouldSortByServerIdAsc) svcInstanceSearchCondition.setShouldSortByServerIdAsc(true);
//
//        return svcInstanceRepository.searchInstanceCameraServer(svcInstanceSearchCondition);
//    }
//
//    //@Cacheable(cacheNames= CacheConstants.CACHE_INSTANCE_EXT_NAME, key = "#instanceExtName", unless = "#result == null")
//    public Optional<SvcInstanceZoneCameraAreaDto> findSvcInstanceZoneCameraArea(final String instanceExtName) {
//        SvcInstanceSearchCondition svcInstanceSearchCondition = SvcInstanceSearchCondition.builder()
//                .instanceExtName(instanceExtName)
//                .cameraDataStatus(DataStatus.ENABLE)
//                .instanceDataStatus(DataStatus.ENABLE)
//                .build();
//
//        Optional<List<SvcInstanceZoneCameraAreaDto>> optionalSvcInstanceCameraAreaDtoList = svcInstanceRepository.searchInstanceZoneCameraArea(svcInstanceSearchCondition);
//
//        if (optionalSvcInstanceCameraAreaDtoList.isPresent()) {
//            List<SvcInstanceZoneCameraAreaDto> svcInstanceCameraAreaDtoList = optionalSvcInstanceCameraAreaDtoList.get();
//
//            if (!svcInstanceCameraAreaDtoList.isEmpty()) {
//                return Optional.ofNullable(svcInstanceCameraAreaDtoList.get(0));
//            }
//        }
//
//        return Optional.empty();
//    }
//}
