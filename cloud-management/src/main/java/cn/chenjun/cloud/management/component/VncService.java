package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.VncUpdateOperate;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Component
public class VncService extends AbstractComponentService {


    @Override
    public int getComponentType() {
        return Constant.ComponentType.VNC;
    }

    @Override
    public String getComponentName() {
        return "System Vnc";
    }

    @Override
    public boolean allocateBasicNic() {
        return true;
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkAndStart(int networkId) {
        ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("component_type", Constant.ComponentType.ROUTE).eq("network_id", networkId).last("limit 0 ,1"));
        if (component == null) {
            return;
        }
        GuestEntity vncGuest = this.guestMapper.selectById(component.getGuestId());
        if (vncGuest == null || !Objects.equals(vncGuest.getStatus(), Constant.GuestStatus.RUNNING)) {
            return;
        }
        super.checkAndStart(networkId);
    }

    @Override
    public int order() {
        return 1;
    }

    public GuestEntity getGuestVncServer(int guestId) {
        //获取到客户机默认网络
        GuestEntity guest = this.guestMapper.selectById(guestId);
        //获取网络对应的组件
        ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("network_id", guest.getNetworkId()).eq("component_type", Constant.ComponentType.VNC).last("limit 0,1"));
        if (component == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机所在网络VNC未初始化完成");
        }
        //获取该组件对应的虚拟机
        GuestEntity vncGuest = this.guestMapper.selectById(component.getGuestId());
        if (vncGuest == null || !Objects.equals(vncGuest.getStatus(), Constant.GuestStatus.RUNNING)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机所在网络VNC未初始化完成");
        }
        return vncGuest;
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
    public GuestQmaRequest getStartQmaRequest(int guestId) {
        ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("guest_id", guestId));
        if (component == null) {
            return null;
        }
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        GuestQmaRequest request = GuestQmaRequest.builder().build();
        request.setName("");
        request.setTimeout((int) TimeUnit.MINUTES.toSeconds(5));
        request.setCommands(commands);
        String[] iptablesRules = null;
        //写入默认网卡
        List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId));
        Collections.sort(guestNetworkList, Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        for (int i = 0; i < guestNetworkList.size(); i++) {
            GuestNetworkEntity guestNetwork = guestNetworkList.get(i);
            NetworkEntity network = this.networkMapper.selectById(guestNetwork.getNetworkId());
            if (network.getType().equals(Constant.NetworkType.BASIC)) {
                iptablesRules = new String[]{"-t", "nat", "-A", "POSTROUTING", "-o", "eth" + guestNetwork.getDeviceId(), "-j", "MASQUERADE"};
                commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth" + guestNetwork.getDeviceId()).fileBody(this.getNicConfig(guestNetwork.getDeviceId(), guestNetwork.getIp(), network.getMask(), network.getGateway(), network.getDns())).build())).build());
            } else {
                commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth" + guestNetwork.getDeviceId()).fileBody(this.getNicConfig(guestNetwork.getDeviceId(), guestNetwork.getIp(), network.getMask(), "", "")).build())).build());
            }
        }
        //重启网卡
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "network"}).checkSuccess(true).build())).build());
        if (iptablesRules != null) {
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("iptables").args(iptablesRules).checkSuccess(true).build())).build());
        }
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("hostnamectl").args(new String[]{"set-hostname", this.getComponentName()}).build())).build());
        //安装websockify
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "python36"}).checkSuccess(true).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "python3-pip"}).checkSuccess(true).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("pip3").args(new String[]{"install", "websockify==0.10.0"}).checkSuccess(true).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("mkdir").args(new String[]{"-p", "/usr/local/websockify/token"}).checkSuccess(true).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("mkdir").args(new String[]{"-p", "/usr/local/websockify/scripts"}).checkSuccess(true).build())).build());
        String websockifyShell = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("config/websockify.sh").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        String websockifyService = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("config/websockify.service").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/usr/local/websockify/scripts/service.sh").fileBody(websockifyShell).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/usr/lib/systemd/system/websockify.service").fileBody(websockifyService).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("chmod").args(new String[]{"a+x", "/usr/local/websockify/scripts/service.sh"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"daemon-reload"}).checkSuccess(true).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "websockify"}).checkSuccess(true).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "websockify"}).checkSuccess(true).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("rm").args(new String[]{"-rf", "/usr/local/websockify/token/*"}).checkSuccess(true).build())).build());
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
