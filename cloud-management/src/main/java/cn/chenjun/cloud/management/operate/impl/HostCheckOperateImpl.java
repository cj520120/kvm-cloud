package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.HostInfo;
import cn.chenjun.cloud.common.bean.NoneRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.HostCheckOperate;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class HostCheckOperateImpl extends AbstractOperate<HostCheckOperate, ResultUtil<HostInfo>> {


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
        String hostKeepKey = RedisKeyUtil.getHostLastKeepKey(updateHost.getHostId());
        RBucket<Long> rBucket = this.redissonClient.getBucket(hostKeepKey);
        boolean isNotify = false;
        if (hostInfo != null) {
            isNotify = !Objects.equals(updateHost.getStatus(), cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE);
            updateHost.setHostName(hostInfo.getHostName());
            updateHost.setOsName(hostInfo.getName());
            updateHost.setOsVersion(hostInfo.getOsVersion());
            updateHost.setTotalCpu(hostInfo.getCpu().getNumber());
            updateHost.setTotalMemory(hostInfo.getMemory());
            updateHost.setEmulator(hostInfo.getEmulator());
            updateHost.setCores(hostInfo.getCpu().getCores());
            updateHost.setSockets(hostInfo.getCpu().getSockets());
            updateHost.setThreads(hostInfo.getCpu().getThreads());
            updateHost.setArch(hostInfo.getCpu().getArch());
            updateHost.setVendor(hostInfo.getCpu().getVendor());
            updateHost.setModel(hostInfo.getCpu().getModel());
            updateHost.setFrequency(hostInfo.getCpu().getFrequency());
            updateHost.setHypervisor(hostInfo.getHypervisor());
            updateHost.setStatus(cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE);
            hostMapper.updateById(updateHost);
            List<ConfigQuery> queryList = new ArrayList<>(2);
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.DEFAULT).id(0).build());
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.HOST).id(1).build());
            int expire = this.configService.getConfig(queryList, ConfigKey.DEFAULT_CLUSTER_TASK_HOST_CHECK_TIMEOUT_SECOND);
            rBucket.set(System.currentTimeMillis(), Math.max(1, expire * 2), TimeUnit.SECONDS);
        } else if (!rBucket.isExists()) {
            isNotify = !Objects.equals(updateHost.getStatus(), cn.chenjun.cloud.management.util.Constant.HostStatus.OFFLINE);
            updateHost.setStatus(cn.chenjun.cloud.management.util.Constant.HostStatus.OFFLINE);
            hostMapper.updateById(updateHost);
        }
        if (isNotify) {
            this.notifyService.publish(NotifyData.<Void>builder().id(param.getHostId()).type(Constant.NotifyType.UPDATE_HOST).build());
        }
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.HOST_CHECK;
    }
}
