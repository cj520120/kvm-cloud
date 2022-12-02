package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.StorageDestroyRequest;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.util.GsonBuilderUtil;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.entity.StorageEntity;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.v2.operate.OperateFactory;
import cn.roamblue.cloud.management.v2.operate.bean.DestroyStorageOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * @author chenjun
 */
public class DestroyStorageOperateImpl extends AbstractOperate<DestroyStorageOperate, ResultUtil<Void>> {

    protected DestroyStorageOperateImpl() {
        super(DestroyStorageOperate.class);
    }

    @Override
    public void operate(DestroyStorageOperate param) {
        StorageMapper storageMapper = SpringContextUtils.getBean(StorageMapper.class);
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        StorageEntity storage = storageMapper.selectById(param.getId());
        List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", storage.getClusterId()));

        for (HostEntity host : hosts) {
            if (Objects.equals(cn.roamblue.cloud.management.v2.util.Constant.HostStatus.READY, host.getStatus())) {
                StorageDestroyRequest request = StorageDestroyRequest.builder()
                        .name(storage.getName())
                        .build();
                this.call(host, param, Constant.Command.STORAGE_DESTROY, request);
            }
        }
        OperateFactory.onCallback(param,"", GsonBuilderUtil.create().toJson(ResultUtil.builder().build()));
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, DestroyStorageOperate param, ResultUtil<Void> resultUtil) {
        StorageMapper storageMapper = SpringContextUtils.getBean(StorageMapper.class);
        storageMapper.deleteById(param.getId());
    }
}
