package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.CreateOvnNetworkOperate;
import cn.chenjun.cloud.management.ovn.model.response.CreateBridgeData;
import cn.chenjun.cloud.management.ovn.service.OvnService;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.util.RequestContextHolderUtil;
import cn.chenjun.cloud.management.util.SubnetCalculator;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * 创建网络
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CreateOvnNetworkOperateServiceImpl extends AbstractOperateService<CreateOvnNetworkOperate, ResultUtil<CreateBridgeData>> {

    @Autowired
    private OvnService ovnService;


    @Override
    public void operate(CreateOvnNetworkOperate param) {
        NetworkEntity network = networkDao.findById(param.getNetworkId());
        switch (network.getStatus()) {
            case Constant.NetworkStatus.CREATING:
            case Constant.NetworkStatus.MAINTENANCE:
                this.initOvnNetwork(param, network);
                break;
            default:
                throw new CodeException(ErrorCode.PARAM_ERROR, "不是初始化状态");
        }
    }

    private void initOvnNetwork(CreateOvnNetworkOperate param, NetworkEntity network) {
        String cidr = network.getSubnet() + "/" + Long.bitCount(SubnetCalculator.ipToLong(network.getMask()));
        String gateway = network.getName();
        String networkName = network.getPoolId();
        this.executor.submit(() -> {
            RequestContextHolderUtil.initContext();
            try {
                CreateBridgeData createBridgeData = ovnService.createBridge(networkName, cidr, gateway);
                this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success(createBridgeData));
            } catch (Exception err) {
                this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.error(ErrorCode.SERVER_ERROR, "Request error: " + err.getMessage()));
            } finally {
                RequestContextHolderUtil.clearContext();
                NotifyContextHolderUtil.afterCompletion();
            }
        });
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<CreateBridgeData>>() {
        }.getType();
    }

    @Override
    public void onFinish(CreateOvnNetworkOperate param, ResultUtil<CreateBridgeData> resultUtil) {
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
        return Constant.OperateType.CREATE_OVN_NETWORK;
    }
}
