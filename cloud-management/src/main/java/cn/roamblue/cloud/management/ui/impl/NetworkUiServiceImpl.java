package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Rule;
import cn.roamblue.cloud.management.bean.NetworkInfo;
import cn.roamblue.cloud.management.bean.VmNetworkInfo;
import cn.roamblue.cloud.management.service.NetworkService;
import cn.roamblue.cloud.management.ui.NetworkUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author chenjun
 */
@Service
public class NetworkUiServiceImpl extends AbstractUiService implements NetworkUiService {
    @Autowired
    private NetworkService networkService;

    @Override
    public ResultUtil<List<NetworkInfo>> listNetworks() {
        return super.call(() -> networkService.listNetwork());
    }

    @Override
    public ResultUtil<List<NetworkInfo>> search(int clusterId) {
        return super.call(() -> networkService.search(clusterId));
    }

    @Override
    public ResultUtil<List<VmNetworkInfo>> findInstanceNetworkByVmId(int vmId) {
        return super.call(() -> networkService.findVmNetworkByVmId(vmId));
    }

    @Override
    public ResultUtil<NetworkInfo> findNetworkById(int id) {
        return super.call(() -> networkService.findNetworkById(id));
    }

    @Override
    @Rule(permissions = "network.create")
    public ResultUtil<NetworkInfo> createNetwork(String name, int clusterId, String guestStartIp, String guestEndIp, String managerStartIp, String managerEndIp, String subnet, String gateway, String dns, String card, String type) {


        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "名称不能为空");
        }
        if (StringUtils.isEmpty(guestStartIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "Guest分配开始IP不能为空");
        }
        if (StringUtils.isEmpty(guestEndIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "Guest分配结束IP不能为空");
        }
        if (StringUtils.isEmpty(managerStartIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "管理分配开始IP不能为空");
        }
        if (StringUtils.isEmpty(managerEndIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "管理分配结束IP不能为空");
        }
        if (StringUtils.isEmpty(subnet)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "子网信息不能为空");
        }
        if (StringUtils.isEmpty(gateway)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "网关不能为空");
        }
        if (StringUtils.isEmpty(dns)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "dns信息不能为空");
        }
        if (StringUtils.isEmpty(type)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "网络类型不能为空");
        }
        if (StringUtils.isEmpty(card)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "网卡名称不能为空");
        }
        if (clusterId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "集群不能为空");
        }
        return super.call(() -> networkService.createNetwork(name, clusterId, managerStartIp, managerEndIp, guestStartIp, guestEndIp, subnet, gateway, dns, card, type));

    }

    @Override
    @Rule(permissions = "network.destroy")
    public ResultUtil<Void> destroyNetworkById(int id) {
        return super.call(() -> networkService.destroyNetworkById(id));

    }

    @Override
    @Rule(permissions = "network.status.modify")
    public ResultUtil<NetworkInfo> startNetwork(int id) {
        return super.call(() -> networkService.startNetworkById(id));

    }

    @Override
    @Rule(permissions = "network.status.modify")
    public ResultUtil<NetworkInfo> pauseNetwork(int id) {
        return super.call(() -> networkService.pauseNetworkById(id));
    }

}
