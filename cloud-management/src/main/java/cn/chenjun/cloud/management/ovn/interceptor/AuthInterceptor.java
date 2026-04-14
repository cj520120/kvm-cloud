package cn.chenjun.cloud.management.ovn.interceptor;

import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.util.ConfigKey;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AuthInterceptor implements Interceptor {

    private final ConfigService configService;

    public AuthInterceptor(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    @NotNull
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request original = chain.request();
        Request request = original.newBuilder()
                .header("X-API-Key", this.configService.getConfig(ConfigKey.NETWORK_OVN_API_KEY))
                .build();
        return chain.proceed(request);
    }
}
