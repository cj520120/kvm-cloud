package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.InitHostNetworkOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class InitHostNetworkOperateImpl extends AbstractNetworkOperate<InitHostNetworkOperate, ResultUtil<Void>> {


    @Override
    public void operate(InitHostNetworkOperate param) {

        if (param.getNextHostIds().isEmpty()) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
            return;
        }
        NetworkEntity network = networkMapper.selectById(param.getNetworkId());
        switch (network.getStatus()) {
            case cn.chenjun.cloud.management.util.Constant.NetworkStatus.CREATING:
            case cn.chenjun.cloud.management.util.Constant.NetworkStatus.MAINTENANCE:
                break;
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "网络状态不是创建状态");
        }
        HostEntity host = hostMapper.selectById(param.getNextHostIds().get(0));
        if (host == null || !Objects.equals(cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
            //主机未就绪直接提交成功
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
            return;
        }
        switch (network.getType()) {
            case cn.chenjun.cloud.management.util.Constant.NetworkType.BASIC: {
                BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                        .poolId(network.getPoolId())
                        .bridge(network.getBridge())
                        .bridgeType(Constant.NetworkBridgeType.fromBridgeType(network.getBridgeType()))
                        .build();
                this.asyncInvoker(host, param, Constant.Command.NETWORK_CREATE_BASIC, basicBridgeNetwork);
            }
            break;
            case cn.chenjun.cloud.management.util.Constant.NetworkType.VLAN: {
                NetworkEntity basicNetworkEntity = networkMapper.selectById(network.getBasicNetworkId());
                if (basicNetworkEntity == null) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "Vlan的基础网络不存在");
                }
                BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                        .poolId(basicNetworkEntity.getPoolId())
                        .bridge(basicNetworkEntity.getBridge())
                        .bridgeType(Constant.NetworkBridgeType.fromBridgeType(network.getBridgeType()))
                        .build();
                VlanNetwork vlan = VlanNetwork.builder()
                        .poolId(network.getPoolId())
                        .vlanId(network.getVlanId())
                        .basic(basicBridgeNetwork)
                        .bridge(basicNetworkEntity.getBridge())
                        .build();
                this.asyncInvoker(host, param, Constant.Command.NETWORK_CREATE_VLAN, vlan);
            }
            break;
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "未知的网络类型:" + network.getType());
        }

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(InitHostNetworkOperate param, ResultUtil<Void> resultUtil) {
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<Integer> hostIds = new ArrayList<>(param.getNextHostIds());
            if (!hostIds.isEmpty()) {
                hostIds.remove(0);
            }
            if (hostIds.isEmpty()) {
                NetworkEntity network = networkMapper.selectById(param.getNetworkId());
                if (network != null) {
                    switch (network.getStatus()) {
                        case cn.chenjun.cloud.management.util.Constant.NetworkStatus.CREATING:
                        case cn.chenjun.cloud.management.util.Constant.NetworkStatus.MAINTENANCE:
                            network.setStatus(cn.chenjun.cloud.management.util.Constant.NetworkStatus.INSTALL);
                            networkMapper.updateById(network);
                        default:
                            break;
                    }
                    this.eventService.publish(NotifyData.<Void>builder().id(param.getNetworkId()).type(Constant.NotifyType.UPDATE_NETWORK).build());
                }
            } else {
                InitHostNetworkOperate operate = InitHostNetworkOperate.builder().taskId(UUID.randomUUID().toString())
                        .networkId(param.getNetworkId())
                        .networkId(param.getNetworkId())
                        .nextHostIds(hostIds)
                        .build();
                this.operateTask.addTask(operate);
            }
        } else {
            NetworkEntity network = networkMapper.selectById(param.getNetworkId());
            if (network != null) {
                switch (network.getStatus()) {
                    case cn.chenjun.cloud.management.util.Constant.NetworkStatus.CREATING:
                    case cn.chenjun.cloud.management.util.Constant.NetworkStatus.MAINTENANCE:
                        network.setStatus(cn.chenjun.cloud.management.util.Constant.NetworkStatus.ERROR);
                        networkMapper.updateById(network);
                    default:
                        break;
                }
                this.eventService.publish(NotifyData.<Void>builder().id(param.getNetworkId()).type(Constant.NotifyType.UPDATE_NETWORK).build());
            }
        }
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.INIT_HOST_NETWORK;
    }
}
