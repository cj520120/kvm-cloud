package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.BasicBridgeNetwork;
import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VlanNetwork;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.GuestNetworkEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.operate.bean.DestroyHostNetworkOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 销毁网络
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyHostNetworkOperateImpl extends AbstractOperate<DestroyHostNetworkOperate, ResultUtil<Void>> {

    public DestroyHostNetworkOperateImpl() {
        super(DestroyHostNetworkOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(DestroyHostNetworkOperate param) {


        if (param.getNextHostIds().isEmpty()) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
            return;
        }
        NetworkEntity network = networkMapper.selectById(param.getNetworkId());
        if (!Objects.equals(cn.roamblue.cloud.management.util.Constant.NetworkStatus.DESTROY, network.getStatus())) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "网络状态不是销毁状态");
        }
        HostEntity host = hostMapper.selectById(param.getNextHostIds().get(0));
        if (host == null || !Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
            //主机未就绪直接提交成功
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
            return;
        }
        switch (network.getType()) {
            case cn.roamblue.cloud.management.util.Constant.NetworkType.BASIC: {
                BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                        .bridge(network.getBridge())
                        .ip(host.getHostIp())
                        .geteway(network.getGateway())
                        .nic(host.getNic())
                        .netmask(network.getMask()).build();
                this.asyncInvoker(host, param, Constant.Command.NETWORK_DESTROY_BASIC, basicBridgeNetwork);
            }
            break;
            case cn.roamblue.cloud.management.util.Constant.NetworkType.VLAN: {
                NetworkEntity basicNetworkEntity = networkMapper.selectById(network.getBasicNetworkId());
                if (basicNetworkEntity == null) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "Vlan的基础网络不存在");
                }
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

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(DestroyHostNetworkOperate param, ResultUtil<Void> resultUtil) {


        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<Integer> hostIds = new ArrayList<>(param.getNextHostIds());
            if (!hostIds.isEmpty()) {
                hostIds.remove(0);
            }
            if (hostIds.isEmpty()) {
                NetworkEntity network = networkMapper.selectById(param.getNetworkId());
                if (network.getStatus() == cn.roamblue.cloud.management.util.Constant.NetworkStatus.DESTROY) {
                    networkMapper.deleteById(param.getNetworkId());
                    guestNetworkMapper.delete(new QueryWrapper<GuestNetworkEntity>().eq("network_id", param.getNetworkId()));
                }
            } else {
                DestroyHostNetworkOperate operate = DestroyHostNetworkOperate.builder().taskId(UUID.randomUUID().toString())
                        .networkId(param.getNetworkId())
                        .networkId(param.getNetworkId())
                        .nextHostIds(hostIds)
                        .build();
                this.operateTask.addTask(operate);
            }
        } else {
            NetworkEntity network = networkMapper.selectById(param.getNetworkId());
            if (network.getStatus() == cn.roamblue.cloud.management.util.Constant.NetworkStatus.DESTROY) {
                network.setStatus(cn.roamblue.cloud.management.util.Constant.NetworkStatus.ERROR);
                networkMapper.updateById(network);
            }
        }

        this.notifyService.publish(NotifyInfo.builder().id(param.getNetworkId()).type(Constant.NotifyType.UPDATE_NETWORK).build());
    }
}
