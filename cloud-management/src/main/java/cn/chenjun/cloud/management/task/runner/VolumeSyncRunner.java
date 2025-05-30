package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.mapper.StorageMapper;
import cn.chenjun.cloud.management.operate.bean.VolumeCheckOperate;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.util.ConfigKey;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class VolumeSyncRunner extends AbstractRunner {


    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private TaskService taskService;

    @Override
    public int getPeriodSeconds() {
        return configService.getConfig(ConfigKey.DEFAULT_TASK_STORAGE_VOLUME_SYNC_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() {
        List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>()).stream().filter(t -> Objects.equals(t.getStatus(), Constant.StorageStatus.READY)).collect(Collectors.toList());
        for (StorageEntity storage : storageList) {
            BaseOperateParam operateParam = VolumeCheckOperate.builder().id(UUID.randomUUID().toString()).title("检测存储池磁盘使用情况").storageId(storage.getStorageId()).build();
            this.taskService.addTask(operateParam);
        }
    }

    @Override
    protected String getName() {
        return "检测存储池磁盘";
    }

}
