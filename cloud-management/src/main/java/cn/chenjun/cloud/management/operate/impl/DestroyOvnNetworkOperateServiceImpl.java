package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyOvnNetworkOperate;
import cn.chenjun.cloud.management.ovn.service.OvnService;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.util.RequestContextHolderUtil;
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
public class DestroyOvnNetworkOperateServiceImpl extends AbstractOperateService<DestroyOvnNetworkOperate, ResultUtil<Void>> {

    @Autowired
    private OvnService ovnService;


    @Override
    public void operate(DestroyOvnNetworkOperate param) {
        NetworkEntity network = networkDao.findById(param.getNetworkId());
        switch (network.getStatus()) {
            case Constant.NetworkStatus.ERROR:
            case Constant.NetworkStatus.DESTROY:
                this.destroyOvnNetwork(param, network);
                break;
            default:
                throw new CodeException(ErrorCode.PARAM_ERROR, "不是初始化状态");
        }
    }

    private void destroyOvnNetwork(DestroyOvnNetworkOperate param, NetworkEntity network) {
        String networkName = network.getPoolId();
        this.executor.submit(() -> {
            RequestContextHolderUtil.initContext();
            try {
                boolean isSuccess = ovnService.deleteBridge(networkName);
                ResultUtil<Void> result = ResultUtil.<Void>builder().build();
                if (isSuccess) {
                    result.setCode(ErrorCode.SUCCESS);
                } else {
                    result.setCode(ErrorCode.SERVER_ERROR);
                }
                this.onSubmitFinishEvent(param.getTaskId(), result);
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
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(DestroyOvnNetworkOperate param, ResultUtil<Void> resultUtil) {
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            networkDao.deleteById(param.getNetworkId());
            guestNetworkDao.deleteByNetworkId(param.getNetworkId());
        } else {
            NetworkEntity network = networkDao.findById(param.getNetworkId());
            network.setStatus(Constant.NetworkStatus.ERROR);
            networkDao.update(network);
        }
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getNetworkId()).type(Constant.NotifyType.UPDATE_NETWORK).build());
    }

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_OVN_NETWORK;
    }
}
