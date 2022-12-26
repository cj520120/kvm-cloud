package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.StorageInfo;
import cn.roamblue.cloud.common.bean.StorageInfoRequest;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.operate.bean.StorageCheckOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 创建磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class StorageCheckOperateImpl extends AbstractOperate<StorageCheckOperate, ResultUtil<List<StorageInfo>>> {

    public StorageCheckOperateImpl() {
        super(StorageCheckOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(StorageCheckOperate param) {

        List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>()).stream().filter(t -> Objects.equals(t.getStatus(), cn.roamblue.cloud.management.util.Constant.StorageStatus.READY)).collect(Collectors.toList());
        if (storageList.isEmpty()) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success(new ArrayList<>()));
        } else {
            List<StorageInfoRequest> requests = storageList.stream().map(t -> StorageInfoRequest.builder().name(t.getName()).build()).collect(Collectors.toList());
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            this.asyncInvoker(host, param, Constant.Command.BATCH_STORAGE_INFO, requests);
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<List<StorageInfo>>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(StorageCheckOperate param, ResultUtil<List<StorageInfo>> resultUtil) {

        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<StorageInfo> storageInfoList = resultUtil.getData();
            if (storageInfoList.isEmpty()) {
                return;
            }
            for (int i = 0; i < storageInfoList.size(); i++) {
                StorageInfo info = storageInfoList.get(i);
                if (info == null) {
                    continue;
                }
                StorageEntity sourceEntity = this.storageMapper.selectOne(new QueryWrapper<StorageEntity>().eq("storage_name", info.getName()));
                if (sourceEntity != null) {
                    if (!Objects.equals(sourceEntity.getCapacity(), info.getCapacity()) ||
                            !Objects.equals(sourceEntity.getAvailable(), info.getAvailable()) ||
                            !Objects.equals(sourceEntity.getAllocation(), info.getAllocation())
                    ) {
                        StorageEntity updateStorage = StorageEntity.builder()
                                .storageId(sourceEntity.getStorageId())
                                .capacity(info.getCapacity())
                                .available(info.getAvailable())
                                .allocation(info.getAllocation())
                                .build();
                        this.storageMapper.updateById(updateStorage);
                        this.notifyService.publish(NotifyInfo.builder().type(Constant.NotifyType.UPDATE_STORAGE).id(sourceEntity.getStorageId()).build());
                    }
                }
            }
        }
    }
}
