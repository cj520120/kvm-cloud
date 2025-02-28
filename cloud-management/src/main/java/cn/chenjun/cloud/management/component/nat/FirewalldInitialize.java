package cn.chenjun.cloud.management.component.nat;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.component.route.ComponentOrder;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@Component
public class FirewalldInitialize implements NatComponentQmaInitialize {
    @Override
    public List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component, int guestId, Map<String, Object> sysconfig) {
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("sh").args(new String[]{"/tmp/open_firewalld.sh"}).build())).build());
        return commands;
    }

    @Override
    public int getOrder() {
        return ComponentOrder.FIREWALLD;
    }
}
