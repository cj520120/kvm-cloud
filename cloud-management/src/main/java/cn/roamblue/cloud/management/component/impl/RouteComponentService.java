package cn.roamblue.cloud.management.component.impl;

import cn.roamblue.cloud.common.bean.GuestQmaRequest;
import cn.roamblue.cloud.common.bean.OsNic;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.management.data.entity.GuestNetworkEntity;
import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.data.mapper.GuestNetworkMapper;
import cn.roamblue.cloud.management.data.mapper.NetworkMapper;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.IpCaculate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.awt.image.IntegerComponentRaster;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RouteComponentService extends AbstractComponentService {
    @Autowired
    private NetworkMapper networkMapper;
    private GuestNetworkMapper guestNetworkMapper;

    @Override
    public int getType() {
        return Constant.ComponentType.ROUTE;
    }

    @Override
    protected String getName() {
        return "Route VM";
    }

    @Override
    public List<OsNic> getDefaultNic(int guestId, int networkId) {
        NetworkEntity network = networkMapper.selectById(networkId);
        if (network == null) {
            return new ArrayList<>();
        }
        OsNic metaServiceNic = OsNic.builder()
                .deviceId(0)
                .name("")
                .mac(IpCaculate.getMacAddrWithFormat(":"))
                .bridgeName(network.getBridge())
                .driveType(cn.roamblue.cloud.common.util.Constant.NetworkDriver.VIRTIO)
                .build();
        return Collections.singletonList(metaServiceNic);
    }

    @Override
    public GuestQmaRequest getStartQmaRequest(int guestId, int networkId) {

        NetworkEntity network = networkMapper.selectById(networkId);
        if(network==null){
            return null;
        }
        List<GuestNetworkEntity> guestNetworkList=this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("network_id",networkId));
        GuestNetworkEntity defaultGuestNic=guestNetworkList.stream().min(Comparator.comparingInt(GuestNetworkEntity::getDeviceId)).orElse(null);
        if(defaultGuestNic==null){
            return null;
        }
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        GuestQmaRequest request = GuestQmaRequest.builder().build();
        request.setName("");
        request.setTimeout(TimeUnit.MINUTES.toSeconds(10));
        request.setCommands(commands);
        //写入默认网卡
        //重启网卡
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart","network"}).build())).build());
        StringBuilder dhcp=new StringBuilder();
        dhcp.append("interface=eth").append(defaultGuestNic.getDeviceId()+1).append("\r\n");
        dhcp.append("listen-address=").append(defaultGuestNic.getIp()).append("\r\n");
        dhcp.append("no-dhcp-interface=").append("\r\n");
        dhcp.append("dhcp-range=").append(network.getStartIp()).append(",").append(network.getEndIp()).append(",").append("24h").append("\r\n");
        for (GuestNetworkEntity guestNetwork : guestNetworkList) {
            //写入网卡
            dhcp.append("dhcp-host=").append(guestNetwork.getMac()).append(",").append(guestNetwork.getIp()).append("\r\n");
        }
        dhcp.append("dhcp-option=option:router,").append(network.getGateway()).append("\r\n");
        dhcp.append("dhcp-option=option:netmask,").append(network.getMask()).append("\r\n");
        dhcp.append("dhcp-option=option:dns-server,").append(network.getDns()).append("\r\n");
        dhcp.append("dhcp-option=option:classless-static-route,169.254.254.254/32,").append(defaultGuestNic.getIp()).append("\r\n");
         //下载dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("yum").args(new String[]{"install","-y","dnsmasq"}).build())).build());
        //写入dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/dnsmasq.conf").fileBody(dhcp.toString()).build())).build());
        //启动dnsmasq
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"enable","dnsmasq"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart","dnsmasq"}).build())).build());
        return request;
    }
}
