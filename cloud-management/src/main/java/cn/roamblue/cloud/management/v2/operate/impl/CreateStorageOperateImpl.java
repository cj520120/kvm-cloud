package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.StorageCreateRequest;
import cn.roamblue.cloud.common.bean.StorageInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.GsonBuilderUtil;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.entity.StorageEntity;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.v2.operate.OperateFactory;
import cn.roamblue.cloud.management.v2.operate.bean.CreateStorageOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 创建存储池
 *
 * @author chenjun
 */
public class CreateStorageOperateImpl extends AbstractOperate<CreateStorageOperate, ResultUtil<StorageInfo>> {

    protected CreateStorageOperateImpl() {
        super(CreateStorageOperate.class);
    }

    @Override
    public void operate(CreateStorageOperate param) {
        StorageMapper storageMapper = SpringContextUtils.getBean(StorageMapper.class);
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        StorageEntity storage = storageMapper.selectById(param.getId());
        if (storage.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.StorageStatus.INIT) {
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", storage.getClusterId()));
            ResultUtil<StorageInfo> resultUtil = null;
            for (HostEntity host : hosts) {
                if (Objects.equals(cn.roamblue.cloud.management.v2.util.Constant.HostStatus.ONLINE, host.getStatus())) {
                    Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storage.getParam(), new TypeToken<Map<String, Object>>() {
                    }.getType());
                    StorageCreateRequest request = StorageCreateRequest.builder()
                            .name(storage.getName())
                            .type(storage.getType())
                            .param(storageParam)
                            .build();
                    resultUtil = this.call(host, param, Constant.Command.STORAGE_CREATE, request);
                    if (resultUtil.getCode() != ErrorCode.SUCCESS) {
                        break;
                    }
                }
            }
            OperateFactory.onCallback(param, "", GsonBuilderUtil.create().toJson(resultUtil));
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "存储池[" + storage.getName() + "]状态不正确:" + storage.getStatus());
        }

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, CreateStorageOperate param, ResultUtil<StorageInfo> resultUtil) {
        StorageMapper storageMapper = SpringContextUtils.getBean(StorageMapper.class);
        StorageEntity storage = storageMapper.selectById(param.getId());

        if (storage.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.StorageStatus.INIT) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                storage.setAllocation(resultUtil.getData().getAllocation());
                storage.setCapacity(resultUtil.getData().getCapacity());
                storage.setAvailable(resultUtil.getData().getAvailable());
            } else {
                storage.setStatus(cn.roamblue.cloud.management.v2.util.Constant.StorageStatus.ERROR);
            }
            storageMapper.updateById(storage);
        }
    }

}
