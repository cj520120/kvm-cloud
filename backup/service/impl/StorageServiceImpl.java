package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.bean.StorageInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.ClusterEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.service.AgentService;
import cn.roamblue.cloud.management.service.StorageService;
import cn.roamblue.cloud.management.util.BeanConverter;
import cn.roamblue.cloud.management.util.HostStatus;
import cn.roamblue.cloud.management.util.StorageStatus;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class StorageServiceImpl extends AbstractService implements StorageService {

    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private ClusterMapper clusterMapper;
    @Autowired
    private VolumeMapper volumeMapper;
    @Autowired
    private TemplateRefMapper templateRefMapper;
    @Autowired
    private AgentService agentService;

    @Override
    public List<cn.roamblue.cloud.management.bean.StorageInfo> listStorage() {

        List<StorageEntity> storageEntityList = storageMapper.selectAll();
        List<cn.roamblue.cloud.management.bean.StorageInfo> list = BeanConverter.convert(storageEntityList, this::init);
        return list;
    }

    @Override
    public List<cn.roamblue.cloud.management.bean.StorageInfo> search(int clusterId) {

        QueryWrapper<StorageEntity> wrapper = new QueryWrapper<>();
        if (clusterId > 0) {
            wrapper.eq("cluster_id", clusterId);
        }
        List<StorageEntity> storageEntityList = storageMapper.selectList(wrapper);
        List<cn.roamblue.cloud.management.bean.StorageInfo> list = BeanConverter.convert(storageEntityList, this::init);
        return list;
    }


    @Override
    public cn.roamblue.cloud.management.bean.StorageInfo findStorageById(int id) {

        StorageEntity entity = storageMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储不存在");
        }
        cn.roamblue.cloud.management.bean.StorageInfo info = init(entity);
        return info;
    }

    @Override
    public cn.roamblue.cloud.management.bean.StorageInfo createStorage(int clusterId, String name, String uri, String source) {

        ClusterEntity clusterEntity = this.clusterMapper.selectById(clusterId);
        if (clusterEntity == null) {
            throw new CodeException(ErrorCode.CLUSTER_NOT_FOUND, "集群不存在");
        }
        List<HostEntity> hostList = this.hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", clusterId));
        StorageEntity entity = StorageEntity.builder()
                .storageAllocation(0L)
                .storageCapacity(0L)
                .clusterId(clusterId)
                .storageAvailable(0L)
                .storageName(name)
                .storageHost(uri)
                .storageSource(source)
                .storageStatus(StorageStatus.READY)
                .storageTarget(UUID.randomUUID().toString().replace("-", ""))
                .createTime(new Date())
                .build();
        for (HostEntity hostInfo : hostList) {
            if (!hostInfo.getHostStatus().equals(HostStatus.READY)) {
                continue;
            }
            ResultUtil<StorageInfo> addStorageResultUtil = this.agentService.addHostStorage(Constant.StorageType.NFS,hostInfo.getHostUri(), entity.getStorageHost(), entity.getStorageSource(), entity.getStorageTarget());
            if (addStorageResultUtil.getCode() != ErrorCode.SUCCESS) {
                throw new CodeException(addStorageResultUtil.getCode(), addStorageResultUtil.getMessage());
            }
            StorageInfo cloudStorageInfo = addStorageResultUtil.getData();
            entity.setStorageAllocation(cloudStorageInfo.getAllocation());
            entity.setStorageCapacity(cloudStorageInfo.getCapacity());
            entity.setStorageAvailable(cloudStorageInfo.getAvailable());
        }
        storageMapper.insert(entity);
        cn.roamblue.cloud.management.bean.StorageInfo info = init(entity);
        log.info("create storage success.storage={}", info);
        return info;
    }

    @Override
    public void destroyStorageById(int id) {
        StorageEntity entity = storageMapper.selectById(id);
        if (entity == null) {
            return;
        }
        int volumeCount = this.volumeMapper.selectCount(new QueryWrapper<VolumeEntity>().eq("storage_id", id));
        if (volumeCount > 0) {
            throw new CodeException(ErrorCode.HAS_VOLUME_ERROR, "存储包含数据卷");
        }
        if (entity.getStorageStatus().equals(StorageStatus.READY)) {

            List<HostEntity> hostList = this.hostMapper.findByClusterId(entity.getClusterId());
            if (hostList != null) {
                for (HostEntity hostInfo : hostList) {
                    if (!hostInfo.getHostStatus().equals(HostStatus.READY)) {
                        continue;
                    }
                    this.agentService.destroyStorage(hostInfo.getHostUri(), entity.getStorageTarget());
                }
            }
        }
        storageMapper.deleteById(id);
        templateRefMapper.deleteByStorageId(id);
        log.info("destroy storage success.storage={}", entity);

    }


    private cn.roamblue.cloud.management.bean.StorageInfo init(StorageEntity entity) {
        return cn.roamblue.cloud.management.bean.StorageInfo.builder()
                .id(entity.getId())
                .name(entity.getStorageName())
                .clusterId(entity.getClusterId())
                .host(entity.getStorageHost())
                .source(entity.getStorageSource())
                .target(entity.getStorageTarget())
                .capacity(entity.getStorageCapacity())
                .available(entity.getStorageAvailable())
                .allocation(entity.getStorageAllocation())
                .createTime(entity.getCreateTime())
                .status(entity.getStorageStatus())
                .build();
    }
}
