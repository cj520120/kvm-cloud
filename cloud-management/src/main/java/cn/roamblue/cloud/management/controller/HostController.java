package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.HostInfo;
import cn.roamblue.cloud.management.ui.HostUiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 主机管理
 *
 * @author chenjun
 */
@RestController
@Slf4j
public class HostController {
    @Autowired
    private HostUiService hostUiService;

    /**
     * 获取主机列表
     *
     * @return
     */
    @Login
    @GetMapping("/management/host")
    public ResultUtil<List<HostInfo>> listHost() {
        return hostUiService.listHost();
    }

    /**
     * 主机列表
     *
     * @param clusterId
     * @return
     */
    @Login
    @GetMapping("/management/host/search")
    public ResultUtil<List<HostInfo>> search(@RequestParam("clusterId") int clusterId) {
        return hostUiService.search(clusterId);
    }

    /**
     * 查找主机
     *
     * @param id
     * @return
     */
    @Login
    @PostMapping("/management/host/info")
    public ResultUtil<HostInfo> findHostById(@RequestParam("id") int id) {
        return hostUiService.findHostById(id);

    }

    /**
     * 创建主机
     *
     * @param clusterId 集群ID
     * @param name      主机名称
     * @param ip        主机IP
     * @param uri       通信http地址
     * @return
     */
    @Login
    @PostMapping("/management/host/create")
    public ResultUtil<HostInfo> createHost(@RequestParam("clusterId") int clusterId,
                                           @RequestParam("name") String name,
                                           @RequestParam("ip") String ip,
                                           @RequestParam("uri") String uri) {

        return hostUiService.createHost(clusterId, name, ip, uri);

    }

    /**
     * 销毁主机
     *
     * @param id
     * @return
     */
    @Login
    @PostMapping("/management/host/destroy")
    public ResultUtil<Void> destroyHostById(@RequestParam("id") int id) {
        return hostUiService.destroyHostById(id);
    }

}
