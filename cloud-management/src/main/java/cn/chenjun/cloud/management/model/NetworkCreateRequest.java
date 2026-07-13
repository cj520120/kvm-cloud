package cn.chenjun.cloud.management.model;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.servcie.bean.SubnetNetwork;
import cn.chenjun.cloud.management.util.IpValidator;
import cn.chenjun.cloud.management.util.SubnetCalculator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkCreateRequest {
    private String name;
    private String startIp;
    private String endIp;
    private String gateway;
    private String broadcast;
    private String bridge;
    private String mask;
    private String subnet;
    private String dns;
    private String domain;
    private int type;
    private int bridgeType;
    private int vlanId;
    private int basicNetworkId;

    public void validate() {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入网络名称");
        }
        if (StringUtils.isEmpty(domain)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入搜索域");
        }
        if (StringUtils.isEmpty(dns)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入DNS信息");
        }
        switch (type) {
            case Constant.NetworkType.FLAT:
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
                break;
            case Constant.NetworkType.VLAN:
                if (vlanId <= 0) {
                    throw new CodeException(ErrorCode.PARAM_ERROR, "请输入VLan ID");
                }
                break;
            case Constant.NetworkType.VxLAN:
                break;
            default:
                throw new CodeException(ErrorCode.PARAM_ERROR, "不支持的网络类型");
        }
    }

    public SubnetNetwork buildSubnetNetwork() {
        switch (type) {
            case Constant.NetworkType.FLAT:
                return SubnetNetwork.builder()
                        .firstIp(startIp)
                        .lastIp(endIp)
                        .subnet(subnet)
                        .broadcast(broadcast)
                        .gateway(gateway)
                        .mask(mask)
                        .build();
            case Constant.NetworkType.VLAN:
            case Constant.NetworkType.VxLAN:
                return SubnetCalculator.calculate(subnet, mask);
            default:
                return null;
        }
    }
}