package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.BasicBridgeNetwork;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VlanNetwork;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.GsonBuilderUtil;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.data.mapper.NetworkMapper;
import cn.roamblue.cloud.management.v2.operate.OperateFactory;
import cn.roamblue.cloud.management.v2.operate.bean.DestroyNetworkOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * 销毁网络
 *
 * @author chenjun
 */
public class DestroyNetworkOperateImpl extends AbstractOperate<DestroyNetworkOperate, ResultUtil<Void>> {

    public DestroyNetworkOperateImpl() {
        super(DestroyNetworkOperate.class);
    }

    @Override
    public void operate(DestroyNetworkOperate param) {
        NetworkMapper networkMapper = SpringContextUtils.getBean(NetworkMapper.class);
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        NetworkEntity network = networkMapper.selectById(param.getId());
        if (network.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.NetworkStatus.DESTROY) {
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", network.getClusterId()));
            for (HostEntity host : hosts) {
                if (Objects.equals(cn.roamblue.cloud.management.v2.util.Constant.HostStatus.ONLINE, host.getStatus())) {
                    if (Objects.equals(cn.roamblue.cloud.management.v2.util.Constant.NetworkType.BASIC, network.getType())) {
                        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                                .bridge(network.getBridge())
                                .ip(host.getIp())
                                .geteway(network.getGateway())
                                .nic(host.getNic())
                                .netmask(network.getMask()).build();
                        this.call(host, param, Constant.Command.NETWORK_DESTROY_BASIC, basicBridgeNetwork);
                    } else {

                        NetworkEntity basicNetworkEntity = networkMapper.selectById(network.getParentId());
                        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                                .bridge(basicNetworkEntity.getBridge())
                                .ip(host.getIp())
                                .geteway(basicNetworkEntity.getGateway())
                                .nic(host.getNic())
                                .netmask(basicNetworkEntity.getMask()).build();
                        VlanNetwork vlan = VlanNetwork.builder()
                                .vlanId(network.getVlanId())
                                .netmask(network.getMask())
                                .basic(basicBridgeNetwork)
                                .ip(null)
                                .bridge(network.getBridge())
                                .geteway(network.getGateway())
                                .build();
                        this.call(host, param, Constant.Command.NETWORK_DESTROY_VLAN, vlan);
                    }
                }
            }
            OperateFactory.onCallback(param, "", GsonBuilderUtil.create().toJson(ResultUtil.builder().build()));
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "网络[" + network.getName() + "]状态不正确:" + network.getStatus());
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, DestroyNetworkOperate param, ResultUtil<Void> resultUtil) {

        NetworkMapper networkMapper = SpringContextUtils.getBean(NetworkMapper.class);
        NetworkEntity network = networkMapper.selectById(param.getId());
        if (network.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.NetworkStatus.DESTROY) {
            networkMapper.deleteById(param.getId());
        }
    }
}
