package com.roamblue.cloud.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.roamblue.cloud.common.agent.StorageModel;
import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.bean.StorageInfo;
import com.roamblue.cloud.management.data.entity.ClusterEntity;
import com.roamblue.cloud.management.data.entity.HostEntity;
import com.roamblue.cloud.management.data.entity.StorageEntity;
import com.roamblue.cloud.management.data.entity.VolumeEntity;
import com.roamblue.cloud.management.data.mapper.*;
import com.roamblue.cloud.management.service.AgentService;
import com.roamblue.cloud.management.service.StorageService;
import com.roamblue.cloud.management.util.BeanConverter;
import com.roamblue.cloud.management.util.HostStatus;
import com.roamblue.cloud.management.util.StorageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

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
    public List<StorageInfo> listStorage() {

        List<StorageEntity> storageEntityList = storageMapper.selectAll();
        List<StorageInfo> list = BeanConverter.convert(storageEntityList, this::init);
        return list;
    }

    @Override
    public List<StorageInfo> search(int clusterId) {

        QueryWrapper<StorageEntity> wrapper = new QueryWrapper<>();
        if (clusterId > 0) {
            wrapper.eq("cluster_id", clusterId);
        }
        List<StorageEntity> storageEntityList = storageMapper.selectList(wrapper);
        List<StorageInfo> list = BeanConverter.convert(storageEntityList, this::init);
        return list;
    }


    @Override
    public StorageInfo findStorageById(int id) {

        StorageEntity entity = storageMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储不存在");
        }
        StorageInfo info = init(entity);
        return info;
    }

    @Override
    public StorageInfo createStorage(int clusterId, String name, String uri, String source) {

        ClusterEntity clusterEntity = this.clusterMapper.selectById(clusterId);
        if (clusterEntity == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "集群不存在");
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
            ResultUtil<StorageModel> addStorageResultUtil = this.agentService.addHostStorage(hostInfo.getHostUri(), entity.getStorageHost(), entity.getStorageSource(), entity.getStorageTarget());
            if (addStorageResultUtil.getCode() != ErrorCode.SUCCESS) {
                throw new CodeException(addStorageResultUtil.getCode(), addStorageResultUtil.getMessage());
            }
            StorageModel cloudStorageInfo = addStorageResultUtil.getData();
            entity.setStorageAllocation(cloudStorageInfo.getAllocation());
            entity.setStorageCapacity(cloudStorageInfo.getCapacity());
            entity.setStorageAvailable(cloudStorageInfo.getAvailable());
        }
        storageMapper.insert(entity);
        StorageInfo info = init(entity);
        log.info("创建存储池成功.{}", info);
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
        log.info("删除存储池成功.id={}", id);

    }


    private StorageInfo init(StorageEntity entity) {
        return StorageInfo.builder()
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
