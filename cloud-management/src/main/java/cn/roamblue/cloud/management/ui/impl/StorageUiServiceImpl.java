package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Rule;
import cn.roamblue.cloud.management.bean.StorageInfo;
import cn.roamblue.cloud.management.service.StorageService;
import cn.roamblue.cloud.management.ui.StorageUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author chenjun
 */
@Service
public class StorageUiServiceImpl extends AbstractUiService implements StorageUiService {
    @Autowired
    private StorageService storagePoolService;

    @Override
    public ResultUtil<List<StorageInfo>> listStorage() {
        return super.call(() -> storagePoolService.listStorage());
    }

    @Override
    public ResultUtil<List<StorageInfo>> search(int clusterId) {
        return super.call(() -> storagePoolService.search(clusterId));
    }

    @Override
    public ResultUtil<StorageInfo> findStorageById(int id) {
        return super.call(() -> storagePoolService.findStorageById(id));
    }

    @Override
    @Rule(permissions = "storage.create")
    public ResultUtil<StorageInfo> createStorage(int clusterId, String name, String uri, String source) {
        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "名称不能为空");
        }
        if (StringUtils.isEmpty(uri)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "存储池地址不能为空");
        }
        if (StringUtils.isEmpty(source)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "存储挂载地址不能为空");
        }
        if (clusterId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "集群不能为空");
        }
        return super.call(() -> storagePoolService.createStorage(clusterId, name, uri, source));
    }

    @Override
    @Rule(permissions = "storage.destroy")
    public ResultUtil<Void> destroyStorageById(int id) {
        return super.call(() -> storagePoolService.destroyStorageById(id));
    }


}
