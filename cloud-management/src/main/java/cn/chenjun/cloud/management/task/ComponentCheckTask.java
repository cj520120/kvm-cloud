package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.component.ComponentProcess;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.servcie.EventService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.reflect.TypeToken;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class ComponentCheckTask extends AbstractTask {

    @Autowired
    protected EventService eventService;
    @Autowired
    protected GuestMapper guestMapper;
    @Autowired
    private NetworkMapper networkMapper;
    @Autowired
    private PluginRegistry<ComponentProcess, Integer> processPluginRegistry;
    @Autowired
    private LockRunner lockRunner;
    @Autowired
    private ComponentMapper componentMapper;

    @Override
    protected void dispatch() throws Exception {
        List<NetworkEntity> networkList = networkMapper.selectList(new QueryWrapper<>());
        for (NetworkEntity network : networkList) {
            if (network.getStatus() == Constant.NetworkStatus.READY || network.getStatus() == Constant.NetworkStatus.INSTALL) {
                List<ComponentEntity> components = this.componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, network.getNetworkId()));

                for (ComponentEntity component : components) {
                        lockRunner.lockRun(RedisKeyUtil.GLOBAL_LOCK_KEY,()->processPluginRegistry.getPluginFor(component.getComponentType()).ifPresent(componentProcess -> componentProcess.checkAndStart(network, component)));

                }
                lockRunner.lockRun(RedisKeyUtil.GLOBAL_LOCK_KEY,()->{
                    //检测Route组件
                    ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.COMPONENT_TYPE, Constant.ComponentType.ROUTE).eq(ComponentEntity.NETWORK_ID, network.getNetworkId()).last("limit 0 ,1"));
                    if (component == null) {
                        return;
                    }
                    List<Integer> componentGuestIds = GsonBuilderUtil.create().fromJson(component.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
                    }.getType());
                    componentGuestIds.add(component.getMasterGuestId());
                    List<GuestEntity> componentGuestList = guestMapper.selectBatchIds(componentGuestIds).stream().filter(guestEntity -> Objects.equals(guestEntity.getStatus(), Constant.GuestStatus.RUNNING)).collect(Collectors.toList());
                    if (network.getStatus() == Constant.NetworkStatus.INSTALL && !componentGuestList.isEmpty()) {
                        //检测route组件是否已经初始化
                        network.setStatus(Constant.NetworkStatus.READY);
                        networkMapper.updateById(network);
                        this.eventService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
                    } else if (network.getStatus() == Constant.NetworkStatus.READY && componentGuestList.isEmpty()) {
                        //检测route组件
                        network.setStatus(Constant.NetworkStatus.INSTALL);
                        networkMapper.updateById(network);
                        this.eventService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
                    }
                });
            }
        }

    }

    @Override
    protected String getName() {
        return "系统组件检测";
    }

    @Override
    protected boolean canRunning() {
        return true;
    }
}
