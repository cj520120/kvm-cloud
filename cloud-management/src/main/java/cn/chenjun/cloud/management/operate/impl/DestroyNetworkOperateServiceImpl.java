package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyHostNetworkOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyNetworkOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyOvnNetworkOperate;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 销毁网络
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyNetworkOperateServiceImpl extends AbstractOperateService<DestroyNetworkOperate, ResultUtil<Void>> {


    @Override
    public void operate(DestroyNetworkOperate param) {
        if (param.getNetworkType() == Constant.NetworkType.VxLAN) {
            DestroyOvnNetworkOperate operate = DestroyOvnNetworkOperate.builder().id(UUID.randomUUID().toString()).networkId(param.getNetworkId()).build();
            this.taskService.addTask(operate);
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
        } else {
            List<HostEntity> hosts = hostDao.listByStatus(Constant.HostStatus.ONLINE);
            List<Integer> hostIds = hosts.stream().map(HostEntity::getHostId).collect(Collectors.toList());
            DestroyHostNetworkOperate operate = DestroyHostNetworkOperate.builder().id(UUID.randomUUID().toString())
                    .title(param.getTitle())
                    .networkId(param.getNetworkId())
                    .nextHostIds(hostIds)
                    .build();
            this.taskService.addTask(operate);
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(DestroyNetworkOperate param, ResultUtil<Void> resultUtil) {
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            NetworkEntity network = networkDao.findById(param.getNetworkId());
            if (network != null && network.getStatus() == Constant.NetworkStatus.DESTROY) {
                networkDao.deleteById(param.getNetworkId());
                this.configService.deleteAllocateConfig(Constant.ConfigType.NETWORK, param.getNetworkId());
            }
        }

        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getNetworkId()).type(Constant.NotifyType.UPDATE_NETWORK).build());
    }

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_NETWORK;
    }
}
