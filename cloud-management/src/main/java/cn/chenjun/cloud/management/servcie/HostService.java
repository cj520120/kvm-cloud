package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.model.HostModel;
import cn.chenjun.cloud.management.operate.bean.CreateHostOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class HostService extends AbstractService {

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<HostModel>> listAllHost() {
        List<HostEntity> hostList = this.hostMapper.selectList(new QueryWrapper<>());
        List<HostModel> models = hostList.stream().map(this::initHost).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    public ResultUtil<Page<HostModel>> search(String keyword, int no, int size) {
        QueryWrapper<HostEntity> wrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(keyword)) {
            String condition = "%" + keyword + "%";
            wrapper.like(HostEntity.HOST_DISPLAY_NAME, condition)
                    .or().like(HostEntity.HOST_IP, condition)
                    .or().like(HostEntity.HOST_OS_NAME, condition);

        }
        int nCount = Math.toIntExact(this.hostMapper.selectCount(wrapper));
        int nOffset = (no - 1) * size;
        wrapper.last("limit " + nOffset + ", " + size);
        List<HostEntity> list = this.hostMapper.selectList(wrapper);
        List<HostModel> models = list.stream().map(this::initHost).collect(Collectors.toList());
        Page<HostModel> page = Page.create(nCount, nOffset, size);
        page.setList(models);
        return ResultUtil.success(page);
    }
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<HostModel> getHostInfo(int hostId) {
        HostEntity host = this.hostMapper.selectById(hostId);
        if (host == null) {
            return ResultUtil.error(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        }
        return ResultUtil.success(this.initHost(host));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<HostModel> getHostInfoByClientId(String clientId) {
        HostEntity host = this.hostMapper.selectOne(new QueryWrapper<HostEntity>().eq(HostEntity.CLIENT_ID, clientId));
        if (host == null) {
            throw new CodeException(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        }
        return ResultUtil.success(this.initHost(host));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<HostModel> createHost(String name, String ip, String uri, String nic) {
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
                .status(Constant.HostStatus.REGISTER).build();
        this.hostMapper.insert(host);
        BaseOperateParam operateParam = CreateHostOperate.builder().hostId(host.getHostId()).id(UUID.randomUUID().toString())
                .title("添加主机[" + host.getDisplayName() + "]")
                .build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(host.getHostId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_HOST).build());
        return ResultUtil.success(this.initHost(host));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<HostModel> registerHost(int hostId) {
        HostEntity host = this.hostMapper.selectById(hostId);
        host.setStatus(Constant.HostStatus.REGISTER);
        if (StringUtils.isEmpty(host.getClientId())) {
            host.setClientId(AppUtils.getAppId());
            host.setClientSecret(AppUtils.getAppSecret(host.getClientId()));
        }
        this.hostMapper.updateById(host);
        BaseOperateParam operateParam = CreateHostOperate.builder().hostId(host.getHostId()).id(UUID.randomUUID().toString())
                .title("注册主机[" + host.getDisplayName() + "]").build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(host.getHostId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_HOST).build());
        return ResultUtil.success(this.initHost(host));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<HostModel> maintenanceHost(int hostId) {
        HostEntity host = this.hostMapper.selectById(hostId);
        if (host == null) {
            throw new CodeException(ErrorCode.HOST_NOT_FOUND, "主机不存在");
        }

        host.setStatus(Constant.HostStatus.MAINTENANCE);
        this.hostMapper.updateById(host);
        this.notifyService.publish(NotifyData.<Void>builder().id(host.getHostId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_HOST).build());
        return ResultUtil.success(this.initHost(host));

    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> destroyHost(int hostId) {
        HostEntity host = this.hostMapper.selectById(hostId);
        if (this.guestMapper.selectCount(new QueryWrapper<GuestEntity>().eq(GuestEntity.HOST_ID, hostId)) > 0) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "请关闭当前主机的所有虚拟机后删除");
        }
        if (this.storageMapper.selectCount(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_HOST_ID, hostId)) > 0) {
            throw new CodeException(ErrorCode.HOST_HAS_LOCAL_STORAGE, "该主机已经启用了本地存储池，请首先删除该主机的本地存储池");
        }
        this.hostMapper.deleteById(hostId);
        this.configService.deleteAllocateConfig(Constant.ConfigType.HOST, hostId);
        this.notifyService.publish(NotifyData.<Void>builder().id(host.getHostId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_HOST).build());
        return ResultUtil.success();
    }
}
