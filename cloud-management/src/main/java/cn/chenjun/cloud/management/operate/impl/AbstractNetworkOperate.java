package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;

/**
 * @author chenjun
 */
public abstract class AbstractNetworkOperate<T extends BaseOperateParam, V extends ResultUtil> extends AbstractOperate<T, V> {
    protected VlanNetwork buildVlanRequest(NetworkEntity basicNetworkEntity, HostEntity host, NetworkEntity network) {

        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                .bridge(basicNetworkEntity.getBridge())
                .ip(host.getHostIp())
                .geteway(basicNetworkEntity.getGateway())
                .nic(host.getNic())
                .netmask(basicNetworkEntity.getMask()).build();
        return VlanNetwork.builder()
                .vlanId(network.getVlanId())
                .netmask(network.getMask())
                .basic(basicBridgeNetwork)
                .ip(null)
                .bridge(network.getBridge())
                .geteway(network.getGateway())
                .build();
    }
}
