package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Builder
@RequiredArgsConstructor
public class VxLanProxy {
    private final String baseUrl;
    private final String token;
    private final long connectTimeout;
    private final long readTimeout;


    public BaseResponse<CreateBridgeResponse> createBridge(CreateBridgeRequest request) {
        Type type = new TypeToken<BaseResponse<CreateBridgeResponse>>() {
        }.getType();
        return post("/api/bridge/", request, type);
    }

    public BaseResponse<Map<String, String>> deleteBridge(String name) {
        Type type = new TypeToken<BaseResponse<Map<String, String>>>() {
        }.getType();
        return delete("/api/bridge/" + encodePath(name), type);
    }

    public BaseResponse<BridgePortData> createBridgePort(CreateBirgePortRequest request) {
        Type type = new TypeToken<BaseResponse<BridgePortData>>() {
        }.getType();
        return post("/api/bridge/port/", request, type);
    }


    private <T> T post(String path, Object requestBody, Type type) {
        HttpRequest request = HttpUtil.createPost(baseUrl + path);
        request.contentType("application/json;charset=utf-8");
        request.body(GsonBuilderUtil.create().toJson(requestBody));
        return execute(request, type);
    }

    private <T> T delete(String path, Type type) {

        HttpRequest request = HttpUtil.createRequest(Method.DELETE, baseUrl + path);
        return execute(request, type);
    }

    private <T> T execute(HttpRequest request, Type type) {
        request.setConnectionTimeout((int) TimeUnit.SECONDS.toMillis(connectTimeout));
        request.setReadTimeout((int) TimeUnit.SECONDS.toMillis(readTimeout));
        request.header("X-API-Key", this.token);
        try (HttpResponse response = request.execute()) {
            if (response.isOk()) {
                return GsonBuilderUtil.create().fromJson(response.body(), type);
            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, "请求失败: " + response.body());
            }
        } catch (Exception e) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "请求失败: " + e.getMessage(), e);
        }
    }


    @SneakyThrows
    private String encodePath(String value) {
        return URLEncoder.encode(value, "UTF-8");
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBirgePortRequest {

        @SerializedName("bridge_name")
        private String bridgeName;

        private String mac;
        private String model;
        private String ip;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBridgeRequest {

        private String name;

        private String cidr;

        private String gateway;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseResponse<T> {

        private Integer code;

        private String msg;

        private T data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBridgeResponse {

        @SerializedName("app_bridge_name")
        private String appBridgeName;

        @SerializedName("ovn_bridge_uuid")
        private String ovnBridgeUuid;

        @SerializedName("ovn_bridge_name")
        private String ovnBridgeName;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BridgePortData {

        private String xml;

        @SerializedName("port_name")
        private String portName;

        private String mac;

        @SerializedName("bridge_name")
        private String bridgeName;
    }

}
