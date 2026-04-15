package cn.chenjun.cloud.management.ovn.client;

import cn.chenjun.cloud.management.ovn.exception.OvnApiException;
import cn.chenjun.cloud.management.ovn.model.request.BuildInterfaceXmlRequest;
import cn.chenjun.cloud.management.ovn.model.request.CreateBridgeRequest;
import cn.chenjun.cloud.management.ovn.model.response.BaseResponse;
import cn.chenjun.cloud.management.ovn.model.response.CreateBridgeResponse;
import cn.chenjun.cloud.management.ovn.model.response.HealthResponse;
import cn.chenjun.cloud.management.ovn.model.response.NicXmlData;

import java.util.Map;

public interface OvnClient {

    BaseResponse<CreateBridgeResponse> createBridge(CreateBridgeRequest request) throws OvnApiException;

    BaseResponse<Map<String, String>> deleteBridge(String name) throws OvnApiException;

    BaseResponse<NicXmlData> buildInterfaceXml(BuildInterfaceXmlRequest request) throws OvnApiException;


}
