package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.config.ApplicationConfig;
import cn.chenjun.cloud.agent.operate.NetworkOperate;
import cn.chenjun.cloud.agent.util.NetworkType;
import cn.chenjun.cloud.common.bean.BasicBridgeNetwork;
import cn.chenjun.cloud.common.bean.VlanNetwork;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.List;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NetworkOperateImpl implements NetworkOperate {


    @Autowired
    private ApplicationConfig applicationConfig;
    @Override
    public void createBasic(Connect connect, BasicBridgeNetwork request) throws Exception {
        log.info("创建基础网络:{} type={}",request,applicationConfig.getNetworkType());
        if(NetworkType.OPEN_SWITCH.equalsIgnoreCase(applicationConfig.getNetworkType())){
           List<String> networkNames= Arrays.asList(connect.listNetworks());
           if(!networkNames.contains(request.getBridge())){
               String xml = ResourceUtil.readUtf8Str("xml/network/OpenSwitch.xml");
               connect.networkCreateXML(String.format(xml, request.getBridge(),request.getBridge()));
           }
        }

    }

    @Override
    public void createVlan(Connect connect, VlanNetwork vlan) throws Exception {
        log.info("创建Vlan网络:{}",vlan);
    }

    @Override
    public void destroyBasic(Connect connect, BasicBridgeNetwork bridge) throws Exception {
        log.info("销毁基础网络:{}",bridge);
        if(NetworkType.OPEN_SWITCH.equalsIgnoreCase(applicationConfig.getNetworkType())){
            List<String> networkNames= Arrays.asList(connect.listNetworks());
            if(!networkNames.contains(bridge.getBridge())){
                try {
                    Network network = connect.networkLookupByName(bridge.getBridge());
                    network.destroy();
                }catch (Exception err){

                }
            }
        }
    }

    @Override
    public void destroyVlan(Connect connect, VlanNetwork vlan) throws Exception {
        log.info("销毁Vlan网络:{}",vlan);
    }



}
