package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.BasicBridgeNetwork;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VlanNetwork;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.operate.bean.CreateNetworkOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * 创建网络
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CreateNetworkOperateImpl extends AbstractOperate<CreateNetworkOperate, ResultUtil<Void>> {

    public CreateNetworkOperateImpl() {
        super(CreateNetworkOperate.class);
    }

    @Override
    public void operate(CreateNetworkOperate param) {
        NetworkEntity network = networkMapper.selectById(param.getNetworkId());
        List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>());
        ResultUtil<Void> resultUtil = null;
        for (HostEntity host : hosts) {
            if (Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
                if (Objects.equals(cn.roamblue.cloud.management.util.Constant.NetworkType.BASIC, network.getType())) {
                    BasicBridgeNetwork basicBridgeNetwork = BasicBridgeNetwork.builder()
                            .bridge(network.getBridge())
                            .ip(host.getHostIp())
                            .geteway(network.getGateway())
                            .nic(host.getNic())
                            .netmask(network.getMask()).build();
                    resultUtil = this.syncInvoker(host, param, Constant.Command.NETWORK_CREATE_BASIC, basicBridgeNetwork);
                } else if (Objects.equals(cn.roamblue.cloud.management.util.Constant.NetworkType.VLAN, network.getType())) {
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
                    resultUtil = this.syncInvoker(host, param, Constant.Command.NETWORK_CREATE_VLAN, vlan);
                }else{
                    throw new CodeException(ErrorCode.SERVER_ERROR,"未知的网络类型:"+network.getType());
                }
                if(resultUtil.getCode()!= ErrorCode.SUCCESS){
                    break;
                }
            }
        }
        this.onSubmitFinishEvent(param.getTaskId(), resultUtil);
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(CreateNetworkOperate param, ResultUtil<Void> resultUtil) {
        NetworkEntity network = networkMapper.selectById(param.getNetworkId());
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            network.setStatus(cn.roamblue.cloud.management.util.Constant.NetworkStatus.READY);
        } else {
            network.setStatus(cn.roamblue.cloud.management.util.Constant.NetworkStatus.ERROR);
        }
        networkMapper.updateById(network);
    }
}
