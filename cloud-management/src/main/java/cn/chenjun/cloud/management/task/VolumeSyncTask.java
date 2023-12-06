package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.mapper.StorageMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.VolumeCheckOperate;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
public class VolumeSyncTask extends AbstractTask {


    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    @Lazy
    private OperateTask operateTask;

    @Override
    protected int getPeriodSeconds() {
        return 600;
    }

    @Override
    protected void dispatch() {
        List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>()).stream().filter(t -> Objects.equals(t.getStatus(), Constant.StorageStatus.READY)).collect(Collectors.toList());
        for (StorageEntity storage : storageList) {
            BaseOperateParam operateParam = VolumeCheckOperate.builder().taskId(UUID.randomUUID().toString()).title("检测存储池磁盘使用情况").storageId(storage.getStorageId()).build();
            this.operateTask.addTask(operateParam);
        }
    }

}
