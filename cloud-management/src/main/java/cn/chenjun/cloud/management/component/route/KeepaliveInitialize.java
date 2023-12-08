package cn.chenjun.cloud.management.component.route;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.hutool.core.io.resource.ResourceUtil;
import com.hubspot.jinjava.Jinjava;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class KeepaliveInitialize implements RouteComponentQmaInitialize {
    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(3);
        map.put("vip", component.getComponentVip());
        map.put("virtual_router_id", component.getComponentId());
        if (component.getMasterGuestId() == guestId) {
            map.put("state", "MASTER");
            map.put("priority", "150");
        } else {
            map.put("state", "BACKUP");
            map.put("priority", "100");
        }
        String config = new String(Base64.getDecoder().decode(ResourceUtil.readUtf8Str("tpl/keepalived.tpl")), StandardCharsets.UTF_8);
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
