package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.component.AbstractComponentService;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * @author chenjun
 */
@Component
public class ComponentCheckTask extends AbstractTask {

    @Autowired
    private NetworkMapper networkMapper;

    @Autowired
    private List<AbstractComponentService> componentServiceList;

    public ComponentCheckTask(@Autowired  List<AbstractComponentService> componentServiceLis){
        this.componentServiceList=componentServiceLis;
        this.componentServiceList.sort(Comparator.comparingInt(AbstractComponentService::order));
    }


    @Override
    protected void dispatch() throws Exception {
        List<NetworkEntity> networkList = networkMapper.selectList(new QueryWrapper<>());
        for (NetworkEntity network : networkList) {
            if (network.getStatus() == Constant.NetworkStatus.READY) {
                for (AbstractComponentService componentService : this.componentServiceList) {
                        componentService.checkAndStart(network.getNetworkId());
                }
            }
        }

    }
}
