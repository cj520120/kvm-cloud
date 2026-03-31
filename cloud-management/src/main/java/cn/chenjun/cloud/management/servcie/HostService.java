package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.CreateHostOperate;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * @author chenjun
 */
@Service
public class HostService extends AbstractHostStorageService {

    public List<HostEntity> listAllHost() {
        List<HostEntity> hostList = this.hostDao.listAll();
        return hostList;
    }

    public Page<HostEntity> search(String keyword, int no, int size) {
        Page<HostEntity> page = this.hostDao.search(keyword, no, size);
        return page;
    }

    public HostEntity getHostInfo(int hostId) {
        HostEntity host = this.hostDao.findById(hostId);
        if (host == null) {
            throw new CodeException(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        }
        return host;
    }

    public HostEntity getHostByClientId(String clientId) {
        HostEntity host = this.hostDao.findByClientId(clientId);
        if (host == null) {
            throw new CodeException(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        }
        return host;
    }

    public HostEntity getHostInfoByClientId(String clientId) {
        HostEntity host = this.hostDao.findByClientId(clientId);
        if (host == null) {
            throw new CodeException(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        }
        return host;
    }

    public HostEntity createHost(String name, String ip, String uri, String nic, int role) {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入主机名称");
        }
        if (StringUtils.isEmpty(ip)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的主机ip");
        }
        if (StringUtils.isEmpty(uri)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的主机通信地址");
        }
        if (StringUtils.isEmpty(nic)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入主机网卡名称");
        }
        String clientId = AppUtils.getAppId();
        String clientSecret = AppUtils.getAppSecret(clientId);
        HostEntity host = HostEntity.builder().hostIp(ip).displayName(name)
                .uri(uri).arch("").emulator("").nic(nic).hypervisor("")
                .osName("")
                .osVersion("")
                .vendor("")
                .model("")
                .frequency(0L)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .totalMemory(0L)
                .allocationMemory(0L)
                .totalCpu(0)
                .allocationCpu(0)
                .sockets(0)
                .threads(0)
                .cores(0)
                .role(role)
                .status(Constant.HostStatus.REGISTER).build();
        this.hostDao.insert(host);
        BaseOperateParam operateParam = CreateHostOperate.builder().hostId(host.getHostId()).id(UUID.randomUUID().toString())
                .title("添加主机[" + host.getDisplayName() + "]")
                .build();
        this.operateTask.addTask(operateParam);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(host.getHostId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_HOST).build());
        return host;
    }

    @Transactional(rollbackFor = Exception.class)
    public HostEntity registerHost(int hostId) {
        HostEntity host = this.getHostInfo(hostId);
        host.setStatus(Constant.HostStatus.REGISTER);
        if (StringUtils.isEmpty(host.getClientId())) {
            host.setClientId(AppUtils.getAppId());
            host.setClientSecret(AppUtils.getAppSecret(host.getClientId()));
        }
        this.hostDao.update(host);
        List<StorageEntity> storageList = this.storageDao.listLocalStorage();
        for (StorageEntity storageEntity : storageList) {
            this.checkAndInitHostLocalStorage(storageEntity, host);
        }
        BaseOperateParam operateParam = CreateHostOperate.builder().hostId(host.getHostId()).id(UUID.randomUUID().toString())
                .title("注册主机[" + host.getDisplayName() + "]").build();
        this.operateTask.addTask(operateParam);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(host.getHostId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_HOST).build());
        return host;
    }

    @Transactional(rollbackFor = Exception.class)
    public HostEntity maintenanceHost(int hostId) {
        HostEntity host = this.hostDao.findById(hostId);
        if (host == null) {
            throw new CodeException(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        }

        host.setStatus(Constant.HostStatus.MAINTENANCE);
        this.hostDao.update(host);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(host.getHostId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_HOST).build());
        return host;

    }
    @Transactional(rollbackFor = Exception.class)
    public HostEntity updateHostRole(int hostId, int role) {
        HostEntity host = this.hostDao.findById(hostId);
        if (host == null) {
            throw new CodeException(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        }

        host.setRole(role);
        this.hostDao.update(host);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(host.getHostId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_HOST).build());
        return host;

    }

    @Transactional(rollbackFor = Exception.class)
    public void destroyHost(int hostId) {
        HostEntity host = this.hostDao.findById(hostId);
        if (this.guestDao.countByHostId(hostId) > 0) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机有运行的虚拟机或有绑定的虚拟机，请先停止或删除虚拟机后再进行操作");
        }
        List<StorageEntity> storageList = this.storageDao.listByHostId(hostId);
        for (StorageEntity storage:storageList){
            if (this.volumeDao.countByStorageId(storage.getStorageId()) > 0) {
                throw new CodeException(ErrorCode.HOST_HAS_LOCAL_STORAGE, "该主机已经启用了本地存储池，且有磁盘占用，请先删除磁盘后再进行操作");
            }
        }
        this.hostDao.deleteById(hostId);
        this.storageDao.deleteByHostId(hostId);
        this.configService.deleteAllocateConfig(Constant.ConfigType.HOST, hostId);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(host.getHostId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_HOST).build());

    }

    public List<HostEntity> listHostByIds(List<Integer> hostIds) {
        return this.hostDao.listByIds(hostIds);
    }


}
