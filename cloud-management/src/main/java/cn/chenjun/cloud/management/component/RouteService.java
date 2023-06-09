package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.util.Constant;
import cn.hutool.core.io.resource.ResourceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Component
public class RouteService extends AbstractComponentService {
    @Autowired
    private RouteInitialize routeInitialize;

    @Override
    public int getComponentType() {
        return Constant.ComponentType.ROUTE;
    }

    @Override
    public boolean allocateBasicNic() {
        return true;
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public String getComponentName() {
        return "System Route";
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

        //写入网卡固定IP
        List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId));
        Collections.sort(guestNetworkList, Comparator.comparingInt(GuestNetworkEntity::getDeviceId));
        String[] iptablesRules = null;
        for (int i = 0; i < guestNetworkList.size(); i++) {
            GuestNetworkEntity guestNetwork = guestNetworkList.get(i);
            NetworkEntity network = this.networkMapper.selectById(guestNetwork.getNetworkId());
            int index = guestNetwork.getDeviceId();
            String networkConfig;
            if (network.getType().equals(Constant.NetworkType.BASIC)) {
                iptablesRules = new String[]{"-t", "nat", "-A", "POSTROUTING", "-o", "eth" + index, "-j", "MASQUERADE"};
                networkConfig = this.getNicConfig(index, guestNetwork.getIp(), network.getMask(), network.getGateway(), network.getDns());
            } else {
                networkConfig = this.getNicConfig(index, guestNetwork.getIp(), network.getMask(), "", "");

            }
            if (i == 0) {
                networkConfig += "\r\nIPADDR1=169.254.169.254";
            }
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/sysconfig/network-scripts/ifcfg-eth" + index).fileBody(networkConfig).build())).build());
        }

        //重启网卡
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "network"}).checkSuccess(true).build())).build());
        if (iptablesRules != null) {
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("iptables").args(iptablesRules).checkSuccess(true).build())).build());
        }
//      //设置主机名
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("hostnamectl").args(new String[]{"set-hostname", this.getComponentName()}).checkSuccess(true).build())).build());
        commands.addAll(this.initYumSource());
        commands.addAll(routeInitialize.initialize(guestId));

        if (routeInitialize.isEnableMetaService()) {


            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "python36"}).checkSuccess(true).build())).build());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "python3-pip"}).checkSuccess(true).build())).build());
            commands.add(this.pip3Install("flask", true));
            commands.add(this.pip3Install("requests", true));


            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("mkdir").args(new String[]{"-p", "/usr/local/meta-service/"}).checkSuccess(true).build())).build());


            String metaServiceShell = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("meta/meta.sh").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            String metaService = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("meta/meta.service").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            String metaPython = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("meta/meta.py").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            NetworkEntity network = this.networkMapper.selectById(component.getNetworkId());
            metaPython = String.format(metaPython, applicationConfig.getManagerUri(), network.getSecret());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/usr/local/meta-service/meta.py").fileBody(metaPython).build())).build());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/usr/local/meta-service/service.sh").fileBody(metaServiceShell).build())).build());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/usr/lib/systemd/system/meta-service.service").fileBody(metaService).build())).build());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("chmod").args(new String[]{"a+x", "/usr/local/meta-service/service.sh"}).build())).build());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"daemon-reload"}).checkSuccess(true).build())).build());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "meta-service"}).build())).build());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "meta-service"}).build())).build());


            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install", "-y", "nginx"}).checkSuccess(true).build())).build());
            String metaServiceConfig = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("meta/nginx.conf").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/nginx/nginx.conf").fileBody(metaServiceConfig).build())).build());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable", "nginx"}).checkSuccess(true).build())).build());
            commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "nginx"}).checkSuccess(true).build())).build());


        }
        return request;
    }
}
