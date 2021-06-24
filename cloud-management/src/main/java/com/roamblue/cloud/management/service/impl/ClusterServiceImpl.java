package com.roamblue.cloud.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.bean.ClusterInfo;
import com.roamblue.cloud.management.data.entity.ClusterEntity;
import com.roamblue.cloud.management.data.mapper.ClusterMapper;
import com.roamblue.cloud.management.data.mapper.HostMapper;
import com.roamblue.cloud.management.data.mapper.NetworkMapper;
import com.roamblue.cloud.management.data.mapper.StorageMapper;
import com.roamblue.cloud.management.service.ClusterService;
import com.roamblue.cloud.management.util.BeanConverter;
import com.roamblue.cloud.management.util.ClusterStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    private ClusterMapper clusterMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private NetworkMapper networkRepository;
    @Autowired
    private StorageMapper storageRepository;

    @Override
    public List<ClusterInfo> listCluster() {
        List<ClusterEntity> clusterEntityList = clusterMapper.selectAll();
        List<ClusterInfo> list = BeanConverter.convert(clusterEntityList, this::init);
        return list;
    }


    @Override
    public ClusterInfo findClusterById(int id) {
        ClusterEntity entity = clusterMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.CLUSTER_NOT_FOUND, "集群不存在");
        }
        ClusterInfo clusterInfo = init(entity);
        return clusterInfo;
    }

    @Override
    public ClusterInfo createCluster(String name, float overCpu, float overMemory) {

        ClusterEntity entity = ClusterEntity.builder()
                .clusterName(name)
                .overCpu(overCpu)
                .overMemory(overMemory)
                .createTime(new Date())
                .clusterStatus(ClusterStatus.READY)
                .build();
        clusterMapper.insert(entity);
        ClusterInfo clusterInfo = init(entity);
        log.info("创建集群cluster={}", clusterInfo);
        return clusterInfo;

    }

    @Override
    public ClusterInfo modifyCluster(int id, String name, float overCpu, float overMemory) {

        ClusterEntity entity = clusterMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.CLUSTER_NOT_FOUND, "集群不存在");
        }
        entity.setClusterName(name);
        entity.setOverCpu(overCpu);
        entity.setOverMemory(overMemory);
        clusterMapper.updateById(entity);
        return this.init(entity);

    }

    @Override
    public void destroyClusterById(int id) {


        QueryWrapper wrapper = new QueryWrapper<>().eq("cluster_id", id);
        if (hostMapper.selectCount(wrapper) > 0) {
            throw new CodeException(ErrorCode.HAS_HOST_ERROR, "请先删除主机信息");
        }
        if (networkRepository.selectCount(wrapper) > 0) {
            throw new CodeException(ErrorCode.HAS_NETWORK_ERROR, "请先删除网络信息");
        }
        if (storageRepository.selectCount(wrapper) > 0) {
            throw new CodeException(ErrorCode.HAS_STORAGE_ERROR, "请先删除存储信息");
        }
        log.info("销毁集群clusterId={}", id);
        clusterMapper.deleteById(id);

    }

    private ClusterInfo init(ClusterEntity clusterEntity) {
        return ClusterInfo.builder()
                .id(clusterEntity.getId())
                .name(clusterEntity.getClusterName())
                .overCpu(clusterEntity.getOverCpu())
                .overMemory(clusterEntity.getOverMemory())
                .status(clusterEntity.getClusterStatus())
                .createTime(clusterEntity.getCreateTime())
                .build();

    }
}
