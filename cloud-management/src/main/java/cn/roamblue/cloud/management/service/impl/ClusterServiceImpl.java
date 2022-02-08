package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.ClusterInfo;
import cn.roamblue.cloud.management.data.entity.ClusterEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.mapper.ClusterMapper;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.NetworkMapper;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.service.ClusterService;
import cn.roamblue.cloud.management.util.BeanConverter;
import cn.roamblue.cloud.management.util.ClusterStatus;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
public class ClusterServiceImpl extends AbstractService implements ClusterService {

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
        log.info("create cluster={}", clusterInfo);
        return clusterInfo;

    }

    @Override
    public ClusterInfo modifyCluster(int id, String name, float overCpu, float overMemory) {

        ClusterEntity entity = clusterMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.CLUSTER_NOT_FOUND,"集群不存在");
        }
        entity.setClusterName(name);
        entity.setOverCpu(overCpu);
        entity.setOverMemory(overMemory);
        clusterMapper.updateById(entity);
        return this.init(entity);

    }

    @Override
    public void destroyClusterById(int id) {

 
        if (hostMapper.selectCount( new QueryWrapper<HostEntity>().eq("cluster_id", id)) > 0) {
            throw new CodeException(ErrorCode.HAS_HOST_ERROR, "删除集群前请先删除主机信息");
        }
        if (networkRepository.selectCount(new QueryWrapper<NetworkEntity>().eq("cluster_id", id)) > 0) {
            throw new CodeException(ErrorCode.HAS_NETWORK_ERROR, "删除集群前请请先删除网络信息");
        }
        if (storageRepository.selectCount(new QueryWrapper<StorageEntity>().eq("cluster_id", id)) > 0) {
            throw new CodeException(ErrorCode.HAS_STORAGE_ERROR, "删除集群前请请先删除存储信息");
        }
        log.info("destroy clusterId={}", id);
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
