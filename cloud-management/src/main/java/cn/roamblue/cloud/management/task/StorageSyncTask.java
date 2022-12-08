package cn.roamblue.cloud.management.task;

import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.StorageInfo;
import cn.roamblue.cloud.common.bean.StorageInfoRequest;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.servcie.AllocateService;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StorageSyncTask extends AbstractTask {
    private final int TASK_CHECK_TIME = (int) TimeUnit.MINUTES.toSeconds(10);

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private VolumeMapper volumeMapper;

    @Autowired
    private AllocateService allocateService;
    @Autowired
    private StorageMapper storageMapper;

    @Override
    protected void dispatch() {
        RBucket<Long> rBucket = redissonClient.getBucket(RedisKeyUtil.STORAGE_SYNC_KEY);
        if (rBucket.isExists()) {
            return;
        }
        if (rBucket.trySet(System.currentTimeMillis(), TASK_CHECK_TIME, TimeUnit.SECONDS)) {
            List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>()).stream().filter(t -> Objects.equals(t.getStorageId(), cn.roamblue.cloud.management.util.Constant.StorageStatus.READY)).collect(Collectors.toList());
            List<StorageInfoRequest> requests = storageList.stream().map(t -> StorageInfoRequest.builder().name(t.getName()).build()).collect(Collectors.toList());
            Map<String, Object> map = new HashMap<>(3);
            map.put("command", Constant.Command.BATCH_STORAGE_INFO);
            map.put("taskId", UUID.randomUUID().toString());
            map.put("data", GsonBuilderUtil.create().toJson(requests));
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            try {
                String uri = String.format("%s/api/operate", host.getUri());
                String response = HttpUtil.post(uri, map);
                ResultUtil<List<StorageInfo>> resultUtil = GsonBuilderUtil.create().fromJson(response, new TypeToken<ResultUtil<List<StorageInfo>>>() {
                }.getType());
                List<StorageInfo> storageInfoList = resultUtil.getData();
                for (int i = 0; i < storageInfoList.size(); i++) {
                    StorageInfo info = storageInfoList.get(i);
                    if (info == null) {
                        continue;
                    }
                    StorageEntity updateStorage = StorageEntity.builder()
                            .storageId(storageList.get(i).getStorageId())
                            .capacity(info.getCapacity())
                            .allocation(info.getAllocation())
                            .build();
                    this.storageMapper.updateById(updateStorage);
                }
            } catch (Exception err) {
                log.error("同步存储池失败.Host={}", host.getDisplayName(), err);
            }

        }
    }
}
