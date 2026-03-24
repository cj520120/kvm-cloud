package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyHostNetworkOperate;
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
public class DestroyHostNetworkOperateServiceImpl extends AbstractOperateService<DestroyHostNetworkOperate, ResultUtil<Void>> {


    @Override
    public void operate(DestroyHostNetworkOperate param) {


        if (param.getNextHostIds().isEmpty()) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
            return;
        }
        NetworkEntity network = networkDao.findById(param.getNetworkId());
        if (!Objects.equals(Constant.NetworkStatus.DESTROY, network.getStatus())) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "网络状态不是销毁状态");
        }
        HostEntity host = hostDao.findById(param.getNextHostIds().get(0));
        if (host == null || !Objects.equals(Constant.HostStatus.ONLINE, host.getStatus())) {
            //主机未就绪直接提交成功
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
            return;
        }
        switch (network.getType()) {
            case Constant.NetworkType.BASIC: {
                BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                        .poolId(network.getPoolId())
                        .build();
                this.asyncInvoker(host, param, Constant.Command.NETWORK_DESTROY_BASIC, basicBridgeNetwork);
            }
            break;
            case Constant.NetworkType.VLAN: {
                NetworkEntity basicNetworkEntity = networkDao.findById(network.getBasicNetworkId());
                if (basicNetworkEntity == null) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "Vlan的基础网络不存在");
                }
                VlanNetwork vlan = VlanNetwork.builder().poolId(network.getPoolId()).build();
                this.asyncInvoker(host, param, Constant.Command.NETWORK_DESTROY_VLAN, vlan);
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
    public void onFinish(DestroyHostNetworkOperate param, ResultUtil<Void> resultUtil) {


        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<Integer> hostIds = new ArrayList<>(param.getNextHostIds());
            if (!hostIds.isEmpty()) {
                hostIds.remove(0);
            }
            if (hostIds.isEmpty()) {
                NetworkEntity network = networkDao.findById(param.getNetworkId());
                if (network != null && network.getStatus() == Constant.NetworkStatus.DESTROY) {
                    networkDao.deleteById(param.getNetworkId());
                    guestNetworkDao.deleteByNetworkId(param.getNetworkId());
                }
            } else {
                DestroyHostNetworkOperate operate = DestroyHostNetworkOperate.builder().id(UUID.randomUUID().toString())
                        .networkId(param.getNetworkId())
                        .networkId(param.getNetworkId())
                        .nextHostIds(hostIds)
                        .build();
                this.taskService.addTask(operate);
            }
        } else {
            NetworkEntity network = networkDao.findById(param.getNetworkId());
            if (network != null && network.getStatus() == Constant.NetworkStatus.DESTROY) {
                network.setStatus(Constant.NetworkStatus.ERROR);
                networkDao.update(network);
            }
        }

        this.notifyService.publish(NotifyData.<Void>builder().id(param.getNetworkId()).type(Constant.NotifyType.UPDATE_NETWORK).build());
    }

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_HOST_NETWORK;
    }
}
