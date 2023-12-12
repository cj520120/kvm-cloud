package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.component.route.RouteComponentQmaInitialize;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Component
public class RouteComponentService extends AbstractComponentService {


    private final List<RouteComponentQmaInitialize> componentQmaInitializeList;

    @Override
    public boolean allocateBasicNic() {
        return true;
    }

    @Override
    public int order() {
        return 0;
    }

    public RouteComponentService(@Autowired List<RouteComponentQmaInitialize> componentQmaInitializeList) {

        componentQmaInitializeList.sort(Comparator.comparingInt(Ordered::getOrder));
        this.componentQmaInitializeList = componentQmaInitializeList;
    }

    @Override
    public int getComponentType() {
        return Constant.ComponentType.ROUTE;
    }

    @Override
    public String getComponentName() {
        return "Route VM";
    }


    @Override
    public boolean isAllow(NetworkEntity network) {
        return true;
    }

    @Override
    public GuestQmaRequest getStartQmaRequest(int networkId, int guestId) {

        ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, networkId).eq(ComponentEntity.COMPONENT_TYPE, this.getComponentType()));
        if (component == null) {
            return null;
        }
        List<GuestQmaRequest.QmaBody> commands = new ArrayList<>();
        GuestQmaRequest request = GuestQmaRequest.builder().build();
        request.setName("");
        request.setTimeout((int) TimeUnit.MINUTES.toSeconds(5));
        request.setCommands(commands);
        commands.add(GuestQmaRequest.QmaBody.builder().command(GuestQmaRequest.QmaType.EXECUTE).data(GsonBuilderUtil.create().toJson(GuestQmaRequest.Execute.builder().command("hostnamectl").args(new String[]{"set-hostname", this.getComponentName()}).checkSuccess(true).build())).build());
        for (ComponentQmaInitialize componentQmaInitialize : componentQmaInitializeList) {
            List<GuestQmaRequest.QmaBody> childCommands = componentQmaInitialize.initialize(component, guestId);
            if (!ObjectUtils.isEmpty(childCommands)) {
                commands.addAll(childCommands);
            }
        }
        return request;
    }
}
