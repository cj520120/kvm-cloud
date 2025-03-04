package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.management.component.route.RouteComponentQmaInitialize;
import cn.chenjun.cloud.management.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenjun
 */
@Component
public class RouteComponentService extends AbstractComponentService<RouteComponentQmaInitialize> {


    public RouteComponentService(@Autowired List<RouteComponentQmaInitialize> componentQmaInitializeList) {
        super(componentQmaInitializeList);
    }

    @Override
    public int getComponentType() {
        return Constant.ComponentType.ROUTE;
    }

    @Override
    public String getComponentName() {
        return "Route VM";
    }


}
