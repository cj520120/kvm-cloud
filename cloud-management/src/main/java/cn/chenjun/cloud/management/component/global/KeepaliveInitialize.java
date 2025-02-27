package cn.chenjun.cloud.management.component.global;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.component.route.ComponentOrder;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.GuestNetworkMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.TemplateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author chenjun
 */
@Component
public class KeepaliveInitialize implements GlobalComponentQmaInitialize {
    public static final int MASTER_PRIORITY = 100;
    public static final int SLAVE_PRIORITY = 90;
    @Autowired
    private GuestNetworkMapper guestNetworkMapper;
    @Autowired
    private NetworkMapper networkMapper;

    private static Map<String, Object> buildVrrp(String name, String nic, String vip, ComponentEntity component, int guestId, Map<String, Object> sysconfig) {
        List<Integer> slaveIds = GsonBuilderUtil.create().fromJson(component.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
        }.getType());
        int routeId = component.getComponentId() % 255;
        routeId = Math.max(1, routeId);
        Map<String, Object> vrrp = new HashMap<>(3);
        vrrp.put("__SYS__", sysconfig);
        vrrp.put("name", name);
        vrrp.put("interface", nic);
        vrrp.put("vip", vip);
        //非抢占模式全部为backup
        vrrp.put("state", "BACKUP");
        vrrp.put("routeId", routeId);
        if (component.getMasterGuestId() == guestId) {
            vrrp.put("priority", MASTER_PRIORITY);
        } else {
            int index = slaveIds.indexOf(guestId) + 1;
            vrrp.put("priority", Math.max(1, SLAVE_PRIORITY - index));
        }
        return vrrp;
    }

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId, Map<String, Object> sysconfig) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, guestId).eq(GuestNetworkEntity.ALLOCATE_TYPE, Constant.NetworkAllocateType.GUEST));
        List<Map<String, Object>> vrrpList = new ArrayList<>(1);

        NetworkEntity network = this.networkMapper.selectById(component.getNetworkId());
        for (int i = 0; i < guestNetworkList.size(); i++) {
            GuestNetworkEntity guestNetwork = guestNetworkList.get(i);
            String name = "VI_" + i;
            String nic = "eth" + i;
            String vip = null;
            if (Objects.equals(network.getNetworkId(), guestNetwork.getNetworkId())) {
                vip = component.getComponentVip();
            } else if (Objects.equals(network.getBasicNetworkId(), guestNetwork.getNetworkId())) {
                vip = component.getBasicComponentVip();
            }
            if (ObjectUtils.isNotEmpty(vip)) {
                vrrpList.add(buildVrrp(name, nic, vip, component, guestId, sysconfig));
            }
        }
        Map<String, Object> map = new HashMap<>(1);
        map.put("__SYS__", sysconfig);
        map.put("vrrpList", vrrpList);
        String config = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/keepalived/keepalived.tpl")), StandardCharsets.UTF_8);
        config = TemplateUtil.create().render(config, map);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("sh").args(new String[]{"/tmp/check_install_service_shell.sh", "keepalived"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/keepalived/keepalived.conf").fileBody(config).build())).build());
        //启动keepalive
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "keepalived"}).build())).build());
        return commands;
    }

    @Override
    public int getOrder() {
        return ComponentOrder.KEEP_ALIVE;
    }
}
