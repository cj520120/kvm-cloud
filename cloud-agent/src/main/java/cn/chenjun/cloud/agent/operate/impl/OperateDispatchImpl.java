package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.*;
import cn.chenjun.cloud.agent.operate.bean.Consumer;
import cn.chenjun.cloud.agent.operate.bean.Dispatch;
import cn.chenjun.cloud.agent.service.ConnectPool;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.http.HttpUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateDispatchImpl implements OperateDispatch {
    private final ConcurrentHashMap<String, Long> taskMap = new ConcurrentHashMap<>();
    private final Map<String, Dispatch<?, ?>> dispatchMap = new HashMap<>();
    @Autowired
    private ConnectPool connectPool;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private ClientService clientService;

    public OperateDispatchImpl(@Autowired NetworkOperate networkOperate,
                               @Autowired StorageOperate storageOperate,
                               @Autowired VolumeOperate volumeOperate,
                               @Autowired OsOperate osOperate,
                               @Autowired HostOperate hostOperate) {
        dispatchMap.put(Constant.Command.HOST_INFO, Dispatch.<NoneRequest, HostInfo>builder().paramType(NoneRequest.class).consumer(hostOperate::getHostInfo).build());
        dispatchMap.put(Constant.Command.HOST_INIT, Dispatch.<InitHostRequest, HostInfo>builder().paramType(InitHostRequest.class).consumer(hostOperate::initHost).build());
        dispatchMap.put(Constant.Command.NETWORK_CREATE_BASIC, Dispatch.<BasicBridgeNetwork, Void>builder().paramType(BasicBridgeNetwork.class).consumer(networkOperate::createBasic).build());
        dispatchMap.put(Constant.Command.NETWORK_CREATE_VLAN, Dispatch.<VlanNetwork, Void>builder().paramType(VlanNetwork.class).consumer(networkOperate::createVlan).build());
        dispatchMap.put(Constant.Command.NETWORK_DESTROY_BASIC, Dispatch.<BasicBridgeNetwork, Void>builder().paramType(BasicBridgeNetwork.class).consumer(networkOperate::destroyBasic).build());
        dispatchMap.put(Constant.Command.NETWORK_DESTROY_VLAN, Dispatch.<VlanNetwork, Void>builder().paramType(VlanNetwork.class).consumer(networkOperate::destroyVlan).build());
        dispatchMap.put(Constant.Command.STORAGE_INFO, Dispatch.<StorageInfoRequest, StorageInfo>builder().paramType(StorageInfoRequest.class).consumer(storageOperate::getStorageInfo).build());
        dispatchMap.put(Constant.Command.BATCH_STORAGE_INFO, Dispatch.<List<StorageInfoRequest>, List<StorageInfo>>builder().paramType(new TypeToken<List<StorageInfoRequest>>() {
        }.getType()).consumer(storageOperate::batchStorageInfo).build());
        dispatchMap.put(Constant.Command.STORAGE_CREATE, Dispatch.<StorageCreateRequest, StorageInfo>builder().paramType(StorageCreateRequest.class).consumer(storageOperate::create).build());
        dispatchMap.put(Constant.Command.STORAGE_DESTROY, Dispatch.<StorageDestroyRequest, Void>builder().paramType(StorageCreateRequest.class).consumer(storageOperate::destroy).build());
        dispatchMap.put(Constant.Command.VOLUME_INFO, Dispatch.<VolumeInfoRequest, VolumeInfo>builder().paramType(VolumeInfoRequest.class).consumer(volumeOperate::getInfo).build());
        dispatchMap.put(Constant.Command.BATCH_VOLUME_INFO, Dispatch.<List<VolumeInfoRequest>, List<VolumeInfo>>builder().paramType(new TypeToken<List<VolumeInfoRequest>>() {
        }.getType()).consumer(volumeOperate::batchInfo).build());
        dispatchMap.put(Constant.Command.VOLUME_CREATE, Dispatch.<VolumeCreateRequest, VolumeInfo>builder().paramType(VolumeCreateRequest.class).consumer(volumeOperate::create).build());
        dispatchMap.put(Constant.Command.VOLUME_DESTROY, Dispatch.<VolumeDestroyRequest, Void>builder().paramType(VolumeDestroyRequest.class).consumer(volumeOperate::destroy).build());
        dispatchMap.put(Constant.Command.VOLUME_RESIZE, Dispatch.<VolumeResizeRequest, VolumeInfo>builder().paramType(VolumeDestroyRequest.class).consumer(volumeOperate::resize).build());
        dispatchMap.put(Constant.Command.VOLUME_CLONE, Dispatch.<VolumeCloneRequest, VolumeInfo>builder().paramType(VolumeCloneRequest.class).consumer(volumeOperate::clone).build());
        dispatchMap.put(Constant.Command.VOLUME_MIGRATE, Dispatch.<VolumeMigrateRequest, VolumeInfo>builder().paramType(VolumeMigrateRequest.class).consumer(volumeOperate::migrate).build());
        dispatchMap.put(Constant.Command.VOLUME_SNAPSHOT, Dispatch.<VolumeCreateSnapshotRequest, VolumeInfo>builder().paramType(VolumeCreateSnapshotRequest.class).consumer(volumeOperate::snapshot).build());
        dispatchMap.put(Constant.Command.VOLUME_TEMPLATE, Dispatch.<VolumeCreateTemplateRequest, VolumeInfo>builder().paramType(VolumeCreateTemplateRequest.class).consumer(volumeOperate::template).build());
        dispatchMap.put(Constant.Command.VOLUME_DOWNLOAD, Dispatch.<VolumeDownloadRequest, VolumeInfo>builder().paramType(VolumeDownloadRequest.class).consumer(volumeOperate::download).build());
        dispatchMap.put(Constant.Command.GUEST_DESTROY, Dispatch.<GuestDestroyRequest, Void>builder().paramType(GuestDestroyRequest.class).consumer(osOperate::destroy).build());
        dispatchMap.put(Constant.Command.GUEST_INFO, Dispatch.<GuestInfoRequest, GuestInfo>builder().paramType(GuestInfoRequest.class).consumer(osOperate::getGustInfo).build());
        dispatchMap.put(Constant.Command.ALL_GUEST_INFO, Dispatch.<NoneRequest, List<GuestInfo>>builder().paramType(NoneRequest.class).consumer(osOperate::listAllGuestInfo).build());
        dispatchMap.put(Constant.Command.BATCH_GUEST_INFO, Dispatch.<List<GuestInfoRequest>, List<GuestInfo>>builder().paramType(new TypeToken<List<GuestInfoRequest>>() {
        }.getType()).consumer(osOperate::batchGustInfo).build());
        dispatchMap.put(Constant.Command.GUEST_START, Dispatch.<GuestStartRequest, GuestInfo>builder().paramType(GuestStartRequest.class).consumer(osOperate::start).build());
        dispatchMap.put(Constant.Command.GUEST_REBOOT, Dispatch.<GuestRebootRequest, Void>builder().paramType(GuestRebootRequest.class).consumer(osOperate::reboot).build());
        dispatchMap.put(Constant.Command.GUEST_SHUTDOWN, Dispatch.<GuestShutdownRequest, Void>builder().paramType(GuestShutdownRequest.class).consumer(osOperate::shutdown).build());
        dispatchMap.put(Constant.Command.GUEST_ATTACH_CD_ROOM, Dispatch.<OsCdRoom, Void>builder().paramType(OsCdRoom.class).consumer(osOperate::attachCdRoom).build());
        dispatchMap.put(Constant.Command.GUEST_DETACH_CD_ROOM, Dispatch.<OsCdRoom, Void>builder().paramType(OsCdRoom.class).consumer(osOperate::detachCdRoom).build());
        dispatchMap.put(Constant.Command.GUEST_ATTACH_DISK, Dispatch.<OsDisk, Void>builder().paramType(OsDisk.class).consumer(osOperate::attachDisk).build());
        dispatchMap.put(Constant.Command.GUEST_DETACH_DISK, Dispatch.<OsDisk, Void>builder().paramType(OsDisk.class).consumer(osOperate::detachDisk).build());
        dispatchMap.put(Constant.Command.GUEST_ATTACH_NIC, Dispatch.<OsNic, Void>builder().paramType(OsNic.class).consumer(osOperate::attachNic).build());
        dispatchMap.put(Constant.Command.GUEST_DETACH_NIC, Dispatch.<OsNic, Void>builder().paramType(OsNic.class).consumer(osOperate::detachNic).build());
        dispatchMap.put(Constant.Command.GUEST_QMA, Dispatch.<GuestQmaRequest, Void>builder().paramType(GuestQmaRequest.class).consumer(osOperate::qma).build());
        dispatchMap.put(Constant.Command.GUEST_MIGRATE, Dispatch.<GuestMigrateRequest, Void>builder().paramType(GuestMigrateRequest.class).consumer(osOperate::migrate).build());
    }

    private void submitTask(String taskId, String command, String data) {
        taskMap.put(taskId, System.currentTimeMillis());
        log.info("提交异步任务:taskId={},command={},data={}", taskId, command, data);
        this.executor.submit(() -> {
            ResultUtil<?> result = null;
            try {
                result = dispatch(taskId, command, data);
            } catch (CodeException err) {
                result = ResultUtil.error(err.getCode(), err.getMessage());
            } catch (Exception err) {
                result = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
                log.error("执行任务出错.", err);
            } finally {
                try {
                    String nonce = String.valueOf(System.nanoTime());
                    Map<String, Object> map = new HashMap<>(5);
                    map.put("taskId", taskId);
                    map.put("data", GsonBuilderUtil.create().toJson(result));
                    map.put("timestamp", System.currentTimeMillis());
                    String sign = AppUtils.sign(map, clientService.getClientId(), clientService.getClientSecret(), nonce);
                    map.put("sign", sign);
                    HttpUtil.post(clientService.getManagerUri() + "api/agent/task/report", map);
                } catch (Exception err) {
                    log.error("上报任务出现异常。command={} param={} result={}", command, data, result, err);
                } finally {
                    taskMap.remove(taskId);
                    log.info("移除异步任务:{}", taskId);
                }

            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ResultUtil<?> dispatch(String taskId, String command, String data) {

        this.taskMap.put(taskId, System.currentTimeMillis());
        Connect connect = null;
        try {
            Object result = null;
            connect = connectPool.borrowObject();
            switch (command) {
                case Constant.Command.CHECK_TASK:
                    result = new ArrayList<>(taskMap.keySet());
                    break;

                case Constant.Command.SUBMIT_TASK:
                    TaskRequest taskRequest = GsonBuilderUtil.create().fromJson(data, TaskRequest.class);
                    this.submitTask(taskRequest.getTaskId(), taskRequest.getCommand(), taskRequest.getData());
                    break;
                default:
                    Dispatch<?, ?> dispatch = this.dispatchMap.get(command);
                    if (dispatch == null) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的操作:" + command);
                    }
                    Consumer consumer = dispatch.getConsumer();
                    Object param = StringUtils.isEmpty(data) ? null : GsonBuilderUtil.create().fromJson(data, dispatch.getParamType());
                    result = consumer.dispatch(connect, param);
                    break;

            }
            return ResultUtil.builder().data(result).build();
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            log.error("dispatch fail. taskId={} command={} data={}", taskId, command, data, err);
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        } finally {
            this.taskMap.remove(taskId);
            if (connect != null) {
                connectPool.returnObject(connect);
            }
        }
    }


}
