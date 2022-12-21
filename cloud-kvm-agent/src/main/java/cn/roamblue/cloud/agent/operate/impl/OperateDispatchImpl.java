package cn.roamblue.cloud.agent.operate.impl;

import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.agent.config.ApplicationConfig;
import cn.roamblue.cloud.agent.operate.*;
import cn.roamblue.cloud.agent.service.ConnectPool;
import cn.roamblue.cloud.agent.util.HostUtil;
import cn.roamblue.cloud.common.bean.*;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.AppUtils;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateDispatchImpl implements OperateDispatch {
    private final ConcurrentHashMap<String, Long> taskMap = new ConcurrentHashMap<>();
    @Autowired
    private ConnectPool connectPool;
    @Autowired
    private NetworkOperate networkOperate;
    @Autowired
    private StorageOperate storageOperate;
    @Autowired
    private VolumeOperate volumeOperate;
    @Autowired
    private OsOperate osOperate;
    @Autowired
    private HostOperate hostOperate;
    @Autowired
    private ApplicationConfig config;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private HostUtil hostUtil;

    private void submitTask(String taskId, String command, String data) {
        taskMap.put(taskId, System.currentTimeMillis());
        this.executor.submit(() -> {
            ResultUtil result = null;
            try {
                result = dispatch(taskId, command, data);
            } catch (CodeException err) {
                result = ResultUtil.error(err.getCode(), err.getMessage());
            } catch (Exception err) {
                result = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
                log.error("执行任务出错.", err);
            } finally {
                taskMap.remove(taskId);
                String nonce = String.valueOf(System.currentTimeMillis());
                Map<String, Object> map = new HashMap<>(5);
                map.put("taskId", taskId);
                map.put("clientId", hostUtil.getClientId());
                map.put("data", GsonBuilderUtil.create().toJson(result));
                map.put("nonce", nonce);
                String sign = null;
                try {
                    sign = AppUtils.sign(map, hostUtil.getClientId(), hostUtil.getClientSecret(), nonce);
                } catch (Exception err) {

                }
                map.put("sign", sign);
                HttpUtil.post(this.config.getManagerUri() + "/api/task/report", map);
            }
        });
    }

    @Override
    public <T> ResultUtil<T> dispatch(String taskId, String command, String data) {
        this.taskMap.put(taskId, System.currentTimeMillis());
        Connect connect = null;
        try {
            T result = null;
            connect = connectPool.borrowObject();
            switch (command) {
                case Constant.Command.CHECK_TASK:
                    result = (T) taskMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
                    break;

                case Constant.Command.SUBMIT_TASK:
                    TaskRequest taskRequest = GsonBuilderUtil.create().fromJson(data, TaskRequest.class);
                    this.submitTask(taskRequest.getTaskId(), taskRequest.getCommand(), taskRequest.getData());
                    break;

                case Constant.Command.HOST_INFO:
                    result = (T) hostOperate.getHostInfo(connect);
                    break;

                case Constant.Command.HOST_INIT:
                    result = (T) hostOperate.initHost(connect, GsonBuilderUtil.create().fromJson(data, InitHostRequest.class));
                    break;

                case Constant.Command.NETWORK_CREATE_BASIC:
                    networkOperate.createBasic(connect, GsonBuilderUtil.create().fromJson(data, BasicBridgeNetwork.class));
                    break;
                case Constant.Command.NETWORK_CREATE_VLAN:
                    networkOperate.createVlan(connect, GsonBuilderUtil.create().fromJson(data, VlanNetwork.class));
                    break;
                case Constant.Command.NETWORK_DESTROY_BASIC:
                    networkOperate.destroyBasic(connect, GsonBuilderUtil.create().fromJson(data, BasicBridgeNetwork.class));
                    break;
                case Constant.Command.NETWORK_DESTROY_VLAN:
                    networkOperate.destroyVlan(connect, GsonBuilderUtil.create().fromJson(data, VlanNetwork.class));
                    break;
                case Constant.Command.STORAGE_INFO:
                    result = (T) storageOperate.getStorageInfo(connect, GsonBuilderUtil.create().fromJson(data, StorageInfoRequest.class));
                    break;
                case Constant.Command.BATCH_STORAGE_INFO:
                    result = (T) storageOperate.batchStorageInfo(connect, GsonBuilderUtil.create().fromJson(data, new TypeToken<List<StorageInfoRequest>>() {
                    }.getType()));
                    break;
                case Constant.Command.STORAGE_CREATE:
                    result = (T) storageOperate.create(connect, GsonBuilderUtil.create().fromJson(data, StorageCreateRequest.class));
                    break;
                case Constant.Command.STORAGE_DESTROY:
                    storageOperate.destroy(connect, GsonBuilderUtil.create().fromJson(data, StorageDestroyRequest.class));
                    break;
                case Constant.Command.VOLUME_INFO:
                    result = (T) this.volumeOperate.getInfo(connect, GsonBuilderUtil.create().fromJson(data, VolumeInfoRequest.class));
                    break;
                case Constant.Command.BATCH_VOLUME_INFO:
                    result = (T) this.volumeOperate.batchInfo(connect, GsonBuilderUtil.create().fromJson(data, new TypeToken<List<VolumeInfoRequest>>() {
                    }.getType()));
                    break;

                case Constant.Command.VOLUME_CREATE:
                    result = (T) this.volumeOperate.create(connect, GsonBuilderUtil.create().fromJson(data, VolumeCreateRequest.class));
                    break;
                case Constant.Command.VOLUME_DESTROY:
                    this.volumeOperate.destroy(connect, GsonBuilderUtil.create().fromJson(data, VolumeDestroyRequest.class));
                    break;
                case Constant.Command.VOLUME_RESIZE:
                    result = (T) this.volumeOperate.resize(connect, GsonBuilderUtil.create().fromJson(data, VolumeResizeRequest.class));
                    break;
                case Constant.Command.VOLUME_CLONE:
                    result = (T) this.volumeOperate.clone(connect, GsonBuilderUtil.create().fromJson(data, VolumeCloneRequest.class));
                    break;
                case Constant.Command.VOLUME_MIGRATE:
                    result = (T) this.volumeOperate.migrate(connect, GsonBuilderUtil.create().fromJson(data, VolumeMigrateRequest.class));
                    break;
                case Constant.Command.VOLUME_SNAPSHOT:
                    result = (T) this.volumeOperate.snapshot(connect, GsonBuilderUtil.create().fromJson(data, VolumeCreateSnapshotRequest.class));
                    break;
                case Constant.Command.VOLUME_TEMPLATE:
                    result = (T) this.volumeOperate.template(connect, GsonBuilderUtil.create().fromJson(data, VolumeCreateTemplateRequest.class));
                    break;
                case Constant.Command.VOLUME_DOWNLOAD:
                    result = (T) this.volumeOperate.download(connect, GsonBuilderUtil.create().fromJson(data, VolumeDownloadRequest.class));
                    break;
                case Constant.Command.GUEST_DESTROY:
                    this.osOperate.destroy(connect, GsonBuilderUtil.create().fromJson(data, GuestDestroyRequest.class));
                    break;
                case Constant.Command.GUEST_INFO:
                    result = (T) this.osOperate.getGustInfo(connect, GsonBuilderUtil.create().fromJson(data, GuestInfoRequest.class));
                    break;
                case Constant.Command.ALL_GUEST_INFO:
                    result = (T) this.osOperate.listAllGuestInfo(connect);
                    break;
                case Constant.Command.BATCH_GUEST_INFO:
                    result = (T) this.osOperate.batchGustInfo(connect, GsonBuilderUtil.create().fromJson(data, new TypeToken<List<GuestInfoRequest>>() {
                    }.getType()));
                    break;
                case Constant.Command.GUEST_START:
                    result = (T) this.osOperate.start(connect, GsonBuilderUtil.create().fromJson(data, GuestStartRequest.class));
                    break;
                case Constant.Command.GUEST_REBOOT:
                    this.osOperate.reboot(connect, GsonBuilderUtil.create().fromJson(data, GuestRebootRequest.class));
                    break;
                case Constant.Command.GUEST_SHUTDOWN:
                    this.osOperate.shutdown(connect, GsonBuilderUtil.create().fromJson(data, GuestShutdownRequest.class));
                    break;
                case Constant.Command.GUEST_ATTACH_CD_ROOM:
                    this.osOperate.attachCdRoom(connect, GsonBuilderUtil.create().fromJson(data, OsCdRoom.class));
                    break;
                case Constant.Command.GUEST_DETACH_CD_ROOM:
                    this.osOperate.detachCdRoom(connect, GsonBuilderUtil.create().fromJson(data, OsCdRoom.class));
                    break;
                case Constant.Command.GUEST_ATTACH_DISK:
                    this.osOperate.attachDisk(connect, GsonBuilderUtil.create().fromJson(data, OsDisk.class));
                    break;
                case Constant.Command.GUEST_DETACH_DISK:
                    this.osOperate.detachDisk(connect, GsonBuilderUtil.create().fromJson(data, OsDisk.class));
                    break;
                case Constant.Command.GUEST_ATTACH_NIC:
                    this.osOperate.attachNic(connect, GsonBuilderUtil.create().fromJson(data, OsNic.class));
                    break;
                case Constant.Command.GUEST_DETACH_NIC:
                    this.osOperate.detachNic(connect, GsonBuilderUtil.create().fromJson(data, OsNic.class));
                    break;
                case Constant.Command.GUEST_QMA:
                    this.osOperate.qma(connect, GsonBuilderUtil.create().fromJson(data, GuestQmaRequest.class));
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的操作:" + command);
            }
            return ResultUtil.<T>builder().data(result).build();
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        } finally {
            if (connect != null) {
                connectPool.returnObject(connect);
            }
            this.taskMap.remove(taskId);
        }
    }
}
