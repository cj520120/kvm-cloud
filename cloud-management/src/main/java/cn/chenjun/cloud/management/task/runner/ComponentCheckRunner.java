package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.component.ComponentProcess;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.servcie.NotifyService;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class ComponentCheckRunner extends AbstractRunner {

    @Autowired
    protected NotifyService notifyService;
    @Autowired
    protected GuestMapper guestMapper;
    @Autowired
    private NetworkMapper networkMapper;
    @Autowired
    private PluginRegistry<ComponentProcess, Integer> processPluginRegistry;
    @Autowired
    private ComponentMapper componentMapper;

    @Override
    public int getPeriodSeconds() {
        return configService.getConfig(ConfigKey.DEFAULT_TASK_COMPONENT_CHECK_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() throws Exception {
        List<NetworkEntity> networkList = networkMapper.selectList(new QueryWrapper<>());
        for (NetworkEntity network : networkList) {
            List<ConfigQuery> queryList=new ArrayList<>();
            queryList.add(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build());
            queryList.add(ConfigQuery.builder().type(Constant.ConfigType.NETWORK).id(network.getNetworkId()).build());
            if(Objects.equals(this.configService.getConfig(queryList,ConfigKey.SYSTEM_COMPONENT_ENABLE), Constant.Enable.NO)){
                continue;
            }
            if (network.getStatus() == Constant.NetworkStatus.READY || network.getStatus() == Constant.NetworkStatus.INSTALL) {
                List<ComponentEntity> components = this.componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, network.getNetworkId()));
                for (ComponentEntity component : components) {
                    processPluginRegistry.getPluginFor(component.getComponentType()).ifPresent(componentProcess -> componentProcess.checkAndStart(network, component));
                }
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
                    this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
                } else if (network.getStatus() == Constant.NetworkStatus.READY && componentGuestList.isEmpty()) {
                    //检测route组件
                    network.setStatus(Constant.NetworkStatus.INSTALL);
                    networkMapper.updateById(network);
                    this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
                }
            }
        }

    }

    @Override
    protected String getName() {
        return "系统组件检测";
    }

}
