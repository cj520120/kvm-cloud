package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.management.component.nat.NatComponentQmaInitialize;
import cn.chenjun.cloud.management.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenjun
 */
@Component
public class NatComponentService extends AbstractComponentService<NatComponentQmaInitialize> {



    public NatComponentService(@Autowired List<NatComponentQmaInitialize> componentQmaInitializeList) {
        super(componentQmaInitializeList);
    }


    @Override
    public int getComponentType() {
        return Constant.ComponentType.NAT;
    }

    @Override
    public String getComponentName() {
        return "Nat VM";
    }

}
