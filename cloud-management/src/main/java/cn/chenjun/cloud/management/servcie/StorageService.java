package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.NotifyInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.model.StorageModel;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.CreateStorageOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyStorageOperate;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class StorageService extends AbstractService {


    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<List<StorageModel>> listStorage() {
        List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>());
        List<StorageModel> models = storageList.stream().map(this::initStorageModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    public ResultUtil<StorageModel> getStorageInfo(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        return ResultUtil.success(this.initStorageModel(storage));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> createStorage(String description, String type, String param) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入存储池名称");
        }
        if (StringUtils.isEmpty(type)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择存储池类型");
        }
        if (StringUtils.isEmpty(param)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "存储池参数不正确");
        }
        String storageName = UUID.randomUUID().toString().toLowerCase().replace("-", "");
        StorageEntity storage = StorageEntity.builder()
                .description(description)
                .name(storageName)
                .type(type)
                .param(param)
                .mountPath("/mnt/" + storageName)
                .allocation(0L)
                .capacity(0L)
                .available(0L)
                .status(Constant.StorageStatus.INIT)
                .build();
        this.storageMapper.insert(storage);
        BaseOperateParam operateParam = CreateStorageOperate.builder().taskId(UUID.randomUUID().toString()).title("创建存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyInfo.builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
        return ResultUtil.success(this.initStorageModel(storage));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> registerStorage(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        switch (storage.getStatus()) {
            case Constant.StorageStatus.READY:
            case Constant.StorageStatus.INIT:
            case Constant.StorageStatus.ERROR:
            case Constant.StorageStatus.MAINTENANCE:
                storage.setStatus(Constant.StorageStatus.INIT);
                this.storageMapper.updateById(storage);
                BaseOperateParam operateParam = CreateStorageOperate.builder().taskId(UUID.randomUUID().toString()).title("注册存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
                this.operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyInfo.builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
                return ResultUtil.success(this.initStorageModel(storage));
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "等待存储池状态就绪");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> maintenanceStorage(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        switch (storage.getStatus()) {
            case Constant.StorageStatus.READY:
            case Constant.StorageStatus.MAINTENANCE:
            case Constant.StorageStatus.ERROR:
                storage.setStatus(Constant.StorageStatus.MAINTENANCE);
                this.storageMapper.updateById(storage);
                this.notifyService.publish(NotifyInfo.builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
                return ResultUtil.success(this.initStorageModel(storage));
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "等待存储池状态就绪");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<StorageModel> destroyStorage(int storageId) {
        StorageEntity storage = this.storageMapper.selectById(storageId);
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在");
        }
        switch (storage.getStatus()) {
            case Constant.StorageStatus.READY:
            case Constant.StorageStatus.ERROR:
                if (volumeMapper.selectCount(new QueryWrapper<VolumeEntity>().eq("storage_id", storageId)) > 0) {
                    throw new CodeException(ErrorCode.STORAGE_BUSY, "当前存储有挂载磁盘，请首先迁移存储文件");
                }
                storage.setStatus(Constant.StorageStatus.DESTROY);
                this.storageMapper.updateById(storage);
                BaseOperateParam operateParam = DestroyStorageOperate.builder().taskId(UUID.randomUUID().toString()).title("销毁存储池[" + storage.getName() + "]").storageId(storage.getStorageId()).build();
                this.operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyInfo.builder().id(storage.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
                return ResultUtil.success(this.initStorageModel(storage));
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "等待存储池状态就绪");
        }
    }
}
