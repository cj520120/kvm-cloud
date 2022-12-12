package cn.roamblue.cloud.management.component.impl;

import cn.roamblue.cloud.common.bean.GuestQmaRequest;
import cn.roamblue.cloud.common.bean.OsNic;
import cn.roamblue.cloud.management.util.Constant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VncComponentService extends AbstractComponentService {
    @Override
    public int getType() {
        return Constant.ComponentType.VNC;
    }

    @Override
    protected String getName() {
        return "Vnc VM";
    }

    @Override
    public List<OsNic> getDefaultNic(int guestId, int networkId) {
        return new ArrayList<>();
    }

    @Override
    public GuestQmaRequest getStartQmaRequest(int guestId, int networkId) {
        return null;
    }
}
