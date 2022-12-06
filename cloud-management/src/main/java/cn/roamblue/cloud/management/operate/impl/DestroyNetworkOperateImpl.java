package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.BasicBridgeNetwork;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VlanNetwork;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.operate.bean.DestroyNetworkOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * 销毁网络
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyNetworkOperateImpl extends AbstractOperate<DestroyNetworkOperate, ResultUtil<Void>> {

    public DestroyNetworkOperateImpl() {
        super(DestroyNetworkOperate.class);
    }

    @Override
    public void operate(DestroyNetworkOperate param) {
        localReportTask.addTaskId(param.getTaskId());
        NetworkEntity network = networkMapper.selectById(param.getNetworkId());
        if (network.getStatus() == cn.roamblue.cloud.management.util.Constant.NetworkStatus.DESTROY) {
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>());
            for (HostEntity host : hosts) {
                if (Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
                    if (Objects.equals(cn.roamblue.cloud.management.util.Constant.NetworkType.BASIC, network.getType())) {
                        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                                .bridge(network.getBridge())
                                .ip(host.getHostIp())
                                .geteway(network.getGateway())
                                .nic(host.getNic())
                                .netmask(network.getMask()).build();
                        this.syncInvoker(host, param, Constant.Command.NETWORK_DESTROY_BASIC, basicBridgeNetwork);
                    } else {

                        NetworkEntity basicNetworkEntity = networkMapper.selectById(network.getBasicNetworkId());
                        BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                                .bridge(basicNetworkEntity.getBridge())
                                .ip(host.getHostIp())
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
                        this.syncInvoker(host, param, Constant.Command.NETWORK_DESTROY_VLAN, vlan);
                    }
                }
            }
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.<Void>builder().build());
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
    public void onFinish(DestroyNetworkOperate param, ResultUtil<Void> resultUtil) {
        localReportTask.removeTaskId(param.getTaskId());
        NetworkEntity network = networkMapper.selectById(param.getNetworkId());
        if (network.getStatus() == cn.roamblue.cloud.management.util.Constant.NetworkStatus.DESTROY) {
            networkMapper.deleteById(param.getNetworkId());
        }
    }
}
