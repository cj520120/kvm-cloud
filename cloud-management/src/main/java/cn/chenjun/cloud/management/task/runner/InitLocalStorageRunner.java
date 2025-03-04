package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.data.mapper.StorageMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.CreateStorageOperate;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.NotifyService;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.MapUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        String path = this.configService.getConfig(Constant.ConfigKey.STORAGE_LOCAL_PATH);
        String enable = this.configService.getConfig(Constant.ConfigKey.STORAGE_LOCAL_ENABLE);
        if (Objects.equals(Constant.Enable.YES, enable)) {
            Map<String, String> storageParm = MapUtil.of("path", path);
            String paramStr = GsonBuilderUtil.create().toJson(storageParm);
            List<HostEntity> hostList = this.hostMapper.selectList(new QueryWrapper<>());
            Map<Integer, HostEntity> hostMap = hostList.stream().collect(Collectors.toMap(HostEntity::getHostId, Function.identity()));

            if (!hostMap.isEmpty()) {
                for (HostEntity host : hostMap.values()) {
                    List<StorageEntity> storageList=this.storageMapper.selectList(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_HOST_ID,host.getHostId()).eq(StorageEntity.STORAGE_TYPE, cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL));
                    if (storageList.stream().filter(v-> Objects.equals(v.getMountPath(),path)).count()==0) {
                        String storageName = UUID.randomUUID().toString().replace("-", "").toUpperCase();
                        String mountPath = path;
                        StorageEntity storage = StorageEntity.builder()
                                .description("Local Storage(" + host.getDisplayName() + ")")
                                .name(storageName)
                                .type(cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL)
                                .hostId(host.getHostId())
                                .param(paramStr)
                                .mountPath(mountPath)
                                .supportCategory(Constant.StorageSupportCategory.VOLUME)
                                .allocation(0L)
                                .capacity(0L)
                                .available(0L)
                                .status(Constant.StorageStatus.INIT)
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
        return Objects.equals(this.configService.getConfig(Constant.ConfigKey.STORAGE_LOCAL_ENABLE), Constant.Enable.YES);
    }
}
