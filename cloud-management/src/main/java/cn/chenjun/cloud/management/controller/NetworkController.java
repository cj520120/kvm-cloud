package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.BeanConverter;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.model.NetworkModel;
import cn.chenjun.cloud.management.model.NicMode;
import cn.chenjun.cloud.management.model.SimpleNetworkModel;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.util.IpValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class NetworkController extends BaseController {
    @Autowired
    private NetworkService networkService;

    @GetMapping("/api/network/info")
    public ResultUtil<NetworkModel> getNetworkInfo(@RequestParam("networkId") int networkId) {
        NetworkEntity network = networkService.getNetworkInfo(networkId);
        return ResultUtil.success(this.convertService.initNetworkModel(network));

    }

    @GetMapping("/api/network/search")
    public ResultUtil<Page<SimpleNetworkModel>> search(@RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam("no") int no,
                                                       @RequestParam("size") int size) {

        Page<NetworkEntity> page = networkService.search(keyword, no, size);
        Page<SimpleNetworkModel> pageModel = Page.convert(page, source -> BeanConverter.convert(source, SimpleNetworkModel.class));
        return ResultUtil.success(pageModel);
    }

    @GetMapping("/api/network/all")
    public ResultUtil<List<SimpleNetworkModel>> listNetwork() {
        List<NetworkEntity> networks = networkService.listNetwork();
        List<SimpleNetworkModel> models = BeanConverter.convert(networks, SimpleNetworkModel.class);
        return ResultUtil.success(models);
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/network/create")
    public ResultUtil<NetworkModel> createNetwork(@RequestParam("name") String name,
                                                  @RequestParam("startIp") String startIp,
                                                  @RequestParam("endIp") String endIp,
                                                  @RequestParam(value = "gateway", defaultValue = "") String gateway,
                                                  @RequestParam("mask") String mask,
                                                  @RequestParam(value = "bridge", defaultValue = "") String bridge,
                                                  @RequestParam("subnet") String subnet,
                                                  @RequestParam("broadcast") String broadcast,
                                                  @RequestParam("dns") String dns,
                                                  @RequestParam("domain") String domain,
                                                  @RequestParam("type") int type,
                                                  @RequestParam("bridgeType") int bridgeType,
                                                  @RequestParam(value = "vlanId", defaultValue = "0") int vlanId,
                                                  @RequestParam(value = "basicNetworkId", defaultValue = "0") int basicNetworkId) {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入网络名称");
        }
        if (!IpValidator.isValidIp(startIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的开始IP");
        }
        if (!IpValidator.isValidIp(endIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的结束IP");
        }
        if (cn.chenjun.cloud.common.util.Constant.NetworkType.BASIC == type && StringUtils.isEmpty(gateway)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的网关地址");
        }
        if (!IpValidator.isValidIp(mask)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的子网掩码");
        }
        if (cn.chenjun.cloud.common.util.Constant.NetworkType.BASIC == type && StringUtils.isEmpty(bridge)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入桥接网卡名称");
        }
        if (StringUtils.isEmpty(dns)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入DNS信息");
        }
        if (!IpValidator.isValidIp(subnet)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的子网信息");
        }
        if (!IpValidator.isValidIp(broadcast)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的广播地址");
        }
        if (StringUtils.isEmpty(domain)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入搜索域");
        }
        if (Objects.equals(cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN, type) && vlanId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入Vlan ID");
        }
        if (Objects.equals(cn.chenjun.cloud.common.util.Constant.NetworkType.VLAN, type)) {
            if (vlanId <= 0) {
                throw new CodeException(ErrorCode.PARAM_ERROR, "请输入Vlan ID");
            }
            if (basicNetworkId <= 0) {
                throw new CodeException(ErrorCode.PARAM_ERROR, "请输入基础网络");
            }
        }
        NetworkEntity network = this.globalLockCall(() -> networkService.createNetwork(name, startIp, endIp, gateway, mask, subnet, broadcast, bridge, dns, domain, type, vlanId, basicNetworkId, bridgeType));
        return ResultUtil.success(this.convertService.initNetworkModel(network));
    }
    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @GetMapping("/api/network/nics")
    public ResultUtil<List<NicMode>> listNetworkNic(@RequestParam("networkId") int networkId) {
        List<GuestNetworkEntity> nics = networkService.listNetworkNic(networkId);
        List<NicMode> modes = nics.stream().map(this.convertService::initNicModel).collect(Collectors.toList());
        return ResultUtil.success(modes);
    }
    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/network/nic/special/allocate")
    public ResultUtil< NicMode> allocateSpecialNetworkNic(@RequestParam("guestNetworkId") int guestNetworkId,
                                                          @RequestParam("allocateId") int allocateId,
                                                          @RequestParam("allocateDescription") String allocateDescription) {
        GuestNetworkEntity nic = this.globalLockCall(() -> networkService.allocateSpecialNetworkNic(guestNetworkId, allocateId, allocateDescription));
        return ResultUtil.success(this.convertService.initNicModel(nic));
    }
    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/network/nic/special/release")
    public ResultUtil<NicMode> releaseSpecialNetworkNic(@RequestParam("guestNetworkId") int guestNetworkId) {
        GuestNetworkEntity nic = this.globalLockCall(() -> networkService.releaseSpecialNetworkNic(guestNetworkId));
        return ResultUtil.success(this.convertService.initNicModel(nic));
    }
    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/network/register")
    public ResultUtil<NetworkModel> registerNetwork(@RequestParam("networkId") int networkId) {
        NetworkEntity network = this.globalLockCall(() -> networkService.registerNetwork(networkId));
        return ResultUtil.success(this.convertService.initNetworkModel(network));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/network/maintenance")
    public ResultUtil<NetworkModel> maintenanceNetwork(@RequestParam("networkId") int networkId) {
        NetworkEntity network = this.globalLockCall(() -> networkService.maintenanceNetwork(networkId));
        return ResultUtil.success(this.convertService.initNetworkModel(network));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/network/destroy")
    public ResultUtil<NetworkModel> destroyNetwork(@RequestParam("networkId") int networkId) {
        NetworkEntity network = this.globalLockCall(() -> networkService.destroyNetwork(networkId));
        return ResultUtil.success(this.convertService.initNetworkModel(network));
    }
}
