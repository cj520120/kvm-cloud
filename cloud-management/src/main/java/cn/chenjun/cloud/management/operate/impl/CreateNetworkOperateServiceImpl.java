package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.CreateNetworkOperate;
import cn.chenjun.cloud.management.operate.bean.CreateOvnNetworkOperate;
import cn.chenjun.cloud.management.operate.bean.InitHostNetworkOperate;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 创建网络
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CreateNetworkOperateServiceImpl extends AbstractOperateService<CreateNetworkOperate, ResultUtil<Void>> {


    @Override
    public void operate(CreateNetworkOperate param) {
        switch (param.getNetworkType()) {
            case Constant.NetworkType.BASIC:
            case Constant.NetworkType.VLAN: {
                List<HostEntity> hosts = hostDao.listAll();
                List<Integer> hostIds = hosts.stream().filter(t -> Objects.equals(cn.chenjun.cloud.common.util.Constant.HostStatus.ONLINE, t.getStatus())).map(HostEntity::getHostId).collect(Collectors.toList());
                if (ObjectUtils.isEmpty(hostIds)) {
                    NetworkEntity network = networkDao.findById(param.getNetworkId());
                    network.setStatus(Constant.NetworkStatus.INSTALL);
                    networkDao.update(network);
                    this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
                    return;
                }
                InitHostNetworkOperate operate = InitHostNetworkOperate.builder().id(UUID.randomUUID().toString())
                        .title(param.getTitle())
                        .networkId(param.getNetworkId())
                        .networkId(param.getNetworkId())
                        .nextHostIds(hostIds)
                        .build();
                this.taskService.addTask(operate);
                this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
            }
            break;
            case Constant.NetworkType.VxLAN:
                CreateOvnNetworkOperate operate = CreateOvnNetworkOperate.builder().id(UUID.randomUUID().toString()).networkId(param.getNetworkId()).build();
                this.taskService.addTask(operate);
                this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
                break;
            default:
                throw new CodeException(ErrorCode.PARAM_ERROR, "不支持的网络类型");
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(CreateNetworkOperate param, ResultUtil<Void> resultUtil) {
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            NetworkEntity network = networkDao.findById(param.getNetworkId());
            if (network != null && Objects.equals(network.getStatus(), cn.chenjun.cloud.common.util.Constant.NetworkStatus.CREATING)) {
                network.setStatus(cn.chenjun.cloud.common.util.Constant.NetworkStatus.ERROR);
                networkDao.update(network);
            }
        }
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());

    }

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_NETWORK;
    }
}
