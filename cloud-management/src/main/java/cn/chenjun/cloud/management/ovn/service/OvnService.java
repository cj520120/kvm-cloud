package cn.chenjun.cloud.management.ovn.service;

import cn.chenjun.cloud.management.ovn.client.OvnClient;
import cn.chenjun.cloud.management.ovn.exception.OvnApiException;
import cn.chenjun.cloud.management.ovn.model.request.BuildInterfaceXmlRequest;
import cn.chenjun.cloud.management.ovn.model.request.CreateBridgeRequest;
import cn.chenjun.cloud.management.ovn.model.response.BaseResponse;
import cn.chenjun.cloud.management.ovn.model.response.CreateBridgeData;
import cn.chenjun.cloud.management.ovn.model.response.NicXmlData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OvnService {

    private final OvnClient ovnClientClient;

    public CreateBridgeData createBridge(String name, String cidr, String gateway) throws OvnApiException {
        CreateBridgeRequest request = CreateBridgeRequest.builder().cidr(cidr).gateway(gateway).name(name).build();
        BaseResponse<CreateBridgeData> response = ovnClientClient.createBridge(request);

        if (response.getCode() == 0) {
            log.info("网桥创建成功: {}", response.getData().getUserBridgeName());
            return response.getData();
        } else {
            log.error("网桥创建失败: {}", response.getMsg());
            throw new OvnApiException(response.getMsg());
        }
    }

    public boolean deleteBridge(String name) {
        BaseResponse<Map<String, String>> response = ovnClientClient.deleteBridge(name);
        if (response.getCode() == 0) {
            log.info("网桥删除成功: {}", name);
            return true;
        } else {
            log.error("网桥删除失败: {}", response.getMsg());
            return false;
        }
    }

    public String buildInterfaceXml(String bridgeName, String model, String mac) {
        BuildInterfaceXmlRequest request = BuildInterfaceXmlRequest.builder()
                .bridgeName(bridgeName)
                .mac(mac)
                .model(model)
                .build();
        BaseResponse<NicXmlData> response = ovnClientClient.buildInterfaceXml(request);
        if (response.getCode() == 0) {
            log.info("获取网卡 XML 成功, portUuid: {}", response.getData().getPortUuid());
            return response.getData().getXml();
        } else {
            log.error("获取网卡 XML 失败: {}", response.getMsg());
            throw new OvnApiException(response.getMsg());
        }
    }
}
