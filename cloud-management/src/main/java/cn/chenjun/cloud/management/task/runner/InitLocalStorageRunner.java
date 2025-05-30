package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.MapUtil;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.data.mapper.StorageMapper;
import cn.chenjun.cloud.management.operate.bean.CreateStorageOperate;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.NotifyService;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class InitLocalStorageRunner extends AbstractRunner {
    @Autowired
    @Lazy
    protected TaskService operateTask;
    @Autowired
    protected NotifyService notifyService;
    @Autowired
    protected ConfigService configService;
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private HostMapper hostMapper;

    @Override
    protected void dispatch() throws Exception {
        List<HostEntity> hostList = this.hostMapper.selectList(new QueryWrapper<>());
        Map<Integer, HostEntity> hostMap = hostList.stream().collect(Collectors.toMap(HostEntity::getHostId, Function.identity()));

        if (!hostMap.isEmpty()) {
            for (HostEntity host : hostMap.values()) {
                List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.DEFAULT).build(), ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.HOST).id(host.getHostId()).build());
                String enable = this.configService.getConfig(queryList, ConfigKey.STORAGE_LOCAL_ENABLE);
                if (Objects.equals(cn.chenjun.cloud.common.util.Constant.Enable.YES, enable)) {
                    String path = this.configService.getConfig(queryList, ConfigKey.STORAGE_LOCAL_PATH);
                    Map<String, String> storageParm = MapUtil.of("path", path);
                    String paramStr = GsonBuilderUtil.create().toJson(storageParm);
                    List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_HOST_ID, host.getHostId()).eq(StorageEntity.STORAGE_TYPE, cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL));
                    if (storageList.stream().noneMatch(v -> Objects.equals(v.getMountPath(), path))) {
                        String storageName = UUID.randomUUID().toString().replace("-", "").toUpperCase();
                        StorageEntity storage = StorageEntity.builder()
                                .description("Local Storage(" + host.getDisplayName() + ")")
                                .name(storageName)
                                .type(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL)
                                .hostId(host.getHostId())
                                .param(paramStr)
                                .mountPath(path)
                                .supportCategory(Constant.StorageCategory.VOLUME)
                                .allocation(0L)
                                .capacity(0L)
                                .available(0L)
                                .status(cn.chenjun.cloud.common.util.Constant.StorageStatus.INIT)
                                .build();
                        this.storageMapper.insert(storage);
                        BaseOperateParam operateParam = CreateStorageOperate.builder().id(UUID.randomUUID().toString()).title("创建存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
                        this.operateTask.addTask(operateParam);
                        this.notifyService.publish(NotifyData.<Void>builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
                    }

                }
            }

        }
    }

    @Override
    protected String getName() {
        return "初始化本地存储池";
    }

    @Override
    protected boolean isStart() {
        return Objects.equals(this.configService.getConfig(ConfigKey.STORAGE_LOCAL_ENABLE), cn.chenjun.cloud.common.util.Constant.Enable.YES);
    }
}
