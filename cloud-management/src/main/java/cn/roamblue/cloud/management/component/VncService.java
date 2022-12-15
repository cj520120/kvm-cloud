package cn.roamblue.cloud.management.component;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.roamblue.cloud.common.bean.GuestQmaRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.data.mapper.GuestVncMapper;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.operate.bean.VncUpdateOperate;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class VncService extends AbstractComponentService {
    @Autowired
    private GuestVncMapper guestVncMapper;
    @Autowired
    private HostMapper hostMapper;

    @Override
    public int getComponentType() {
        return Constant.ComponentType.VNC;
    }

    @Override
    public String getComponentName() {
        return "System Vnc";
    }


    public GuestEntity getGuestVncServer(int guestId) {
        //获取到客户机默认网络
        GuestNetworkEntity guestNetwork = this.guestNetworkMapper.selectOne(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId).eq("device_id", 0));
        if (guestNetwork == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机默认网络不存在");
        }
        //获取网络对应的组件
        ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("network_id", guestNetwork.getNetworkId()).eq("component_type", Constant.ComponentType.VNC).last("limit 0,1"));
        if (component == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机所在网络VNC未初始化完成");
        }
        //获取改组件对应的虚拟机
        GuestEntity guest = this.guestMapper.selectById(component.getGuestId());
        if (guest == null || !Objects.equals(guest.getStatus(), Constant.GuestStatus.RUNNING)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机所在网络VNC未初始化完成");
        }
//        HostEntity host = this.hostMapper.selectById(guest.getHostId());
//        if (host == null) {
//            throw new CodeException(ErrorCode.SERVER_ERROR, "VNC所在宿主机不存在");
//        }
//        if(Objects.equals(host.getStatus(), Constant.HostStatus.ONLINE)){
//            throw new CodeException(ErrorCode.SERVER_ERROR, "VNC所在宿主机未就绪");
//        }
//        //host vnc
        return guest;
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public GuestVncEntity getGuestVnc(int guestId) {
        GuestVncEntity guestVncEntity = this.guestVncMapper.selectById(guestId);
        if (guestVncEntity == null) {
            guestVncEntity = GuestVncEntity.builder()
                    .guestId(guestId)
                    .port(0)
                    .password(RandomStringUtils.randomAlphanumeric(8))
                    .token(RandomStringUtils.randomAlphanumeric(16))
                    .build();
            this.guestVncMapper.insert(guestVncEntity);
        }
        return guestVncEntity;
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public void updateVncPort(int guestId, int port) {
        GuestVncEntity guestVncEntity = this.guestVncMapper.selectById(guestId);
        if (guestVncEntity != null) {
            guestVncEntity.setPort(port);
            this.guestVncMapper.updateById(guestVncEntity);
            GuestEntity guest = this.guestMapper.selectById(guestId);
            if (guest == null) {
                return;
            }
            HostEntity host = this.hostMapper.selectById(guest.getHostId());
            if (host == null) {
                return;
            }
            BaseOperateParam operateParam = VncUpdateOperate.builder()
                    .taskId(UUID.randomUUID().toString())
                    .title("更新Vnc地址[" + guest.getDescription() + "]")
                    .guestId(guestId)
                    .ip(host.getHostIp())
                    .port(port)
                    .token(guestVncEntity.getToken())
                    .build();
            this.operateTask.addTask(operateParam);
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public void destroyGuest(int guestId) {
        this.guestVncMapper.deleteById(guestId);
    }

    @Override
    public GuestQmaRequest getQmaRequest(int guestId) {
        ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("guest_id", guestId));
        if (component == null) {
            return null;
        }
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        GuestQmaRequest request = GuestQmaRequest.builder().build();
        request.setName("");
        request.setTimeout((int) TimeUnit.MINUTES.toSeconds(5));
        request.setCommands(commands);
        //写入默认网卡
        List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId));
        for (int i = 0; i < guestNetworkList.size(); i++) {
            GuestNetworkEntity guestNetwork = guestNetworkList.get(i);
            NetworkEntity network = this.networkMapper.selectById(guestNetwork.getNetworkId());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth" + i).fileBody(this.getNicConfig(i, guestNetwork.getIp(), network.getMask(), network.getGateway(), network.getDns())).build())).build());

        }
        //重启网卡
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "network"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("hostnamectl").args(new String[]{"set-hostname", this.getComponentName()}).build())).build());
        //安装websockify
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "python36"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "python3-pip"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("pip3").args(new String[]{"install", "websockify==0.10.0"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("mkdir").args(new String[]{"-p", "/usr/local/websockify/token"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("mkdir").args(new String[]{"-p", "/usr/local/websockify/scripts"}).build())).build());
        String websockifyShell = ResourceUtil.readUtf8Str("config/websockify.sh");
        String websockifyService = ResourceUtil.readUtf8Str("config/websockify.service");
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/usr/local/websockify/scripts/service.sh").fileBody(websockifyShell).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/usr/lib/systemd/system/websockify.service").fileBody(websockifyService).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("chmod").args(new String[]{"a+x", "/usr/local/websockify/scripts/service.sh"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"daemon-reload"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "websockify"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "websockify"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("rm").args(new String[]{"-rf", "/usr/local/websockify/token/*"}).build())).build());
        List<GuestVncEntity> guestVncList = this.guestVncMapper.selectList(new QueryWrapper<>());
        Map<Integer, HostEntity> hostMap = new HashMap<>(guestVncList.size());
        for (GuestVncEntity guestVncEntity : guestVncList) {
            if (guestVncEntity.getPort() <= 0 || StringUtils.isEmpty(guestVncEntity.getToken())) {
                continue;
            }
            GuestEntity guest = this.guestMapper.selectById(guestVncEntity.getGuestId());
            if (guest == null || guest.getHostId() <= 0) {
                continue;
            }
            HostEntity host = hostMap.computeIfAbsent(guest.getHostId(), hostId -> hostMapper.selectById(hostId));
            if (host == null) {
                continue;
            }
            String file = "/usr/local/websockify/token/vnc_" + guest.getGuestId();
            String body = String.format("%s: %s:%d", guestVncEntity.getToken(), host.getHostIp(), guestVncEntity.getPort());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName(file).fileBody(body).build())).build());
        }
        return request;
    }
}
