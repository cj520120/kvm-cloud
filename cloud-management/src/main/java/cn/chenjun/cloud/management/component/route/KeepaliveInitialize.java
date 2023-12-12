package cn.chenjun.cloud.management.component.route;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.hutool.core.io.resource.ResourceUtil;
import com.google.common.reflect.TypeToken;
import com.hubspot.jinjava.Jinjava;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author chenjun
 */
@Component
public class KeepaliveInitialize implements RouteComponentQmaInitialize {
    public static final int MASTER_PRIORITY = 500;
    public static final int SLAVE_PRIORITY = 300;

    private static Map<String, Object> buildVrrp(String name, String nic, String vip, ComponentEntity component, int guestId) {
        List<Integer> slaveIds = GsonBuilderUtil.create().fromJson(component.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
        }.getType());
        Map<String, Object> vrrp = new HashMap<>(3);
        vrrp.put("name", name);
        vrrp.put("interface", nic);
        vrrp.put("vip", vip);
        if (component.getMasterGuestId() == guestId) {
            vrrp.put("state", "MASTER");
            vrrp.put("priority", MASTER_PRIORITY);
        } else {
            vrrp.put("state", "BACKUP");
            int index = slaveIds.indexOf(guestId);
            vrrp.put("priority", SLAVE_PRIORITY - index);
        }
        return vrrp;
    }

    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();

        List<Map<String, Object>> vrrpList = new ArrayList<>(1);
        vrrpList.add(buildVrrp("VI_1", "eth0", component.getComponentVip(), component, guestId));

        if (!StringUtils.isEmpty(component.getBasicComponentVip()) && !Objects.equals(component.getComponentVip(), component.getBasicComponentVip())) {
            vrrpList.add(buildVrrp("VI_2", "eth1", component.getBasicComponentVip(), component, guestId));
        }
        Map<String, Object> map = new HashMap<>(1);
        map.put("vrrpList", vrrpList);
        String config = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/keepalived/keepalived.tpl")), StandardCharsets.UTF_8);
        Jinjava jinjava = new Jinjava();
        config = jinjava.render(config, map);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("sh").args(new String[]{"/tmp/check_install_service_shell.sh", "keepalived"}).build())).build());
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.WRITE_FILE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.WriteFile.builder().fileName("/etc/keepalived/keepalived.conf").fileBody(config).build())).build());
        //启动keepalive
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("systemctl").args(new String[]{"restart", "keepalived"}).build())).build());
        return commands;
    }

    @Override
    public int getOrder() {
        return RouteOrder.KEEPALIVE;
    }
}
