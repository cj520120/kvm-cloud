package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Rule;
import cn.roamblue.cloud.management.bean.ClusterInfo;
import cn.roamblue.cloud.management.service.ClusterService;
import cn.roamblue.cloud.management.ui.ClusterUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author chenjun
 */
@Service
public class ClusterUiServiceImpl extends AbstractUiService implements ClusterUiService {

    @Autowired
    private ClusterService clusterService;

    @Override
    public ResultUtil<List<ClusterInfo>> listCluster() {
        return super.call(clusterService::listCluster);
    }

    @Override
    public ResultUtil<ClusterInfo> findClusterById(int id) {
        return super.call(() -> clusterService.findClusterById(id));
    }

    @Rule(permissions = "cluster.create")
    @Override
    public ResultUtil<ClusterInfo> createCluster(String name, float overCpu, float overMemory) {
        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "集群名称不能为空");
        }
        if (overCpu < 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "CPU超配必须大于0");
        }
        if (overMemory < 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "内存超配必须大于0");
        }
        return super.call(() -> clusterService.createCluster(name, overCpu, overMemory));
    }

    @Rule(permissions = "cluster.modify")
    @Override
    public ResultUtil<ClusterInfo> modifyCluster(int id, String name, float overCpu, float overMemory) {
        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "集群名称不能为空");
        }
        if (overCpu < 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "CPU超配必须大于0");
        }
        if (overMemory < 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "内存超配必须大于0");
        }
        return super.call(() -> clusterService.modifyCluster(id, name, overCpu, overMemory));
    }

    @Rule(permissions = "cluster.destroy")
    @Override
    public ResultUtil<Void> destroyClusterById(int id) {
        return super.call(() -> clusterService.destroyClusterById(id));
    }
}