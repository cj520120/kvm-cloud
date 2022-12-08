package cn.roamblue.cloud.management.task;

import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.bean.VolumeInfoRequest;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
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
public class VolumeSyncTask extends AbstractTask {

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
    protected int getPeriodSeconds() {
        return (int) TimeUnit.MINUTES.toSeconds(10);
    }

    @Override
    protected void dispatch() {

        RBucket<Long> rBucket = redissonClient.getBucket(RedisKeyUtil.VOLUME_SYNC_KEY);
        if (rBucket.isExists()) {
            return;
        }
        if (rBucket.trySet(System.currentTimeMillis(), TASK_CHECK_TIME, TimeUnit.SECONDS)) {
            List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>()).stream().filter(t -> Objects.equals(t.getStorageId(), cn.roamblue.cloud.management.util.Constant.StorageStatus.READY)).collect(Collectors.toList());
            for (StorageEntity storage : storageList) {
                List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq("storage_id", storage.getStorageId())).stream().filter(t -> Objects.equals(t.getStatus(), cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY)).collect(Collectors.toList());
                if (volumeList.isEmpty()) {
                    continue;
                }
                List<VolumeInfoRequest> requests = volumeList.stream().map(t -> VolumeInfoRequest.builder().sourceName(t.getName()).sourceVolume(t.getPath()).sourceStorage(storage.getName()).build()).collect(Collectors.toList());
                Map<String, Object> map = new HashMap<>(3);
                map.put("command", Constant.Command.BATCH_VOLUME_INFO);
                map.put("taskId", UUID.randomUUID().toString());
                map.put("data", GsonBuilderUtil.create().toJson(requests));
                HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
                try {
                    String uri = String.format("%s/api/operate", host.getUri());
                    String response = HttpUtil.post(uri, map);
                    ResultUtil<List<VolumeInfo>> resultUtil = GsonBuilderUtil.create().fromJson(response, new TypeToken<ResultUtil<List<VolumeInfo>>>() {
                    }.getType());
                    List<VolumeInfo> volumeInfoList = resultUtil.getData();
                    for (int i = 0; i < volumeInfoList.size(); i++) {
                        VolumeInfo info = volumeInfoList.get(i);
                        if (info == null) {
                            continue;
                        }
                        VolumeEntity updateVolume = VolumeEntity.builder()
                                .volumeId(volumeList.get(i).getVolumeId())
                                .capacity(info.getCapacity())
                                .allocation(info.getAllocation())
                                .build();
                        this.volumeMapper.updateById(updateVolume);
                    }

                } catch (Exception err) {
                    log.error("检测磁盘失败.hostId={} storageId={}", host.getHostId(), storage.getStorageId(), err);
                }

            }
        }
    }

}
