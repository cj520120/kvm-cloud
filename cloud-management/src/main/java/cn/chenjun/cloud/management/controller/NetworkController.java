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
import cn.chenjun.cloud.management.model.NetworkCreateRequest;
import cn.chenjun.cloud.management.model.NetworkDestroyRequest;
import cn.chenjun.cloud.management.model.NetworkMaintenanceRequest;
import cn.chenjun.cloud.management.model.NetworkModel;
import cn.chenjun.cloud.management.model.NetworkNicAllocateRequest;
import cn.chenjun.cloud.management.model.NetworkNicReleaseRequest;
import cn.chenjun.cloud.management.model.NetworkRegisterRequest;
import cn.chenjun.cloud.management.model.NicMode;
import cn.chenjun.cloud.management.model.SimpleNetworkModel;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.servcie.bean.SubnetNetwork;
import cn.chenjun.cloud.management.util.IpValidator;
import cn.chenjun.cloud.management.util.SubnetCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@LoginRequire
@RestController
public class NetworkController extends BaseController {
    @Autowired
    private NetworkService networkService;

    private static void verifyBaseNetwork(String startIp, String endIp, String bridge, String subnet, String broadcast, String gateway) {
        if (!IpValidator.isValidIp(startIp) || !IpValidator.isValidIp(endIp)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的开始IP和结束IP");
        }
        if (StringUtils.isEmpty(bridge)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入桥接网卡名称");
        }
        if (!IpValidator.isValidIp(subnet)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的子网信息");
        }
        if (!IpValidator.isValidIp(broadcast)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的广播地址");
        }
        if (!IpValidator.isValidIp(gateway)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的网关地址");
        }
    }
    @GetMapping("/api/network/info")
    public ResultUtil<NetworkModel> getNetworkInfo(@RequestParam("networkId") int networkId) {
        NetworkEntity network = networkService.getNetworkInfo(networkId);
        return ResultUtil.success(this.convertService.initNetworkModel(network));

    }

    @GetMapping("/api/network/search")
    public ResultUtil<Page<SimpleNetworkModel>> search(@RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "type",required = false) String type,
                                                       @RequestParam(value = "status",required = false) String status,
                                                       @RequestParam(value = "bridgeType",required = false) String bridgeType,
                                                       @RequestParam("no") int no,
                                                       @RequestParam("size") int size) {

        Page<NetworkEntity> page = networkService.search(type,status,bridgeType,keyword, no, size);
        Page<SimpleNetworkModel> pageModel = Page.convert(page, source -> BeanConverter.convert(source, SimpleNetworkModel.class));
        return ResultUtil.success(pageModel);
    }

    @GetMapping("/api/network/all")
    public ResultUtil<List<SimpleNetworkModel>> listNetwork() {
        List<NetworkEntity> networks = networkService.listNetwork();
        List<SimpleNetworkModel> models = BeanConverter.convert(networks, SimpleNetworkModel.class);
        return ResultUtil.success(models);
    }

    @GetMapping("/api/network/ovn/supported")
    public ResultUtil<Boolean> checkOvnSupport() {
        return ResultUtil.success(networkService.checkOvnSupport());
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/network/create")
    public ResultUtil<NetworkModel> createNetwork(@RequestBody NetworkCreateRequest request) {
        request.validate();
        SubnetNetwork subnetNetwork = null;
        switch (request.getType()) {
            case Constant.NetworkType.FLAT:
                verifyBaseNetwork(request.getStartIp(), request.getEndIp(), request.getBridge(), request.getSubnet(), request.getBroadcast(), request.getGateway());
                subnetNetwork = SubnetNetwork.builder()
                        .firstIp(request.getStartIp())
                        .lastIp(request.getEndIp())
                        .subnet(request.getSubnet())
                        .broadcast(request.getBroadcast())
                        .gateway(request.getGateway())
                        .mask(request.getMask())
                        .build();
                break;
            case Constant.NetworkType.VLAN:
                if (request.getVlanId() <= 0) {
                    throw new CodeException(ErrorCode.PARAM_ERROR, "请输入VLan ID");
                }
                subnetNetwork = SubnetCalculator.calculate(request.getSubnet(), request.getMask());
                break;
            case Constant.NetworkType.VxLAN:
                subnetNetwork = SubnetCalculator.calculate(request.getSubnet(), request.getMask());
                break;
            default:
                throw new CodeException(ErrorCode.PARAM_ERROR, "不支持的网络类型");
        }
        SubnetNetwork finalSubnetNetwork = subnetNetwork;
        NetworkEntity network = this.globalLockCall(() -> networkService.createNetwork(request.getName(), finalSubnetNetwork, request.getBridge(), request.getDns(), request.getDomain(), request.getType(), request.getVlanId(), request.getBasicNetworkId(), request.getBridgeType()));
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
    public ResultUtil< NicMode> allocateSpecialNetworkNic(@RequestBody NetworkNicAllocateRequest request) {
        request.validate();
        GuestNetworkEntity nic = this.globalLockCall(() -> networkService.allocateSpecialNetworkNic(request.getGuestNetworkId(), request.getAllocateId(), request.getAllocateDescription()));
        return ResultUtil.success(this.convertService.initNicModel(nic));
    }
    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/network/nic/special/release")
    public ResultUtil<NicMode> releaseSpecialNetworkNic(@RequestBody NetworkNicReleaseRequest request) {
        request.validate();
        GuestNetworkEntity nic = this.globalLockCall(() -> networkService.releaseSpecialNetworkNic(request.getGuestNetworkId()));
        return ResultUtil.success(this.convertService.initNicModel(nic));
    }
    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/network/register")
    public ResultUtil<NetworkModel> registerNetwork(@RequestBody NetworkRegisterRequest request) {
        request.validate();
        NetworkEntity network = this.globalLockCall(() -> networkService.registerNetwork(request.getNetworkId()));
        return ResultUtil.success(this.convertService.initNetworkModel(network));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/network/maintenance")
    public ResultUtil<NetworkModel> maintenanceNetwork(@RequestBody NetworkMaintenanceRequest request) {
        request.validate();
        NetworkEntity network = this.globalLockCall(() -> networkService.maintenanceNetwork(request.getNetworkId()));
        return ResultUtil.success(this.convertService.initNetworkModel(network));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/network/destroy")
    public ResultUtil<NetworkModel> destroyNetwork(@RequestBody NetworkDestroyRequest request) {
        request.validate();
        NetworkEntity network = this.globalLockCall(() -> networkService.destroyNetwork(request.getNetworkId()));
        return ResultUtil.success(this.convertService.initNetworkModel(network));
    }
}
