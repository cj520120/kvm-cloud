package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VxLanNetworkRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.CreateVxLanNetworkOperate;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.HostRole;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.util.SubnetCalculator;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Type;

/**
 * 创建网络
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CreateVxLanNetworkOperateServiceImpl extends AbstractOperateService<CreateVxLanNetworkOperate, ResultUtil<Void>> {




    @Override
    public void operate(CreateVxLanNetworkOperate param) {
        NetworkEntity network = networkDao.findById(param.getNetworkId());
        switch (network.getStatus()) {
            case Constant.NetworkStatus.CREATING:
            case Constant.NetworkStatus.MAINTENANCE:
                String cidr = network.getSubnet() + "/" + Long.bitCount(SubnetCalculator.ipToLong(network.getMask()));
                String baseUrl = configService.getConfig(ConfigKey.NETWORK_VX_LAN_BASE_URL, "");
                String token = this.configService.getConfig(ConfigKey.NETWORK_VX_LAN_API_KEY);
                if (ObjectUtils.isEmpty(baseUrl) || ObjectUtils.isEmpty(token)) {
                    throw new CodeException(ErrorCode.PARAM_ERROR, "VxLan代理配置不正确");
                }
                HostEntity host = this.allocateService.allocateHost(HostRole.MASTER, 0, null, 0, 0, 0);
                VxLanNetworkRequest request = VxLanNetworkRequest.builder()
                        .poolId(network.getPoolId())
                        .cidr(cidr)
                        .gateway(network.getGateway())
                        .token(token)
                        .baseUrl(baseUrl)
                        .build();
                this.asyncInvoker(host, param, Constant.Command.NETWORK_CREATE_VxLAN, request);
                break;
            default:
                throw new CodeException(ErrorCode.PARAM_ERROR, "不是初始化状态");
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(CreateVxLanNetworkOperate param, ResultUtil<Void> resultUtil) {
        NetworkEntity network = networkDao.findById(param.getNetworkId());
        if (network != null) {
            switch (network.getStatus()) {
                case Constant.NetworkStatus.CREATING:
                case Constant.NetworkStatus.MAINTENANCE:
                    if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                        network.setStatus(Constant.NetworkStatus.INSTALL);
                    } else {
                        network.setStatus(Constant.NetworkStatus.ERROR);
                    }
                    networkDao.update(network);
                default:
                    break;
            }
            NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getNetworkId()).type(Constant.NotifyType.UPDATE_NETWORK).build());
        }
    }

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_VX_LAN_NETWORK;
    }
}
