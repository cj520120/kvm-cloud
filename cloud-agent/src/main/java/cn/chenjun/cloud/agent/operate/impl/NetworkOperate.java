package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.util.VxLanProxy;
import cn.chenjun.cloud.common.bean.BasicBridgeNetworkRequest;
import cn.chenjun.cloud.common.bean.VLanNetworkRequest;
import cn.chenjun.cloud.common.bean.VxLanNetworkRequest;
import cn.chenjun.cloud.common.core.annotation.DispatchBind;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Network;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NetworkOperate {

    @DispatchBind(command = Constant.Command.NETWORK_CREATE_BASIC)
    public Void createBasic(Connect connect, BasicBridgeNetworkRequest request) throws Exception {
        createNetworkPool(connect, request.getPoolId(), request.getXml());
        return null;
    }

    private void createNetworkPool(Connect connect, String name, String xml) throws Exception {

        log.info("创建网络:{},xml={}", name, xml);
        Network network;
        try {
            network = connect.networkLookupByName(name);
        } catch (Exception err) {
            network = connect.networkDefineXML(xml);
            network.setAutostart(true);
        }
        if (network.isActive() == 0) {
            network.create();
        }
    }

    @DispatchBind(command = Constant.Command.NETWORK_CREATE_VLAN)
    public Void createVlan(Connect connect, VLanNetworkRequest vlan) throws Exception {
        log.info("创建Van网络:{}", vlan);
        createNetworkPool(connect, vlan.getPoolId(), vlan.getBasic().getXml());
        createNetworkPool(connect, vlan.getPoolId(), vlan.getXml());
        return null;
    }

    @DispatchBind(command = Constant.Command.NETWORK_DESTROY_BASIC)
    public Void destroyBasic(Connect connect, BasicBridgeNetworkRequest bridge) throws Exception {
        log.info("销毁基础网络:{}", bridge);
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (networkNames.contains(bridge.getPoolId())) {
            try {
                Network network = connect.networkLookupByName(bridge.getPoolId());
                network.destroy();
                network.undefine();
            } catch (Exception ignored) {

            }
        }
        return null;

    }

    @DispatchBind(command = Constant.Command.NETWORK_DESTROY_VLAN)
    public Void destroyVlan(Connect connect, VLanNetworkRequest vlan) throws Exception {
        log.info("销毁VLan网络:{}", vlan);
        List<String> networkNames = Arrays.asList(connect.listNetworks());
        if (networkNames.contains(vlan.getPoolId())) {
            try {
                Network network = connect.networkLookupByName(vlan.getPoolId());
                network.destroy();
                network.undefine();
            } catch (Exception ignored) {

            }
        }
        return null;
    }

    @DispatchBind(command = Constant.Command.NETWORK_CREATE_VxLAN)
    public Void createVxLan(Connect connect, VxLanNetworkRequest vxLan) throws Exception {
        log.info("创建VxLan网络:{}", vxLan);
        VxLanProxy.CreateBridgeRequest request = VxLanProxy.CreateBridgeRequest.builder()
                .cidr(vxLan.getCidr())
                .gateway(vxLan.getGateway())
                .name(vxLan.getPoolId())
                .build();
        VxLanProxy vxLanProxy = VxLanProxy.builder()
                .baseUrl(vxLan.getBaseUrl())
                .token(vxLan.getToken())
                .connectTimeout(30L)
                .readTimeout(30L)
                .build();
        VxLanProxy.BaseResponse<VxLanProxy.CreateBridgeResponse> response = vxLanProxy.createBridge(request);
        if (response.getCode() != 0) {
            throw new CodeException(ErrorCode.SERVER_ERROR, response.getMsg());
        }
        log.info("创建VxLan网络成功:{}", vxLan);
        return null;
//        List<VxLanNetworkResponse.Port> ports=new ArrayList<>(vxLan.getMacs().size()) ;
//        for (String mac: vxLan.getMacs()) {
//
//            VxLanProxy.CreateBirgePortRequest createBirgePortRequest = VxLanProxy.CreateBirgePortRequest.builder()
//                    .bridgeName(vxLan.getPoolId())
//                    .mac(mac)
//                    .build();
//            VxLanProxy.BaseResponse<VxLanProxy.BridgePortData> createBridgePortResponse = vxLanProxy.createBridgePort(createBirgePortRequest);
//            if (createBridgePortResponse.getCode() != 0) {
//                throw new CodeException(ErrorCode.SERVER_ERROR, createBridgePortResponse.getMsg());
//            }
//            VxLanProxy.BridgePortData bridgePortData= createBridgePortResponse.getData();
//            ports.add(VxLanNetworkResponse.Port.builder().mac(mac).port(bridgePortData.getPortName()).build());
//        }
//        return VxLanNetworkResponse.builder().ports(ports).build();

    }

    @DispatchBind(command = Constant.Command.NETWORK_DESTROY_VxLAN)
    public Void destroyVxLan(Connect connect, VxLanNetworkRequest vxLan) throws Exception {
        VxLanProxy vxLanProxy = VxLanProxy.builder()
                .baseUrl(vxLan.getBaseUrl())
                .token(vxLan.getToken())
                .connectTimeout(30L)
                .readTimeout(30L)
                .build();
        VxLanProxy.BaseResponse<Map<String, String>> response = vxLanProxy.deleteBridge(vxLan.getPoolId());
        if (response.getCode() != 0) {
            throw new CodeException(ErrorCode.SERVER_ERROR, response.getMsg());
        }
        return null;

    }
}
