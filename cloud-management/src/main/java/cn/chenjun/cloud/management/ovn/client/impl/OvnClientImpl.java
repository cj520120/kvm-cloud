package cn.chenjun.cloud.management.ovn.client.impl;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.ovn.client.OvnClient;
import cn.chenjun.cloud.management.ovn.exception.OvnApiException;
import cn.chenjun.cloud.management.ovn.model.request.BuildInterfaceXmlRequest;
import cn.chenjun.cloud.management.ovn.model.request.CreateBridgeRequest;
import cn.chenjun.cloud.management.ovn.model.response.BaseResponse;
import cn.chenjun.cloud.management.ovn.model.response.CreateBridgeResponse;
import cn.chenjun.cloud.management.ovn.model.response.NicXmlData;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OvnClientImpl implements OvnClient {

    private final ConfigService configService;
    private final NetworkService networkService;

    public OvnClientImpl(ConfigService configService, NetworkService networkService) {
        this.networkService = networkService;
        this.configService = configService;

    }

    private String getOvnApiUri() {
        if (!Constant.Enable.YES.equals(configService.getConfig(ConfigKey.NETWORK_OVN_ENABLE))) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "ovn服务不可用");
        }
        return configService.getConfig(ConfigKey.NETWORK_OVN_URI, "");
    }


    @Override
    public BaseResponse<CreateBridgeResponse> createBridge(CreateBridgeRequest request) throws OvnApiException {
        Type type = new TypeToken<BaseResponse<CreateBridgeResponse>>() {
        }.getType();
        return post("/api/bridge/", request, type);
    }

    @Override
    public BaseResponse<Map<String, String>> deleteBridge(String name) throws OvnApiException {
        Type type = new TypeToken<BaseResponse<Map<String, String>>>() {
        }.getType();
        return delete("/api/bridge/" + encodePath(name), type);
    }

    @Override
    public BaseResponse<NicXmlData> buildInterfaceXml(BuildInterfaceXmlRequest request) throws OvnApiException {
        Type type = new TypeToken<BaseResponse<NicXmlData>>() {
        }.getType();
        return post("/api/bridge/port/nic/xml", request, type);
    }


    private <T> T post(String path, Object requestBody, Type type) throws OvnApiException {
        String baseUrl = getOvnApiUri();
        HttpRequest request = HttpUtil.createPost(baseUrl + path);
        request.contentType("application/json;charset=utf-8");
        request.body(GsonBuilderUtil.create().toJson(requestBody));
        return execute(request, type);
    }

    private <T> T delete(String path, Type type) throws OvnApiException {
        String baseUrl = getOvnApiUri();
        HttpRequest request = HttpUtil.createRequest(Method.DELETE, baseUrl + path);
        return execute(request, type);
    }

    private <T> T execute(HttpRequest request, Type type) throws OvnApiException {
        int connectTimeout = configService.getConfig(ConfigKey.NETWORK_OVN_API_CONNECT_TIMEOUT_SECONDS, 30);
        int readTimeout = configService.getConfig(ConfigKey.NETWORK_OVN_API_READ_TIMEOUT_SECONDS, 30);
        request.setConnectionTimeout((int) TimeUnit.SECONDS.toMillis(connectTimeout));
        request.setReadTimeout((int) TimeUnit.SECONDS.toMillis(readTimeout));
        request.header("X-API-Key", this.configService.getConfig(ConfigKey.NETWORK_OVN_API_KEY));
        try (HttpResponse response = request.execute()) {
            if (response.isOk()) {
                return GsonBuilderUtil.create().fromJson(response.body(), type);
            } else {
                throw new OvnApiException("请求失败: " + response.body());
            }
        } catch (Exception e) {
            throw new OvnApiException("请求失败: " + e.getMessage(), e);
        }
    }


    @SneakyThrows
    private String encodePath(String value) {
        return URLEncoder.encode(value, "UTF-8");
    }
}
