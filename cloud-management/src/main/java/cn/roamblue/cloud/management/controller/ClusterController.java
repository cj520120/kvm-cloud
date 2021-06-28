package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.ClusterInfo;
import cn.roamblue.cloud.management.ui.ClusterUiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 集群管理
 *
 * @author chenjun
 */
@RestController
@Slf4j
public class ClusterController {
    @Autowired
    private ClusterUiService clusterUiService;

    /**
     * 获取集群列表
     *
     * @return
     */
    @Login
    @GetMapping("/management/cluster")
    public ResultUtil<List<ClusterInfo>> listCluster() {
        return clusterUiService.listCluster();
    }

    /**
     * 查找集群
     *
     * @param id 集群ID
     * @return
     */
    @Login
    @GetMapping("/management/cluster/info")
    public ResultUtil<ClusterInfo> findClusterById(@RequestParam("id") int id) {
        return clusterUiService.findClusterById(id);
    }

    /**
     * 创建集群
     *
     * @param name       集群名称
     * @param overCpu    cpu比例
     * @param overMemory memory比例
     * @return
     */
    @Login
    @PostMapping("/management/cluster/create")
    public ResultUtil<ClusterInfo> createCluster(@RequestParam("name") String name,
                                                 @RequestParam("overCpu") float overCpu,
                                                 @RequestParam("overMemory") float overMemory) {

        return clusterUiService.createCluster(name, overCpu, overMemory);
    }

    /**
     * 修改集群
     *
     * @param id         id
     * @param name       集群名称
     * @param overCpu    cpu比例
     * @param overMemory memory比例
     * @return
     */
    @Login
    @PostMapping("/management/cluster/modify")
    public ResultUtil<ClusterInfo> modifyCluster(@RequestParam("id") int id,
                                                 @RequestParam("name") String name,
                                                 @RequestParam("overCpu") float overCpu,
                                                 @RequestParam("overMemory") float overMemory) {

        return clusterUiService.modifyCluster(id, name, overCpu, overMemory);
    }

    /**
     * 删除集群
     *
     * @param id 集群ID
     * @return
     */
    @Login
    @PostMapping("/management/cluster/destroy")
    public ResultUtil<Void> destroyClusterById(@RequestParam("id") int id) {
        return clusterUiService.destroyClusterById(id);
    }
}
