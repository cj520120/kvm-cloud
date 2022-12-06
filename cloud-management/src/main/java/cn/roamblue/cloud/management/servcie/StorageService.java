package cn.roamblue.cloud.management.servcie;

import cn.hutool.core.convert.impl.BeanConverter;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.model.StorageModel;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.operate.bean.CreateStorageOperate;
import cn.roamblue.cloud.management.operate.bean.DestroyStorageOperate;
import cn.roamblue.cloud.management.task.OperateTask;
import cn.roamblue.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StorageService {
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private OperateTask operateTask;

    private StorageModel initStorageModel(StorageEntity entity) {
        return new BeanConverter<>(StorageModel.class).convert(entity, null);
    }

    public ResultUtil<List<StorageModel>> listStorage(int clusterId) {
        List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<StorageEntity>().eq("cluster_id", clusterId));
        List<StorageModel> models = storageList.stream().map(this::initStorageModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    public ResultUtil<StorageModel> getStorageInfo(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        return ResultUtil.success(this.initStorageModel(storage));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> createStorage(int clusterId, String name, String type, String param) {
        StorageEntity storage = StorageEntity.builder()
                .clusterId(clusterId)
                .name(name)
                .type(type)
                .param(param)
                .mountPath("/mnt/" + UUID.randomUUID().toString().toLowerCase().replace("-", ""))
                .status(Constant.StorageStatus.INIT)
                .build();
        this.storageMapper.insert(storage);
        BaseOperateParam operateParam = CreateStorageOperate.builder().taskId(UUID.randomUUID().toString()).storageId(storage.getStorageId()).build();
        this.operateTask.addTask(operateParam);
        return ResultUtil.success(this.initStorageModel(storage));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> destroyStorage(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        switch (storage.getStatus()) {
            case Constant.StorageStatus.READY:
            case Constant.StorageStatus.ERROR:
                storage.setStatus(Constant.StorageStatus.DESTROY);
                BaseOperateParam operateParam = DestroyStorageOperate.builder().taskId(UUID.randomUUID().toString()).storageId(storage.getStorageId()).build();
                this.operateTask.addTask(operateParam);
                return ResultUtil.success(this.initStorageModel(storage));
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池状态未就绪");
        }
    }
}
