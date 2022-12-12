package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.management.component.ComponentService;
import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.data.mapper.*;
import cn.roamblue.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComponentCheckTask extends AbstractTask {

    @Autowired
    private NetworkMapper networkMapper;

    @Autowired
    private List<ComponentService> componentServiceList;

    @Override
    protected int getPeriodSeconds() {
        return 5;
    }

    @Override
    protected void dispatch() throws Exception {
        List<NetworkEntity> networkList = networkMapper.selectList(new QueryWrapper<>());
        for (NetworkEntity network : networkList) {
            if (network.getStatus() == Constant.NetworkStatus.READY) {
                for (ComponentService componentService : this.componentServiceList) {
                    componentService.init(network.getNetworkId());
                }
            }
        }

    }
}
