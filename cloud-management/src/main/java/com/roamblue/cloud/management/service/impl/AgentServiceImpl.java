package com.roamblue.cloud.management.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.roamblue.cloud.common.agent.*;
import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.service.AgentService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@Component
public class AgentServiceImpl implements AgentService {

    @Override
    public ResultUtil<HostModel> getHostInfo(String uri) {
        return this.call(() -> {
            Gson gson = new Gson();
            ResultUtil<HostModel> resultUtil = gson.fromJson(HttpUtil.get(uri + "/host/info"), new TypeToken<ResultUtil<HostModel>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<List<VmInfoModel>> getInstance(String uri) {
        return this.call(() -> {
            Gson gson = new Gson();
            ResultUtil<List<VmInfoModel>> resultUtil = gson.fromJson(HttpUtil.get(uri + "/vm/list"), new TypeToken<ResultUtil<List<VmInfoModel>>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<List<StorageModel>> getHostStorage(String uri) {
        return this.call(() -> {
            Gson gson = new Gson();
            ResultUtil<List<StorageModel>> resultUtil = gson.fromJson(HttpUtil.get(uri + "/storage/list"), new TypeToken<ResultUtil<List<StorageModel>>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<StorageModel> addHostStorage(String uri, String host, String source, String target) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(4);
            map.put("name", target);
            map.put("nfs", host);
            map.put("path", source);
            map.put("target", "/mnt/" + target);

            ResultUtil<StorageModel> resultUtil = gson.fromJson(HttpUtil.post(uri + "/storage/create", map), new TypeToken<ResultUtil<StorageModel>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<VolumeModel> createVolume(String uri, String storage, String volume, String backingVolume, long size) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(5);
            map.put("storageName", storage);
            map.put("volumeName", volume);
            map.put("path", volume);
            map.put("capacity", size);
            if (!StringUtils.isEmpty(backingVolume)) {
                map.put("backingVolume", backingVolume);
            } else {
                map.put("backingVolume", "");
            }
            ResultUtil<VolumeModel> resultUtil = gson.fromJson(HttpUtil.post(uri + "/volume/create", map), new TypeToken<ResultUtil<VolumeModel>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<VolumeModel> resize(String uri, String storageTarget, String volumeTarget, long size) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(3);
            map.put("storageName", storageTarget);
            map.put("volumeName", volumeTarget);
            map.put("size", size);
            ResultUtil<VolumeModel> resultUtil = gson.fromJson(HttpUtil.post(uri + "/volume/resize", map), new TypeToken<ResultUtil<VolumeModel>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<Void> destroyVolume(String uri, String storage, String volume) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(2);
            map.put("storageName", storage);
            map.put("volumeName", volume);
            ResultUtil<Void> resultUtil = gson.fromJson(HttpUtil.post(uri + "/volume/destroy", map), new TypeToken<ResultUtil<Void>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<Void> destroyStorage(String uri, String storage) {
        return this.call(() -> {
            Map<String, Object> map = new HashMap<>(1);
            map.put("name", storage);
            Gson gson = new Gson();
            ResultUtil<Void> resultUtil = gson.fromJson(HttpUtil.post(uri + "/storage/destroy", map), new TypeToken<ResultUtil<Void>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<Void> destroyVm(String uri, String vm) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(1);
            map.put("name", vm);
            ResultUtil<Void> resultUtil = gson.fromJson(HttpUtil.post(uri + "/vm/destroy", map), new TypeToken<ResultUtil<Void>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<Void> stopVm(String uri, String vm) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(1);
            map.put("name", vm);
            ResultUtil<Void> resultUtil = gson.fromJson(HttpUtil.post(uri + "/vm/stop", map), new TypeToken<ResultUtil<Void>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<Void> rebootVm(String uri, String vm) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(1);
            map.put("name", vm);
            ResultUtil<Void> resultUtil = gson.fromJson(HttpUtil.post(uri + "/vm/restart", map), new TypeToken<ResultUtil<Void>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<Void> writeFile(String uri, String vm, String path, String body) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(3);
            map.put("name", vm);
            map.put("path", path);
            map.put("body", body);
            ResultUtil<Void> resultUtil = gson.fromJson(HttpUtil.post(uri + "/vm/command/write/file", map), new TypeToken<ResultUtil<Void>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<Map<String, Object>> execute(String uri, String vm, String command) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(3);
            map.put("name", vm);
            map.put("command", command);
            map.put("timeout", 10);
            ResultUtil<Map<String, Object>> resultUtil = gson.fromJson(HttpUtil.post(uri + "/vm/command/execute", map), new TypeToken<ResultUtil<Map<String, Object>>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<Void> changeCdRoom(String uri, String vm, String path) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, String> header = new HashMap<>(3);
            HttpRequest request = HttpUtil.createPost(uri + "/vm/update/cdroom");
            request.addHeaders(header);
            VmModel.UpdateCdRoom kvm = new VmModel.UpdateCdRoom();
            kvm.setName(vm);
            kvm.setPath(path);
            request.body(gson.toJson(kvm));
            ResultUtil<Void> resultUtil = gson.fromJson(request.execute().body(), new TypeToken<ResultUtil<Void>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<Void> attachDisk(String uri, String vm, VmModel.Disk disk, boolean attach) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, String> header = new HashMap<>(0);
            HttpRequest request = HttpUtil.createPost(uri + "/vm/update/disk");
            request.addHeaders(header);
            VmModel.UpdateDisk kvm = new VmModel.UpdateDisk();
            kvm.setName(vm);
            kvm.setDisk(disk);
            kvm.setAttach(attach);
            request.body(gson.toJson(kvm));
            ResultUtil<Void> resultUtil = gson.fromJson(request.execute().body(), new TypeToken<ResultUtil<Void>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<VmInfoModel> startVm(String uri, VmModel kvm) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, String> header = new HashMap<>(0);
            HttpRequest request = HttpUtil.createPost(uri + "/vm/start");
            request.addHeaders(header);
            request.body(gson.toJson(kvm));
            ResultUtil<VmInfoModel> resultUtil = gson.fromJson(request.execute().body(), new TypeToken<ResultUtil<VmInfoModel>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<VolumeModel> cloneVolume(String uri, String sourceStorage, String sourceVolume, String targetStorage, String targetVolume, String targetPath) {

        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(5);
            map.put("sourceStorage", sourceStorage);
            map.put("sourceVolume", sourceVolume);
            map.put("targetStorage", targetStorage);
            map.put("targetVolume", targetVolume);
            map.put("targetPath", targetPath);
            ResultUtil<VolumeModel> resultUtil = gson.fromJson(HttpUtil.post(uri + "/volume/clone", map), new TypeToken<ResultUtil<VolumeModel>>() {
            }.getType());
            return resultUtil;
        });

    }

    @Override
    public ResultUtil<VolumeModel> getVolumeInfo(String uri, String storageName, String volumeName) {
        return this.call(() -> {

            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(2);
            map.put("storageName", storageName);
            map.put("volumeName", volumeName);
            ResultUtil<VolumeModel> resultUtil = gson.fromJson(HttpUtil.get(uri + "/volume/info", map), new TypeToken<ResultUtil<VolumeModel>>() {
            }.getType());
            return resultUtil;
        });
    }

    @Override
    public ResultUtil<List<VmStaticsModel>> listVmStatics(String uri) {
        return this.call(() -> {
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<>(0);
            ResultUtil<List<VmStaticsModel>> resultUtil = gson.fromJson(HttpUtil.get(uri + "/vm/list/statics", map), new TypeToken<ResultUtil<List<VmStaticsModel>>>() {
            }.getType());
            return resultUtil;
        });
    }

    private <T extends ResultUtil> T call(AgentCall<T> callable) {
        try {
            return callable.call();
        } catch (Exception err) {
            return (T) ResultUtil.builder().code(ErrorCode.SERVER_ERROR).message(err.getMessage()).build();
        }

    }

    @FunctionalInterface
    public interface AgentCall<T extends ResultUtil> {
        /**
         * 执行agent操作
         *
         * @return
         */
        T call();
    }
}
