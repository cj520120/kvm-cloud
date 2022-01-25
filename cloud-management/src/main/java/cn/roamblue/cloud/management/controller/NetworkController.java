package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.NetworkInfo;
import cn.roamblue.cloud.management.bean.VmNetworkInfo;
import cn.roamblue.cloud.management.ui.NetworkUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 网络管理
 *
 * @author chenjun
 */
@RestController
public class NetworkController {
    @Autowired
    private NetworkUiService networkUiService;

    /**
     * 获取网络列表
     *
     * @return
     */
    @Login
    @GetMapping("/management/network")
    public ResultUtil<List<NetworkInfo>> listNetworks() {
        return networkUiService.listNetworks();
    }

    /**
     * 根据集群获取网络列表
     *
     * @param clusterId
     * @return
     */
    @Login
    @GetMapping("/management/network/search")
    public ResultUtil<List<NetworkInfo>> search(@RequestParam("clusterId") int clusterId) {
        return networkUiService.search(clusterId);
    }

    /**
     * 获取实例网络信息
     *
     * @param vmId
     * @return
     */
    @Login
    @GetMapping("/management/network/vm")
    public ResultUtil<List<VmNetworkInfo>> findInstanceNetworkByVmId(int vmId) {
        return networkUiService.findInstanceNetworkByVmId(vmId);
    }

    /**
     * 获取网络信息
     *
     * @param id
     * @return
     */
    @Login
    @GetMapping("/management/network/info")

    public ResultUtil<NetworkInfo> findNetworkById(@RequestParam("id") int id) {
        return networkUiService.findNetworkById(id);
    }

    /**
     * 创建网络
     *
     * @param name           网络名称
     * @param clusterId      集群ID
     * @param guestStartIp   Guest分配开始IP
     * @param guestEndIp     Guest分配结束IP
     * @param managerStartIp 管理分配开始IP
     * @param managerEndIp   管理分配结束IP
     * @param subnet         subnet 192.168.2.0/24
     * @param gateway        网关 192.168.2.1
     * @param dns            dns信息 192.168.2.1,192.168.1.2,8.8.8.8
     * @param card           网卡名称 br0
     * @param type           网络类型 Bridge
     * @return
     */
    @Login
    @PostMapping("/management/network/create")
    public ResultUtil<NetworkInfo> createNetwork(@RequestParam("name") String name,
                                                 @RequestParam("clusterId") int clusterId,
                                                 @RequestParam("guestStartIp") String guestStartIp,
                                                 @RequestParam("guestEndIp") String guestEndIp,
                                                 @RequestParam("managerStartIp") String managerStartIp,
                                                 @RequestParam("managerEndIp") String managerEndIp,
                                                 @RequestParam("subnet") String subnet,
                                                 @RequestParam("gateway") String gateway,
                                                 @RequestParam("dns") String dns,
                                                 @RequestParam("card") String card,
                                                 @RequestParam("type") String type) {


        return networkUiService.createNetwork(name, clusterId, managerStartIp, managerEndIp, guestStartIp, guestEndIp, subnet, gateway, dns, card, type);
    }

    /**
     * 销毁网络
     *
     * @param id
     * @return
     */
    @Login
    @PostMapping("/management/network/destroy")
    public ResultUtil<Void> destroyNetwork(@RequestParam("id") int id) {
        return networkUiService.destroyNetworkById(id);

    }

    /**
     * 启用网络
     *
     * @param id
     * @return
     */
    @Login
    @PostMapping("/management/network/start")
    public ResultUtil<NetworkInfo> startNetwork(@RequestParam("id") int id) {
        return networkUiService.startNetwork(id);

    }

    /**
     * 暂停网络
     *
     * @param id
     * @return
     */
    @Login
    @PostMapping("/management/network/pause")
    public ResultUtil<NetworkInfo> pauseNetwork(@RequestParam("id") int id) {
        return networkUiService.pauseNetwork(id);

    }

}
