package cn.ruiyun.cloud.management.operate.impl;

import cn.ruiyun.cloud.common.bean.HostInfo;
import cn.ruiyun.cloud.common.bean.NoneRequest;
import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.data.entity.HostEntity;
import cn.ruiyun.cloud.management.operate.bean.HostCheckOperate;
import cn.ruiyun.cloud.management.servcie.bean.ConfigQuery;
import cn.ruiyun.cloud.management.util.CacheKeyUtil;
import cn.ruiyun.cloud.management.util.ConfigKey;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
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
public class HostCheckOperateServiceImpl extends AbstractOperateService<HostCheckOperate, ResultUtil<HostInfo>> {


    @Override
    public void operate(HostCheckOperate param) {
        HostEntity host = this.hostDao.findById(param.getHostId());
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
        String hostKeepKey = CacheKeyUtil.getHostLastKeepKey(updateHost.getHostId());
        boolean isNotify = false;
        if (hostInfo != null) {
            isNotify = !Objects.equals(updateHost.getStatus(), Constant.HostStatus.ONLINE);
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
            updateHost.setStatus(Constant.HostStatus.ONLINE);
            hostDao.update(updateHost);
            List<ConfigQuery> queryList = new ArrayList<>(2);
            queryList.add(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build());
            queryList.add(ConfigQuery.builder().type(Constant.ConfigType.HOST).id(1).build());
            int expire = this.configService.getConfig(queryList, ConfigKey.DEFAULT_TASK_HOST_CHECK_TIMEOUT_SECOND);
            this.cacheService.put(hostKeepKey, System.currentTimeMillis(), TimeUnit.SECONDS.toMillis(Math.max(1, expire * 2)));
        } else if (!cacheService.exists(hostKeepKey)) {
            isNotify = !Objects.equals(updateHost.getStatus(), Constant.HostStatus.OFFLINE);
            updateHost.setStatus(Constant.HostStatus.OFFLINE);
            hostDao.update(updateHost);
        }
        if (isNotify) {
            NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getHostId()).type(Constant.NotifyType.UPDATE_HOST).build());
        }
    }

    @Override
    public int getType() {
        return Constant.OperateType.HOST_CHECK;
    }
}
