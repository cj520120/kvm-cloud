package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.StorageInfo;
import cn.roamblue.cloud.management.ui.StorageUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 存储池管理
 *
 * @author chenjun
 */
@RestController
public class StoragePoolController {
    @Autowired
    private StorageUiService storageUiService;

    /**
     * 获取存储池列表
     *
     * @return
     */
    @Login
    @GetMapping("/management/storage")
    public ResultUtil<List<StorageInfo>> listStorages() {
        return storageUiService.listStorage();
    }

    /**
     * 根据集群获取存储池列表
     *
     * @param clusterId
     * @return
     */
    @Login
    @GetMapping("/management/storage/search")
    public ResultUtil<List<StorageInfo>> search(@RequestParam("clusterId") int clusterId) {
        return storageUiService.search(clusterId);
    }

    /**
     * 获取存储池信息
     *
     * @param id
     * @return
     */
    @Login
    @GetMapping("/management/storage/info")
    public ResultUtil<StorageInfo> findStorageById(@RequestParam("id") int id) {
        return storageUiService.findStorageById(id);
    }

    /**
     * 创建存储池
     *
     * @param clusterId 集群ID
     * @param name      存储池名称
     * @param uri       存储池地址
     * @param source    存储池来源地址
     * @return
     */
    @Login
    @PostMapping("/management/storage/create")
    public ResultUtil<StorageInfo> createStorage(
            @RequestParam("clusterId") int clusterId,
            @RequestParam("name") String name,
            @RequestParam("uri") String uri,
            @RequestParam("source") String source) {
        return storageUiService.createStorage(clusterId, name, uri, source);
    }

    /**
     * 销毁存储池
     *
     * @param id
     * @return
     */
    @Login
    @PostMapping("/management/storage/destroy")
    public ResultUtil<Void> destroyStorageById(@RequestParam("id") int id) {
        return storageUiService.destroyStorageById(id);
    }


}
