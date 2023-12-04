package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.NoneRequest;
import cn.chenjun.cloud.common.bean.HostInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.HostCheckOperate;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class HostCheckOperateImpl extends AbstractOperate<HostCheckOperate, ResultUtil<HostInfo>> {

    public HostCheckOperateImpl() {
        super(HostCheckOperate.class);
    }

    @Override
    public void operate(HostCheckOperate param) {


        HostEntity host = this.hostMapper.selectById(param.getHostId());
        this.asyncInvoker(host, param, Constant.Command.HOST_INFO, NoneRequest.builder());
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<HostInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(HostCheckOperate param, ResultUtil<HostInfo> resultUtil) {
        HostInfo hostInfo = resultUtil.getData();
        HostEntity updateHost = HostEntity.builder().hostId(param.getHostId()).build();
        if (hostInfo != null) {
            updateHost.setHostName(hostInfo.getHostName());
            updateHost.setTotalCpu(hostInfo.getCpu());
            updateHost.setTotalMemory(hostInfo.getMemory());
            updateHost.setEmulator(hostInfo.getEmulator());
            updateHost.setCores(hostInfo.getCores());
            updateHost.setSockets(hostInfo.getSockets());
            updateHost.setThreads(hostInfo.getThreads());
            updateHost.setArch(hostInfo.getArch());
            updateHost.setHypervisor(hostInfo.getHypervisor());
            updateHost.setStatus(cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE);
        } else {
            updateHost.setStatus(cn.chenjun.cloud.management.util.Constant.HostStatus.OFFLINE);
        }
        hostMapper.updateById(updateHost);
    }
}
