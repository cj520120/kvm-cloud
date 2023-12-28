package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;

/**
 * @author chenjun
 */
public abstract class AbstractNetworkOperate<T extends BaseOperateParam, V extends ResultUtil> extends AbstractOperate<T, V> {
    protected VlanNetwork buildVlanRequest(NetworkEntity basicNetworkEntity, HostEntity host, NetworkEntity network) {

        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                .poolId(basicNetworkEntity.getPoolId())
                .bridge(basicNetworkEntity.getBridge())
                .bridgeType(Constant.NetworkBridgeType.fromBridgeType(basicNetworkEntity.getBridgeType()))
                .build();
        return VlanNetwork.builder()
                .poolId(network.getPoolId())
                .vlanId(network.getVlanId())
                .basic(basicBridgeNetwork)
                .bridge(network.getBridge())
                .build();
    }
}
