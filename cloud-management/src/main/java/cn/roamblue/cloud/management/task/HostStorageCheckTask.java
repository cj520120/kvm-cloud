package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.common.util.StorageType;
import cn.roamblue.cloud.management.data.entity.ClusterEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.mapper.ClusterMapper;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.service.AgentService;
import cn.roamblue.cloud.management.service.HostService;
import cn.roamblue.cloud.management.service.LockService;
import cn.roamblue.cloud.management.service.StorageService;
import cn.roamblue.cloud.management.util.ClusterStatus;
import cn.roamblue.cloud.management.util.HostStatus;
import cn.roamblue.cloud.management.util.StorageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 更新存储池占用情况
 *
 * @author chenjun
 */
@Slf4j
@Component
public class HostStorageCheckTask extends AbstractTask {

    @Autowired
    private HostService hostService;

    @Autowired
    private StorageService storagePoolService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private VolumeMapper volumeMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private ClusterMapper clusterMapper;
    @Autowired
    private LockService lockService;

    @Override
    protected int getInterval() {
        return 60000;
    }

    @Override
    protected String getName() {
        return "HostStorageCheckTask";
    }


    @Override
    protected void call() {
        List<ClusterEntity> clusterList = clusterMapper.selectAll().stream().filter(t -> t.getClusterStatus().equals(ClusterStatus.READY)).collect(Collectors.toList());
        for (ClusterEntity clusterEntity : clusterList) {
            List<HostEntity> hostList = hostMapper.selectAll().stream().filter(t -> t.getHostStatus().equals(HostStatus.READY)).collect(Collectors.toList());
            List<StorageEntity> storageList = storageMapper.findByClusterId(clusterEntity.getId()).stream().filter(t -> t.getStorageStatus().equals(StorageStatus.READY)).collect(Collectors.toList());
            for (HostEntity hostEntity : hostList) {
                ResultUtil<List<StorageModel>> resultUtil = agentService.getHostStorage(hostEntity.getHostUri());
                if (resultUtil.getCode() != ErrorCode.SUCCESS) {
                    continue;
                }
                List<StorageModel> list = resultUtil.getData();
                Map<String, StorageModel> map = list.stream().collect(Collectors.toMap(StorageModel::getName, Function.identity()));
                for (StorageEntity storageEntity : storageList) {
                    StorageModel cloudStorageInfo = map.get(storageEntity.getStorageTarget());
                    if (cloudStorageInfo == null) {
                        log.info("storage pool is missing. begin reinitialize.host={} storage={}", hostEntity.getHostName(), storageEntity.getStorageName());
                        ResultUtil<StorageModel> initStorageInfoResultUtil = agentService.addHostStorage(StorageType.NFS,hostEntity.getHostUri(), storageEntity.getStorageHost(), storageEntity.getStorageSource(), storageEntity.getStorageTarget());
                        if (initStorageInfoResultUtil.getCode() != ErrorCode.SUCCESS) {
                            log.info("storage pool is missing. reinitialization failed.host={} storage={} msg={}", hostEntity.getHostName(), storageEntity.getStorageName(), initStorageInfoResultUtil.getMessage());
                        } else {
                            cloudStorageInfo = initStorageInfoResultUtil.getData();
                        }
                    }
                    if (cloudStorageInfo != null) {
                        storageEntity.setStorageAvailable(cloudStorageInfo.getAvailable());
                        storageEntity.setStorageAllocation(cloudStorageInfo.getAllocation());
                        storageEntity.setStorageCapacity(cloudStorageInfo.getCapacity());
                    }
                }
            }
            for (StorageEntity storage : storageList) {
                StorageEntity update = StorageEntity.builder()
                        .id(storage.getId())
                        .storageAllocation(storage.getStorageAllocation())
                        .storageAvailable(storage.getStorageAvailable())
                        .storageCapacity(storage.getStorageCapacity())
                        .build();
                storageMapper.updateById(update);
            }
        }

    }
}
