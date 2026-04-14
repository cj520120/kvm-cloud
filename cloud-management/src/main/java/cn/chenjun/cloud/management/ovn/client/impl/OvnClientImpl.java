package cn.chenjun.cloud.management.ovn.client.impl;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.ovn.client.OvnClient;
import cn.chenjun.cloud.management.ovn.exception.OvnApiException;
import cn.chenjun.cloud.management.ovn.interceptor.AuthInterceptor;
import cn.chenjun.cloud.management.ovn.interceptor.LoggingInterceptor;
import cn.chenjun.cloud.management.ovn.model.request.BuildInterfaceXmlRequest;
import cn.chenjun.cloud.management.ovn.model.request.CreateBridgeRequest;
import cn.chenjun.cloud.management.ovn.model.response.*;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.util.ConfigKey;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OvnClientImpl implements OvnClient {

    private final OkHttpClient httpClient;
    private final ConfigService configService;
    private final NetworkService networkService;

    public OvnClientImpl(ConfigService configService, NetworkService networkService) {
        this.networkService = networkService;
        this.configService = configService;
        int connectTimeout = configService.getConfig(ConfigKey.NETWORK_OVN_API_CONNECT_TIMEOUT_SECONDS, 30);
        int readTimeout = configService.getConfig(ConfigKey.NETWORK_OVN_API_READ_TIMEOUT_SECONDS, 30);
        int writeTimeout = configService.getConfig(ConfigKey.NETWORK_OVN_API_WRITE_TIMEOUT_SECONDS, 30);
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(configService))
                .addInterceptor(new LoggingInterceptor())
                .build();
    }

    private String getOvnApiUri() {
        if (!Constant.Enable.YES.equals(configService.getConfig(ConfigKey.NETWORK_OVN_ENABLE))) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "ovn服务不可用");
        }
        return configService.getConfig(ConfigKey.NETWORK_OVN_URI, "");
    }

    @Override
    public HealthResponse health() throws OvnApiException {
        return get("/health", HealthResponse.class);
    }

    @Override
    public BaseResponse<CreateBridgeData> createBridge(CreateBridgeRequest request) throws OvnApiException {
        Type type = new TypeToken<BaseResponse<CreateBridgeData>>() {
        }.getType();
        return post("/api/network/create", request, type);
    }

    @Override
    public BaseResponse<Map<String, String>> deleteBridge(String name) throws OvnApiException {
        Type type = new TypeToken<BaseResponse<Map<String, String>>>() {
        }.getType();
        return delete("/api/network/delete/" + encodePath(name), type);
    }

    @Override
    public BaseResponse<NicXmlData> buildInterfaceXml(BuildInterfaceXmlRequest request) throws OvnApiException {
        Type type = new TypeToken<BaseResponse<NicXmlData>>() {
        }.getType();
        return post("/api/nic/xml", request, type);
    }


    private <T> T get(String path, Class<T> responseClass) throws OvnApiException {
        String baseUrl = getOvnApiUri();
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .get()
                .build();
        return execute(request, responseClass);
    }

    private <T> T get(String path, Type type) throws OvnApiException {
        String baseUrl = getOvnApiUri();
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .get()
                .build();
        return execute(request, type);
    }

    private <T> T post(String path, Object requestBody, Class<T> responseClass) throws OvnApiException {
        String baseUrl = getOvnApiUri();
        String json = GsonBuilderUtil.create().toJson(requestBody);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(body)
                .build();
        return execute(request, responseClass);
    }

    private <T> T post(String path, Object requestBody, Type type) throws OvnApiException {
        String baseUrl = getOvnApiUri();
        String json = GsonBuilderUtil.create().toJson(requestBody);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(body)
                .build();
        return execute(request, type);
    }

    private <T> T delete(String path, Type type) throws OvnApiException {
        String baseUrl = getOvnApiUri();
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .delete()
                .build();
        return execute(request, type);
    }

    private <T> T execute(Request request, Class<T> responseClass) throws OvnApiException {
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                handleErrorResponse(response.code(), responseBody);
            }

            return GsonBuilderUtil.create().fromJson(responseBody, responseClass);
        } catch (IOException e) {
            throw new OvnApiException("请求失败: " + e.getMessage(), e);
        }
    }

    private <T> T execute(Request request, Type type) throws OvnApiException {
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                handleErrorResponse(response.code(), responseBody);
            }

            return GsonBuilderUtil.create().fromJson(responseBody, type);
        } catch (IOException e) {
            throw new OvnApiException("请求失败: " + e.getMessage(), e);
        }
    }

    private void handleErrorResponse(int code, String responseBody) throws OvnApiException {
        if (code == 422) {
            try {
                HTTPValidationError validationError = GsonBuilderUtil.create().fromJson(responseBody, HTTPValidationError.class);
                throw new OvnApiException("参数验证失败", validationError);
            } catch (Exception e) {
                throw new OvnApiException("HTTP " + code + ": " + responseBody, code);
            }
        }
        throw new OvnApiException("HTTP " + code + ": " + responseBody, code);
    }

    @SneakyThrows
    private String encodePath(String value) {
        return URLEncoder.encode(value, "UTF-8");
    }
}
