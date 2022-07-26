package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.PreAuthority;
import cn.roamblue.cloud.management.bean.VolumeInfo;
import cn.roamblue.cloud.management.service.LockService;
import cn.roamblue.cloud.management.service.VolumeService;
import cn.roamblue.cloud.management.ui.VolumeUiService;
import cn.roamblue.cloud.management.util.LockKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Service
public class VolumeUiServiceImpl extends AbstractUiService implements VolumeUiService {
    @Autowired
    private VolumeService volumeService;
    @Autowired
    private LockService lockService;

    @Override
    public ResultUtil<List<VolumeInfo>> listVolume() {
        return super.call(() -> volumeService.listVolume());
    }

    @Override
    public ResultUtil<List<VolumeInfo>> search(int clusterId, int storageId, int vmId) {
        return super.call(() -> volumeService.search(clusterId, storageId, vmId));
    }


    @Override
    public ResultUtil<VolumeInfo> findVolumeById(int id) {
        return super.call(() -> volumeService.findVolumeById(id));
    }

    @Override
    @PreAuthority(value = "hasAuthority('volume.create')")
    public ResultUtil<VolumeInfo> createVolume(int clusterId, int storageId, String name, long size) {
        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "磁盘名称不能为空");
        }
        if (clusterId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "集群不能为空");
        }
        if (size <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "磁盘大小不能小于1G");
        }
        return super.call(() -> volumeService.createVolume(clusterId, null, storageId, name, size));
    }

    @PreAuthority(value = "hasAuthority('volume.destroy')")
    @Override
    public ResultUtil<VolumeInfo> destroyVolumeById(int id) {
        return lockService.run(LockKeyUtil.getVolumeLockKey(id), () -> this.call(() -> volumeService.destroyVolumeById(id)), 1, TimeUnit.MINUTES);
    }

    @PreAuthority(value = "hasAuthority('volume.resume')")
    @Override
    public ResultUtil<VolumeInfo> resume(int id) {
        return lockService.run(LockKeyUtil.getVolumeLockKey(id), () -> super.call(() -> volumeService.resume(id)), 1, TimeUnit.MINUTES);
    }

    @PreAuthority(value = "hasAuthority('volume.resize')")
    @Override
    public ResultUtil<VolumeInfo> resize(int id, long size) {
        if (size <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR,  "磁盘大小不能小于1G");
        }
        return lockService.run(LockKeyUtil.getVolumeLockKey(id), () -> super.call(() -> volumeService.resize(id, size)), 1, TimeUnit.MINUTES);
    }
}
